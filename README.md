# PairStudy · 双人学习手记

仅供两位好友共同使用的 Android 学习打卡应用。两台手机分别注册，通过邀请码绑定后，共享分区、图文动态、双半圆日历和统计。页面数据通过 Spring Boot API 读取 MySQL，没有内置模拟账号、固定动态或假 API。

## 功能

- 用户名/密码注册登录，BCrypt 密码哈希、HS256 JWT 登录鉴权。
- 16 位随机邀请码，最多两人，一个用户只能属于一个当前空间；事务保护并发绑定。
- 自定义共享分区：创建、改名、上下排序、归档；归档后历史记录保留。
- 文字及 0–9 张图片打卡；文字和图片不能同时为空。
- 系统 Photo Picker（不可用时自动回退系统文件选择器），预览、移除、压缩后真实 multipart 上传。
- 双人图文动态，20 条一页，加载更多，图片大图预览，只能删除自己的记录。
- 月历 Canvas 左右半圆：左边自己、右边搭子；点击某日分别查看双方全部记录。
- 分区历史支持全部/只看我/只看搭子。
- 个人总次数、本月完成天数、连续天数、双方共同完成天数。
- 登录页可直接设置服务器地址，无需为了更换局域网 IP 重新编译 APK。
- 浅色主题、角色图片插槽；不需要 Google Play 服务。

## 技术栈和工程

Android：Kotlin 2.1.20、Compose BOM 2025.05.01、Material 3、MVVM、StateFlow、Navigation Compose、Retrofit、OkHttp、Coroutines、Coil、DataStore。没有为无必要的缓存引入 Room。

后端：JDK 17、Spring Boot 3.5.6、Spring Security、MyBatis 3.0.5、MySQL 8.0、Maven。单体服务，无 Redis、Kafka、Docker。

```text
PairStudy/
  android/                       Android Studio 独立工程，含 Gradle Wrapper
    app/src/main/java/com/pairstudy/app/
      data/api/                  ApiService、RetrofitClient、ServerConfig
      data/local/                DataStore 登录状态和地址
      data/model/                API DTO
      data/repository/           统一响应与错误处理
      ui/                        ViewModel / StateFlow
      ui/screen/                 auth、home、calendar、checkin、category、profile
      ui/component/              Canvas 日历圆、动态卡片、图片预览、角色插槽
      ui/theme/                  全部主题色
      navigation/                Compose 页面路由
      util/                      图片压缩
  backend/                       Maven 独立工程
    src/main/java/com/pairstudy/
      controller/ service/ mapper/ entity/ dto/ vo/
      config/ security/ exception/ util/
    src/test/                    时间规则和真实接口集成测试
    application-example.yml     本地配置示例，不包含真实密码
  database/schema.sql           正式 MySQL 建表 SQL
  database/demo_data.sql        空数据说明；请通过注册和发布创建真实数据
  scripts/                      构建、独立 MySQL 验证、打包脚本
  docs/                         API 说明、验收记录
  release/                      本次构建产物（如构建成功）
```

## 1. 安装开发环境

1. 安装 JDK 17，并设置 `JAVA_HOME` 为 JDK 目录，例如 `C:\Java\jdk-17`；把 `%JAVA_HOME%\bin` 加入 PATH。
2. 用 `java -version` 确认是 17。旧 Java 8/9/11 无法构建本工程。
3. 安装 Maven 3.9，执行 `mvn -version`，确认它使用 JDK 17。
4. 安装 Android Studio，在 SDK Manager 安装 **Android SDK Platform 35**、**Build Tools 35.0.0**、Platform Tools。
5. 安装并启动 MySQL 8.0。记住自己设置的 MySQL 管理员密码。

建议将解压后的项目放到 `C:\dev\PairStudy` 这样的英文路径。工程已允许中文路径，但部分 Windows/SDK 工具仍可能对非英文路径敏感。

