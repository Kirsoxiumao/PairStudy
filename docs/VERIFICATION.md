# 本次构建和验证记录

验证日期：2026-10-01，Windows x64。

## 实际通过

| 项目 | 结果 |
|---|---|
| Maven package / JDK 17 | 通过，生成可执行 Spring Boot JAR |
| 默认 H2 测试数据库 | 7 项测试通过，0 失败 |
| 真正的 MySQL 8.0.32 | 生产 schema.sql 执行成功，7 项测试通过，0 失败 |
| 可执行 JAR 的 HTTP 启动 | 通过，真实 Tomcat 端口 18080；健康、注册、JWT 后的空间查询均成功 |
| Android Gradle 8.11.1 / AGP 8.9.2 | assembleDebug 通过，生成真实 APK |
| Android 网络单元测试 | 4 项通过，0 失败 |
| Android lintDebug | 通过，0 错误；9 条提示（6 条依赖更新、2 条可选角色资源名称查找、1 条备份声明兼容性建议） |
| APK 签名 | apksigner verify 通过，APK Signature Scheme v2，1 个签名者 |
| 核心 TODO / FIXME 检查 | 主代码没有遗留核心 TODO 或“自行实现”占位 |

源测试报告在 `docs/build-reports/`；发布文件 SHA-256 在 `release/SHA256SUMS.txt`。

## 自动化覆盖

- 注册、登录、错误密码、数据库 BCrypt 哈希检查。
- 无令牌与非法 JWT 被拒绝。
- 缺失月历查询参数、非法分页参数和缺失上传文件返回 400。
- 两人绑定、禁止自绑定、并发两位候选搭子只有一人成功、已有空邀请组的合并。
- 共享分区查询、归档保留历史、禁止向归档分区发布。
- 真正 multipart 图片上传、无效文件拒绝、图片与发布用户匹配、跨组盗用图片拒绝。
- 组内读取图片成功，匿名/跨组图片访问失败。
- 文字与图片不能同时为空，同一个 requestId 重试返回同一打卡。
- Feed 分页、日期查询、分区查询、双方日历与共同完成天数。
- 禁止跨组读打卡/分区、禁止删除搭子记录、删除自己的记录后统计重算。
- 03:59:59 与 04:00:00 API 实际 effectiveDate；createdAt 保留真实发布时刻。
- 跨年、闰日、服务器时区、连续打卡规则。
- Android API 使用配置的服务器地址；登录令牌自动携带。
- 私有图片携带令牌，外部来源图片不泄露令牌；拒绝带账号密码或 /api/ 后缀的服务器地址。

测试中的固定 Clock 和样例 JSON 仅用于测试代码；正式业务取服务器当前时间并查询数据库。

## 构建环境处理

原系统 Java 9 不能编译本项目；本次在工作区工具目录下载并使用 JDK 17、Maven 3.9.9、Android SDK 35 / Build Tools 35.0.0，没有替换系统 Java。

中文源目录能够生成 APK，但 Gradle 的 JVM 测试子进程出现测试类加载错误；因此最终构建在授权英文目录的源码副本上完成。MySQL 8.0.32 也使用独立英文测试数据目录。正式使用建议解压到 `C:\dev\PairStudy`。

最终 APK 已复制回标准输出 `android/app/build/outputs/apk/debug/app-debug.apk`，并额外复制到 `release/PairStudy-debug.apk`。后端可执行文件为 `release/pairstudy-backend.jar`。

可选角色资源通过名称查找，是为了在未来 PNG 尚未提供时仍能编译；该 API 为 discouraged 提示，并非已废弃 API。现有依赖锁定已验证的稳定组合，没有为了消除“有新版本”提示临时升级。

备份提示来自 lint 对 Android 12 以下 fullBackupContent 的建议；Manifest 已同时设置 allowBackup=false、fullBackupContent=false，并为 Android 12+ 排除云备份与设备迁移数据。

测试数据库仅监听 127.0.0.1:33307，JAR HTTP 测试仅监听 127.0.0.1:18080，测试结束已关闭；未改动原有 MySQL80 服务和原数据库。

## 尚需真机验收

当前没有连接的 Android 手机或模拟器。因此没有声称已在华为手机安装、实际打开所有页面或完成两台手机 UI 联调，也没有生成伪造截图。

需要按 `docs/ACCEPTANCE.md` 在两台支持 Android APK 的手机上确认安装、图片选择器回退、字体布局、网络环境和双方刷新。纯 HarmonyOS NEXT 不支持本 Android APK。

Debug APK 默认服务器地址为 Android 模拟器地址 `http://10.0.2.2:8080/`。真机首次打开后，在登录页“设置服务器地址”填写电脑的局域网 IPv4，并按 README 配置和启动 MySQL/后端。

交付不是已部署的公共服务；使用者需要提供数据库密码和服务运行环境。没有真实密码或个人 JWT 密钥写入源码、APK 或压缩包。
