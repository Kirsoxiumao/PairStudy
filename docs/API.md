# PairStudy API

根地址如 `http://192.168.1.100:8080`。除注册、登录和健康检查外，必须使用 `Authorization: Bearer <token>`。所有 JSON 为 UTF-8。

成功：`{"code":200,"message":"success","data":...}`；失败使用相应 HTTP 状态码及 `{"code":400,"message":"可读错误","data":null}`。删除成功 data 为 null。

401 登录失效，403 无权限，404 不存在或记录属于其他组，409 冲突/未绑定，413 上传太大，500 通用服务器错误。客户端不会显示堆栈。

## 登录

| 方法 | 路径 | 请求 |
|---|---|---|
| POST | /api/auth/register | username、password、nickname |
| POST | /api/auth/login | username、password |
| GET | /api/health | 无参数，仅检查服务可达 |

用户名：3–32 位字母、数字、下划线。密码：8–64 字符、UTF-8 最多 72 字节（BCrypt 限制）。昵称：1–40 字符。返回 `token`、`user{id,username,nickname,avatar,groupId}`，永不返回密码哈希。

JWT：HS256、issuer=pairstudy、subject=用户 ID、默认 168 小时有效期，严格验证签名/issuer/过期时间。groupId 每次从数据库读取，客户端不能用自传 groupId 越权。

## 双人空间

| 方法 | 路径 | 请求 |
|---|---|---|
| POST | /api/pair/create-invite | 空请求 |
| POST | /api/pair/bind | `{"inviteCode":"16位十六进制字符"}` |
| GET | /api/pair/info | 无 |

返回 `groupId,userA,userB,inviteCode,createdAt,boundAt,effectiveDate,serverNow,nextResetAt`。未创建邀请时组字段为 null；未完成绑定时 userB 为 null，客户端显示绑定页。`boundAt` 是实际绑定时间，与邀请创建时间不同。

身份 A/B 固定在数据库中；日历接口按请求者返回 self/partner，B 查看时也始终自己在左。重复生成邀请码返回当前邀请码；已绑定不能加入第三人，不允许自绑定。

## 分区

| 方法 | 路径 | 请求 |
|---|---|---|
| GET | /api/categories | 返回当前组全部分区，含 archived=true 的历史分区 |
| POST | /api/categories | name、iconName、sortOrder |
| PUT | /api/categories/{id} | name、iconName、sortOrder |
| DELETE | /api/categories/{id} | 归档，不物理删除 |

名称 1–40 字符，iconName 1–32 字符，sortOrder 0–100000。排序为 archived、sortOrder、id。创建后双方共享；归档分区不能用于新打卡，也不能再改名。Android 上下移动后会保存实际 sortOrder。

## 图片上传与访问

`POST /api/upload/image`，multipart 字段名 `file`，每次上传一张。

返回 `{"id":123,"imageUrl":"/uploads/checkin/<随机文件名>.jpg"}`。发布时使用该 id；不能使用他人上传的图片，不能重复使用已绑定打卡的图片。上传前必须完成双人绑定。

单张 ≤8 MB；扩展名 jpg/jpeg/png/webp，且校验实际内容签名。Android 先转换为质量 85、最长边约 1600 的 JPEG。服务端文件名为 UUID，不使用客户端文件名作为路径。

`GET /uploads/checkin/{filename}` 返回二进制图片，**也需要 Bearer token**；未发布的图片只有上传者可访问，发布后同组双方可访问。响应禁止缓存并设置 nosniff。不会公开映射整个 uploads 目录。

## 打卡

`POST /api/checkins`：

```json
{
  "categoryId": 1,
  "content": "学习记录",
  "imageIds": [123],
  "requestId": "每次草稿生成并在重试时复用的 UUID"
}
```

正文最多 5000 字，最多 9 张图片；正文和图片不能同时为空。不要提交 effectiveDate/createdAt/userId/groupId，服务器自行计算和判断。requestId 用于防止网络重试重复发布。

返回字段：`id,userId,nickname,avatar,role,categoryId,categoryName,content,effectiveDate,createdAt,images`。role 为实际 A/B。图片按 sortOrder 排序。

| 方法 | 路径 | 查询参数 |
|---|---|---|
| GET | /api/checkins/feed | page=1、size=20 |
| GET | /api/checkins/{id} | 无 |
| GET | /api/checkins/date/{yyyy-MM-dd} | page=1、size=20、author=all/self/partner |
| GET | /api/checkins/category/{categoryId} | page=1、size=20、author=all/self/partner |
| DELETE | /api/checkins/{id} | 只能删除自己的记录 |

分页接口返回 `items,page,size,total,hasMore`；size 范围 1–50。排序 createdAt 降序，再 id 降序。删除记录会更新签到和统计结果，图片引用级联删除，文件本身保留供后续维护清理。

## 日历和统计

`GET /api/calendar/month?year=2026&month=10` 返回整个月，每天 `date,selfChecked,partnerChecked`。只按 effectiveDate 判断，至少一条记录即 true。

`GET /api/profile` 返回 `user,partner,boundAt`。

`GET /api/profile/statistics` 返回：

- totalCheckins：个人打卡条数。
- monthDays：当前学习日所属月份中个人打卡的不同 effectiveDate 数。
- currentStreak：从今天（若今日未完成，则从昨天）向前连续完成的学习日数。
- togetherDays：从绑定所属学习日开始，双方各有至少一条记录的日期数量。
- effectiveDate：服务器当前学习日。

所有业务时间由 `StudyDayUtil` 使用 Asia/Shanghai 计算；createdAt 为该时区的本地时间字符串，serverNow/nextResetAt 为含 Z 的 ISO Instant。