官方版本配置说明：[AGP/Gradle 兼容关系](https://developer.android.com/build/releases/about-agp)、[Compose BOM](https://developer.android.com/develop/ui/compose/bom)、[MyBatis Spring Boot Starter](https://github.com/mybatis/spring-boot-starter)。依赖版本已固定，无动态 `+` 版本。

## 2. 创建 MySQL 数据库

在 MySQL Workbench 中连接本机数据库，打开 `database/schema.sql` 并执行全部内容。这会创建 `pairstudy` 数据库和表，不会生成测试账号。

也可进入 MySQL 命令行：

```text
mysql -u root -p
SOURCE C:/dev/PairStudy/database/schema.sql;
```

用数据库管理员创建仅供本服务使用的账号（请替换密码）：

```sql
CREATE USER 'pairstudy'@'localhost' IDENTIFIED BY '换成你自己的强密码';
GRANT SELECT, INSERT, UPDATE, DELETE ON pairstudy.* TO 'pairstudy'@'localhost';
```

后端只需连接本机 MySQL，不需要将 3306 端口开放给手机。

## 3. 配置和启动后端

在 PowerShell 中进入 `backend`：

```powershell
cd C:\dev\PairStudy\backend
$env:DB_USER = 'pairstudy'
$env:DB_PASSWORD = '你上一步设置的数据库密码'
# 每次重新启动必须使用同一个密钥；更换后，已有用户需要重新登录。
# 首次生成后保存到你自己的安全配置中，不要提交到代码仓库。
$secretBytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
mvn clean package
java -jar target\pairstudy-1.0.0.jar
```

第一次 Maven 构建需要下载依赖。看到 `Started PairStudyApplication` 后，电脑浏览器打开 `http://localhost:8080/api/health`，应得到 `code: 200`。健康接口只检查服务可访问，注册/登录才会实际访问数据库。

也可使用 `mvn spring-boot:run`。后端监听 `0.0.0.0:8080`。可用 `PORT`、`DB_URL`、`DB_USER`、`DB_PASSWORD`、`JWT_SECRET`、`UPLOAD_DIR` 环境变量覆盖设置。

交付包中若已有 `release/pairstudy-backend.jar`，首次使用可以跳过 Maven 构建：仍先创建数据库、配置环境变量，然后在 `backend` 目录执行 `java -jar ..\release\pairstudy-backend.jar`。

如果希望持久化本机配置，将 `backend/application-example.yml` 复制为同目录 `application-local.yml`，填写数据库密码与至少 32 字节随机 JWT 密钥，然后：

```powershell
java -jar target\pairstudy-1.0.0.jar --spring.profiles.active=local
```

**必须从 backend 目录启动**，这样外部 `application-local.yml` 和默认 `uploads` 位置一致。示例 JWT 占位内容不会被服务接受。不要把本地密码配置放入压缩包或 Git。

## 4. Android Studio 打开和配置

1. 选择 Open，打开 **`PairStudy/android`**，不是外层总目录。
2. 在 Settings → Build Tools → Gradle 将 Gradle JDK 设为 JDK 17。
3. 等待 Gradle Sync 完成；SDK 缺失时按 IDE 提示安装 Android 35。
4. `local.properties` 由 Android Studio 创建，指向你的 SDK；它不应提交。
5. 默认地址集中在 `android/gradle.properties`：

```properties
PAIR_STUDY_BASE_URL=http://10.0.2.2:8080/
```

`10.0.2.2` 仅用于 Android Studio 模拟器访问电脑。**真机必须改为电脑局域网 IPv4**，例如：

```properties
PAIR_STUDY_BASE_URL=http://192.168.1.100:8080/
```

地址要以 `/` 结尾，**不要添加 `/api/`**。也可在 APK 的登录页点击“设置服务器地址”，输入同样的地址；该设置保存在 DataStore 中，优先于编译默认值，退出登录后仍保留。

Debug 允许局域网 HTTP；Release 禁止明文网络，必须使用 HTTPS 地址和自己的签名配置。首次交付的 debug APK 用于你们的个人安装测试。

## 5. 手机和电脑连接同一局域网

1. 电脑运行后端，两台手机和电脑连接同一 WiFi。
2. 电脑执行 `ipconfig`，查找正在使用的 WiFi/以太网网卡 **IPv4 地址**，例如 `192.168.1.100`。
3. 手机浏览器先打开 `http://192.168.1.100:8080/api/health`。
4. 若无法访问，在 Windows 防火墙中允许 Java 或入站 TCP 8080；建议仅作用于专用网络。不要为排查问题关闭全部防火墙。
5. 如果路由器启用了访客隔离/AP 隔离，换到允许设备互访的 WiFi。VPN 也可能影响本地连接。
6. 在两台手机登录页填入同一服务器地址。

**手机里的 localhost / 127.0.0.1 指向手机自身，不是电脑。** 电脑休眠或关闭后端后，手机无法同步。若要不在同一 WiFi 使用，应把后端和 MySQL 部署到长期运行的服务器，经 HTTPS 提供访问；数据和 uploads 需要一起备份。

## 6. 构建 APK

在 `android` 目录打开 PowerShell：

```powershell
.\gradlew.bat assembleDebug
```

标准输出路径：

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

可在命令行覆盖编译默认地址：

```powershell
.\gradlew.bat assembleDebug -PPAIR_STUDY_BASE_URL=http://192.168.1.100:8080/
```

完整检查：

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

`scripts/build-android.ps1` 也支持显式指定 `-JavaHome`、`-SdkRoot`、`-BaseUrl`。根目录的 `release/PairStudy-debug.apk` 是本次构建的副本（具体以 `docs/VERIFICATION.md` 为准）。

## 7. 安装到华为手机

适用于 **Android 8.0/API 26 及以上、能够安装 Android APK 的华为设备/系统**。不依赖 Google 服务。不能安装 Android APK 的纯 HarmonyOS NEXT 设备不适用本 Android 工程。

1. USB 连接电脑，手机选择“文件传输”。
2. 将 `app-debug.apk` 或 `release/PairStudy-debug.apk` 复制到手机 Download 文件夹。
3. 在手机“文件管理”中打开 APK；根据系统提示允许该文件管理器安装应用。
4. 安装后打开 PairStudy，在登录页设置后端服务器地址。
5. 两台手机各注册一个账号；A 生成邀请码并分享给 B，B 输入绑定，A 点击刷新进入。
6. 任意一方创建分区、发布图片打卡，另一方首页刷新即可看到。

也可启用 USB 调试后执行 `adb install -r app-debug.apk`。如果提示签名不一致，不要直接卸载：先确认已有账号和服务器数据能恢复，再选择保留原签名构建或卸载旧测试包。正式版本请自行创建并妥善保存签名密钥。

## 学习日规则

唯一规则入口是后端 `util/StudyDayUtil.java`，固定业务时区 `Asia/Shanghai`，不是手机时区：

| 实际发布时间 | effectiveDate |
|---|---|
| 2026-10-02 00:35 | 2026-10-01 |
| 2026-10-02 03:59:59 | 2026-10-01 |
| 2026-10-02 04:00:00 | 2026-10-02 |
| 2026-10-02 23:50 | 2026-10-02 |

`createdAt` 是实际发布时间；`effectiveDate` 是学习日。客户端不提交日期，后端取同一个 Instant 同时计算两者。统计/签到/日期详情都查询 effectiveDate，不需要凌晨定时清空表。

客户端根据服务器 `nextResetAt`/`serverNow` 的差值安排刷新，并在回到前台时重新读取；网络中断时显示错误，最终归属仍以后端发布时为准。

连续天数：今天有打卡就从今天向前计数；今天还未完成时，从昨天向前计数，直到今天学习日截止才会断签。同一天多次打卡只算 1 天。“共同坚持”是绑定以来双方都打卡的不同学习日数量，**不是个人连续天数**。

## 替换名称、主题和角色 PNG

- 名称：修改 `android/app/src/main/res/values/strings.xml` 的 `app_name`，首页与启动器会一起更新。
- 颜色：修改 `ui/theme/Theme.kt` 的 `StudyColors` 和 `PairStudyTheme`。
- 将你拥有使用权的 PNG 放进 `android/app/src/main/res/drawable-nodpi/`，采用以下文件名：
  `character_home.png`、`character_calendar.png`、`character_empty.png`、`character_success.png`。
- `CharacterSlot` 按名称寻找资源；找不到时使用 `placeholder_character.png`，仍不存在就显示通用 Compose 叶子图形。缺少未来角色 PNG 不影响编译。成功反馈目前是轻量 Snackbar，角色槽组件可用于后续成功界面。
- 当前工程没有下载或生成任何二次元角色图片。

## 数据与权限设计

SQL 主要表：`user`、`pair_group`、`category`、`checkin`、`checkin_image`。额外的 `uploaded_image` 保存图片上传归属/使用状态，`pair_lock` 是绑定操作使用的单行事务锁。

API 不接收 userId/groupId 作为访问凭证；从 JWT 得到用户再查询当前组。所有记录、分区和图片都有组权限检查。绑定前只能管理邀请码，不能创建孤立的学习数据。双方都生成了邀请码也可以绑定，加入者未使用的空邀请组会被合并掉。

图片保存到 `backend/uploads/checkin/`，数据库保存 URL 和元信息，不保存 Base64。`/uploads/checkin/...` 由受保护的资源接口提供，不能匿名访问，Coil 自动携带登录令牌。上传限制为每张 8 MB，校验扩展名及文件内容签名；Android 端 JPEG 压缩质量 85、最长边约 1600。

发布使用每次草稿的 requestId 做幂等处理，重试不会重复创建同一条打卡；上传的图片必须由自己上传且尚未用于另一条打卡。第一版未做孤立图片的自动定时清理，未发布或已删除记录的文件可能占用磁盘，应定期备份和按数据库引用检查清理。

JWT 有效期默认 168 小时，到期重新登录；退出登录会清理客户端登录状态及内存图片缓存。第一版没有服务器会话撤销/刷新令牌系统。手机登录状态禁用系统备份。

## 常见错误

| 现象 | 排查方法 |
|---|---|
| Connection refused / 网络连接失败 | 后端是否运行、IP/8080 是否正确、手机与电脑同一 WiFi、防火墙是否允许、电脑是否休眠 |
| 手机打不开电脑 localhost | 改为电脑 `ipconfig` 中的局域网 IPv4；模拟器才用 10.0.2.2 |
| Cleartext HTTP traffic not permitted | 局域网使用 debug 包；release 必须配置 HTTPS，不要随意放开生产明文流量 |
| MySQL Access denied | 核对 DB_USER/DB_PASSWORD 和账号的 localhost 授权；先用同账号连接 MySQL |
| Unknown database / Table doesn't exist | 完整执行 database/schema.sql，并确认 DB_URL 指向 pairstudy |
| JWT_SECRET 配置错误 | 设置至少 32 字节随机密钥，不能保留示例占位符；重启后端 |
| 404 | Base URL 只写到端口和末尾 /；不要重复添加 api；检查后端是否为此工程 |
| 图片无法显示 | 确认文件在 uploads/checkin；不要只迁移数据库；同组账号登录后访问，匿名浏览器会被拒绝 |
| Gradle 无法下载依赖 | 确认能访问 Google Maven、Maven Central、Gradle 官方源，检查 Android Studio 代理配置 |
| SDK location not found | 用 Android Studio 打开 android；生成 local.properties，或设置 ANDROID_HOME |
| Unsupported class file / requires Java 17 | 调整 JAVA_HOME 和 Android Studio Gradle JDK，不要继续用系统旧 Java |
| 中文路径构建失败 | 移到 C:\dev\PairStudy 后重试；工程已配置路径检查兼容选项 |
| 登录失效 | JWT 到期或服务器换了密钥，重新登录即可 |
| 邀请码已使用 | 该空间已有两人；第一版不提供解除绑定，避免历史数据归属不明 |

## 自动化测试与部署验收

后端 `mvn test` 默认使用仅用于测试的 H2 MySQL 兼容模式，加载正式 schema 的表结构，测试真实 Controller/Service/Mapper。正式运行使用 MySQL 驱动和数据源，没有 H2 运行依赖。

若本机安装了 MySQL Server，可运行 `scripts/verify-mysql.ps1 -JavaHome <JDK目录> -Maven <mvn.cmd路径>`。脚本在 `backend/target/mysql-verification` 初始化独立测试数据目录，只监听 `127.0.0.1:33307`，执行同一组接口测试，结束后关闭自己的测试进程。它不连接现有 3306 服务。

若本机 MySQL 不支持中文数据路径，用 `-TestRoot C:\dev\pairstudy-mysql-test` 指定独立英文目录。加 `-JarSmoke` 可额外在临时 `127.0.0.1:18080` 启动已打包 JAR，通过真实 HTTP 检查健康、注册及 JWT 鉴权；脚本结束会关闭测试进程。33307/18080 应当空闲，测试数据仅用于验证，不能作为正式数据库使用。

接口详见 `docs/API.md`。本次实际执行的结果、产物校验值与尚需真机验证的项目见 `docs/VERIFICATION.md`。

## 源码压缩包

在项目根目录执行 `python scripts/package.py`，会在上一层生成 `PairStudy.zip`。包含 Android/后端源代码、Gradle Wrapper、SQL、文档及 release 产物；排除 build、target、SDK/JDK/Maven 工具、缓存、uploads、local.properties、本地密码配置和签名密钥。
