# Dice Room 项目技术汇报


## 1. 总体架构

项目前后端分离 + 三层后端结构。

```
浏览器 (Vue 3 SPA)
   │  组件事件 → 调用 api/index.js (Axios, baseURL=/api, 自动带 Bearer Token)
   ▼
开发: Vite Dev Server (:5173) --proxy /api--> 后端 :8080
上线: Caddy (:80/:443) --静态文件 + reverse_proxy /api--> 后端 :8080
   ▼
Spring Boot (:8080)
   ├── controller 层   接收 HTTP 请求、鉴权入口、参数/响应 DTO(record)
   ├── service 层      业务规则、权限校验、事务性逻辑
   ├── backend 层      领域对象（用户/房间/消息/角色卡/骰子），彼此解耦
   └── repository 层   JDBC + PreparedStatement 访问 SQLite
   ▼
SQLite 数据库文件 dice_room.db （建表脚本 src/database/schema.sql）
```

层次与功能如下：
| 层次 | 目录 | 功能 |
| --- | --- | --- |
| UI 表现层 | `src/frontend` | Vue 页面、路由、Pinia 状态、Element Plus 组件、Axios 请求封装 |
| UI 接口层 | `src/controller` | REST 控制器、鉴权入口、请求/响应 DTO |
| 业务层 | `src/service` | 用户/房间/消息/掷骰/角色卡/邀请码业务规则与权限校验 |
| 领域层 | `src/backend` | 纯领域对象与算法（骰子表达式、密码哈希接口等） |
| 数据层 | `src/repository` | JDBC 数据访问、SQL 执行、结果集映射、分页 |
| 数据库 | `src/database/schema.sql` | SQLite 建表脚本与索引 |

后端通过 `AppConfig` 以构造器注入方式装配所有 Bean 。

---

## 2. 前端到后端的调用逻辑

### 2.1 example

1. 用户在 `RoomView.vue` 输入 `1d100` 并点发送，组件方法调用 `api.post('/rooms/{roomId}/messages/dice', { content })`。
2. `src/api/index.js` 的请求拦截器自动从 Pinia 的 `useUserStore().token` 取出 token，
   注入请求头 `Authorization: Bearer <token>`。
3. 请求走 `baseURL=/api`。开发期由 Vite 的 `server.proxy` 把 `/api` 转发到 `http://localhost:8080`；
   上线期由 Caddy 的 `handle /api/*` 反向代理到后端 `127.0.0.1:8080`。
4. Spring Boot 的 `MessageController.sendDiceMessage()` 接收请求：
   先调 `authService.requireUser(authorization)` 校验 token，再调 `messageService.sendDiceMessage(...)`。
5. `MessageService` 先通过 `RoomService` 确认调用者是房间成员，再调用 `DiceService` 解析并投掷骰子，
   把结果文本作为消息，经 `MessageRepository.insert()` 写入 SQLite。
6. Controller 把领域对象 `Message` 转换为 `MessageResponse`（record）返回，Spring 自动序列化为 JSON。
7. 前端拿到 JSON 后刷新消息列表。若后端抛异常，`GlobalExceptionHandler` 会转成统一的
   `{ "error": "..." }` 结构与合适的 HTTP 状态码；前端响应拦截器据此弹出 `ElMessage` 提示，
   遇到 401 则自动登出并跳回登录页。

### 2.2 鉴权机制

- 登录成功后 `AuthService` 生成一个随机 Base64 token，存入内存 `ConcurrentHashMap`（带 7 天 TTL），返回给前端。
- 前端把 token 存入 `localStorage`（通过 Pinia store 持久化），之后每个请求都带上。
- 每个受保护接口的第一行都是 `authService.requireUser(...)`／`requireAdmin(...)`／`requireSelfOrAdmin(...)`，
  分别对应“需登录”“需管理员”“需本人或管理员”。
- 由于token 存在内存中，还没有做持久化，所以如果我们后台重启，所有用户都需要重新登录。

### 2.3 跨域上线or部署

- `WebConfig` 配置 CORS：本地地址始终放行，线上域名通过环境变量 `DICE_ROOM_SITE` 注入（与 Caddyfile、run.sh 共用同一变量）。
- `run.sh` 统一用 `DICE_ROOM_SITE` / `DICE_ROOM_BACKEND` / `DICE_ROOM_WEBROOT` 。

---

## 3. 功能说明


### 3.1 根目录与构建/部署文件

- **`pom.xml`** — Maven 配置。
Java 17；依赖：`spring-boot-starter-web 3.5.2`、`sqlite-jdbc 3.53.1.0`、`bcrypt 0.10.2`。`sourceDirectory` 指向 `src`；用 `exec-maven-plugin` 以 `app.DiceRoomApplication` 为主类运行。
- **`Caddyfile`** — 上线期的反向代理。站点名取 `{$DICE_ROOM_SITE:localhost}`，`/api/*` 反代到后端 `{$DICE_ROOM_BACKEND:127.0.0.1:8080}`，其余路径作为 SPA 提供静态文件（`try_files {path} /index.html`），开启 zstd/gzip 压缩。
- **`run.sh`** — 一键启动脚本。导出 `DICE_ROOM_SITE/BACKEND/WEBROOT` 三个环境变量并给默认值，检查 npm/mvn/caddy 是否存在，先 `npm run build` 构建前端，再后台启动后端（日志写入 `target/`），最后 `caddy run` 提供服务；带退出清理逻辑。
- **`.gitignore`** — 忽略构建产物、依赖目录、数据库文件等。
- **`README.md`** — 面向使用者的项目说明与运行指南。
- **`dice_room.db`** — SQLite 数据库文件。（目前是明文存储的，后续可以考虑加密）

### 3.2 应用入口与配置 `src/app/`

- **`DiceRoomApplication.java`** — Spring Boot 启动类，`@SpringBootApplication` 显式 `scanBasePackages` 扫描 `app/controller/service/repository/backend` 五个包。
- **`AppConfig.java`** — 手写 Bean 装配中心。创建并初始化 SQLite `Connection`（打开 `dice_room.db` 并执行 `schema.sql`），提供 `PasswordHasher`（BCrypt），并以构造器注入方式装配所有 Repository 与 Service Bean，集中表达依赖关系。
- **`WebConfig.java`** — CORS 配置。`/api/**` 放行本地地址，线上域名由 `DICE_ROOM_SITE` 环境变量注入，允许携带凭证、常用 HTTP 方法。

### 3.3 controller层 `src/controller/`

- **`UserController.java`** — `/api/users`。注册、登录、登出、初始化管理员、查询/修改个人资料、改登录名、改密码，以及管理员的用户列表/封禁/恢复/删除/改密。内含一组请求与响应 record。
- **`RoomController.java`** — `/api/rooms`。建房、按房间码加入、离开、查房间、我的房间列表、管理员全部房间、成员列表、改名/描述/标签、关闭/删除（房主与管理员两套）、改成员角色、禁言/解禁、绑定角色卡、改房内昵称。
- **`MessageController.java`** — `/api/rooms/{roomId}/messages`。发文本/私聊/明骰/暗骰/技能检定消息，获取可见消息、仅公开消息、分页消息、与某人的私聊记录，删除自己的消息。
- **`DiceController.java`** — `/api/dice`。单次掷骰 `/roll`、重复掷骰 `/repeat`、理智检定 `/san-check`、基于角色卡的技能检定 `/skill-check`。全部要求登录态鉴权。
- **`PlayerCardController.java`** — `/api/player-cards`。建卡、获取卡/卡面 sheet/原始 JSON、更新 sheet/JSON、按用户列卡、改基础信息、删卡。统一用 `requireAccessiblePlayerCard` 校验“本人或管理员”。
- **`InviteCodeController.java`** — `/api/invite-codes`。管理员生成邀请码、列出全部、停用、删除。
- **`GlobalExceptionHandler.java`** — 全局异常处理。（m目前没有做完整的错误日志）；`IllegalArgumentException`→400；`IllegalStateException` 按错误文案判定为 401（登录类）/403（权限类）/400；其他异常→500 且只返回“服务器内部错误”，统一响应体为 `{ "error": "..." }`。

### 3.4 业务层 `src/service/`

- **`AuthService.java`** — 会话鉴权。`login()` 校验凭证后生成 Base64 token 存入内存 Map（7 天 TTL）；`logout()` 移除 token；`requireUser()` 校验并刷新会话；`requireAdmin()`、`requireSelfOrAdmin()` 做权限判定。依赖 `UserService`。
- **`UserService.java`** — 用户业务。`registerUser()` 校验邀请码、哈希密码并存储、使用邀请码；`initializeAdmin()` 仅在无管理员时创建首个管理员；`loginUser()` 校验凭证与激活状态；改密/改资料/改登录名；封禁/恢复/删除；以及 `requireActiveUser()`、`requireAdmin()` 等校验。依赖 `UserRepository`、`InviteCodeService`、`PasswordHasher`。
- **`InviteCodeService.java`** — 邀请码逻辑。生成 8 位大写唯一码，校验使用上限与过期；`useInviteCode()` 累加使用次数并更新状态；`checkInviteCode()` 校验可用；`disableInviteCode()` 停用。依赖 `InviteCodeRepository`。
- **`RoomService.java`** — 房间业务。`createRoom()` 生成唯一 8 位房间码；`joinRoom()` 校验房间开放且未重复加入；`leaveRoom()` 禁止房主离开；改成员角色（禁止转移房主）、禁言/解禁、绑定角色卡、改房内昵称、关闭/删除（含管理员版）。依赖 `RoomRepository`、`UserService`。
- **`MessageService.java`** — 消息业务。发送文本/私聊/明骰/暗骰/技能检定/系统消息，发送前都先校验房间成员身份；`getVisibleMessages()` 返回对用户可见的消息（公开 + 自己收发的私密），支持分页。依赖 `RoomService`、`MessageRepository`。
- **`DiceService.java`** — 掷骰逻辑。`roll()`/`rollRepeated()`/`sanCheck()` 让 `DiceExpressionEvaluator`来计算；`checkSkill()` 从 `PlayerCardService` 取技能值，掷 d100 并按 CoC 规则判定大成功/极难/困难/普通成功/失败/大失败。依赖 `PlayerCardService`。
- **`PlayerCardService.java`** — 角色卡逻辑。建卡（默认模板）、读 sheet、更新 sheet/JSON（校验拥有者）、取技能值供检定、软删除机制。依赖 `PlayerCardRepository`、`UserService`。
- **`AdminInitialize.java`** — 管理员初始化工具类。`initialize()` 接收显式凭证，`initializeFromEnv()` 从环境变量读取，二者都委托 `UserService.initializeAdmin()`。

### 3.5 数据层 `src/repository/`

- **`DatabaseConnection.java`** — SQLite 连接方法。`open()` 通过 JDBC `jdbc:sqlite:` 建立连接并 `PRAGMA foreign_keys=ON` 开启外键防止孤儿数据。
- **`DatabaseInitializer.java`** — schema 初始化与迁移。读取 schema 文件按分号拆分逐条执行；`addColumnIfMissing()` 在缺失时给 `room_members` 补 `display_name` 列（用 `PRAGMA table_info` 检查）。
- **`UserRepository.java`** — 用户表 CRUD。增、按 id/登录名查、列全部、列管理员、判断是否存在管理员、改密码哈希/登录名/资料/状态、软删除；`applyStatus()` 在装载时回填状态。
- **`InviteCodeRepository.java`** — 邀请码表 CRUD。插入（status=ACTIVE）、按码查、列全部、更新使用次数/状态、删除，日期。
- **`RoomRepository.java`** — 房间与成员表 CRUD。建房（唯一房间码、房主自动入会为 OWNER）、多种查询、改基础信息/状态；成员侧：入会、离会（写 `left_at`）、改角色/禁言/绑卡/房内昵称、列活跃成员、判断是否活跃成员；标签以 JSON 数组字符串存储。
- **`MessageRepository.java`** — 消息表 CRUD。插入（区分 type 与 visibility）、按 id/房间查、可见消息查询（公开 + 自己收发，支持分页）、双向私聊查询、删除；系统消息允许 `sender_id` 为空。
- **`PlayerCardRepository.java`** — 角色卡表 CRUD。插入（card_json 空则归一为 `{}`，status=ACTIVE）、按 id 查（排除 DELETED）、按 owner 列卡、取 JSON、改基础信息/JSON、归档/软删除。
- **`Page.java`** — 通用分页 record。字段 `items/page/size/total`，方法 `totalPages()`、`hasNext()`。供消息分页查询使用。

### 3.6  `src/backend/dice/` 掷骰层

- **`DiceConstants.java`** — 上限常量：最大骰子数 100、最大骰面 100000、最大重复 20、表达式最大长度 120、最大骰子项 20、明细最多显示 30 次。
- **`DiceError.java`** — 掷骰相关的自定义 `RuntimeException`，用于非法表达式/超限/解析失败。
- **`DiceRoller.java`** — 单个骰子项的解析与投掷，正则 `(\d*)d(\d+|%)(?:(kh|kl|dh|dl)(\d+))?`，支持取高/取低/弃高/弃低。
- **`SafeArithmeticEvaluator.java`** — 求值，通过递归下降解析 `+ - *`、括号与一元符号，校验无残留非法字符从而防注入。
- **`DiceExpressionEvaluator.java`** — 表达式总入口。组合 `DiceRoller` 与 `SafeArithmeticEvaluator`，归一化全角字符、校验长度与项数、处理 `N#expr` 重复、构造优劣势骰（`2d20kh1`/`2d20kl1`）、执行带成功/失败损失率的理智检定。
- **`DiceTermResult.java`** — 单个骰子项结果（原始 token、合计、各次点数、备注），`formatDetail()` 显示明细（封顶 30 次）。
- **`DiceExpressionResult.java`** — 一条完整表达式结果（表达式、最终值、各项结果列表），`getDetails()` 格式化每一项。
- **`RepeatRollResult.java`** — 重复投掷结果（重复次数、表达式、各次的 `DiceExpressionResult`）。
- **`SanCheckResult.java`** — 理智检定结果（损失表达式、目标值、骰值、成功与否、理智损失的 `DiceExpressionResult`）。
- **`SkillCheckResult.java`** — 技能检定结果（用户/卡/技能名、技能值、困难值、极难值、骰值、成功等级），`isSuccess()` 对各级成功返回真。

### 3.7  `src/backend/user/` 用户与权限

- **`User.java`** — 用户实体（id、登录名、密码哈希、昵称、角色、状态、头像、简介、邮箱（目前没做smtp和邮箱登陆功能）），含 `suspend()/recover()/delete()` ，是被管理员所控制的软删除机制。
- **`UserRole.java`** — 枚举 `USER`/`ADMIN`，含 `isAdmin()`。
- **`UserStatus.java`** — 枚举 `ACTIVE`/`SUSPENDED`/`DELETED`，含状态判定方法。
- **`InviteCode.java`** — 邀请码（码、使用上限、已用次数、状态、过期时间）。`canUse()` 校验可用，`use()` 累加并在达上限时置 USED，`checkExpiration()` 过期置 EXPIRED。
- **`InviteCodeStatus.java`** — 枚举 `ACTIVE`/`EXPIRED`/`USED`/`DISABLED`。
- **`passwordHash/PasswordHasher.java`** — 密码哈希接口，定义 `hash()` 与 `matches()`。
- **`passwordHash/BCryptPasswordHasher.java`** — BCrypt 实现（COST=12），当前启用。
- **`passwordHash/Sha256PasswordHasher.java`** — SHA-256 备用实现（保留以示可替换性）。

### 3.8  `src/backend/room/` 房间

- **`Room.java`** — 房间实体（id、房间码、房主、名称、描述、状态、标签、成员、创建时间），创建时房主自动作为 OWNER 入会；含增删成员、查活跃成员、标签管理、`close()/delete()`。
- **`RoomMember.java`** — 房间成员（房间/用户 id、角色、禁言、绑定卡 id、房内昵称、加入/离开时间），含禁言、改角色、绑卡、改昵称、`leave()`。
- **`RoomRole.java`** — 枚举 `OWNER`/`PLAYER`/`BOT`，含 `isOwner()`。
- **`RoomStatus.java`** — 枚举 `OPEN`/`CLOSED`/`DELETED`，含 `isOpen()`。

### 3.9  `src/backend/message/` 消息

- **`Message.java`** — 消息实体（id、房间、发送者、接收者[0=公开]、类型、可见性、内容、时间戳），`setContent()` 校验非空。
- **`MessageType.java`** — 枚举 `TEXT`/`DICE`/`SKILL_CHECK`/`SYSTEM`，含类型判定方法。
- **`MessageVisibility.java`** — 枚举 `PUBLIC`/`PRIVATE`，含可见性判定方法。

### 3.10 `src/backend/playercard/` COC 角色卡

- **`PlayerCard.java`** — 调查员角色卡主对象（归属、id、时代、基础信息、属性、状态、技能表、武器、战斗信息、财产、现金资产、克苏鲁神话、背景故事、同伴、已玩模组）。
- **`BasicInformation.java`** — 基础信息（姓名[必填]、职业、年龄、性别、居住地、出生地、头像），`setName()` 校验非空。
- **`Characteristics.java`** — 九大属性（STR/DEX/SIZ/APP/CON/INT/POW/EDU/LUC）容器，外加 HP/MP/SAN 三项资源值，SAN 默认上限 99。
- **`Characteristic.java`** — 单条属性（名称、代码、骰公式、值[0-100]、困难值=值/2、极难值=值/5），`setValue()` 自动算困难/极难。
- **`ResourceValue.java`** — 资源值（当前/初始/上限，均非负），用于 HP/MP/SAN。
- **`CharacterStatus.java`** — 角色状态布尔标志：重伤、昏迷、死亡、临时疯狂、永久疯狂、不定性疯狂。
- **`SkillSheet.java`** — 技能表（职业点、兴趣点、职业技能上限 75、其他上限 50），初始化 40+ 个 COC 默认技能；`getSkill()` 缺失抛错，`setSkill()` 缺失则建自定义技能。
- **`Skill.java`** — 单条技能（类别、名称、基础/职业/兴趣/成长值、成功率=总和、困难率=成功/2、极难率=成功/5、是否职业技能），setter 校验 0-100 并刷新总值。
- **`Weapon.java`** — 武器（名称、技能名、成功率、伤害公式、射程、是否穿刺、每轮攻击数、弹药、故障值）。
- **`CombatInfo.java`** — 战斗信息（伤害加值、体格、护甲[非负]、移动力[非负]）。
- **`CashAndAssets.java`** — 财务信息（信用评级[0-100]、现金、消费水平、资产）。
- **`Story.java`** — 背景叙事（个人描述、相貌、思想信念、重要之人、重要地点、宝贵之物、特质、伤疤、精神症状）。
- **`Companion.java`** — 同伴记录（角色名、关系、玩家名，构造后不可变）。
- **`CthulhuMythos.java`** — 神话知识（神话技能值[0-100] + 条目列表）。
- **`CthulhuMythosEntry.java`** — 单条神话条目（类型、名称、描述、来源，不可变）。
- **`ExperiencedScenarios.java`** — 已玩模组容器（条目列表）。
- **`ExperiencedScenarioEntry.java`** — 单条模组记录（模组名、经历，不可变）。
- **`CardValueValidator.java`** — 卡值校验工具：`checkPercentValue()` 校验 0-100、`checkNonNegative()` 校验非负。
- **`PlayerCardSheetTemplate.java`** — 角色卡 sheet 工厂：由 `PlayerCard` 经 mapper 生成默认 `PlayerCardSheet`（JSON 字符串或对象）。
- **`sheet/PlayerCardSheet.java`** — 可 JSON 序列化的卡面 DTO（version=1），含一组静态内部类对应基础信息/属性/资源/技能点/技能/武器/战斗/故事/财产/现金/神话/同伴/模组；属性、资源、状态用 `LinkedHashMap` 保持 JSON 字段顺序。
- **`sheet/PlayerCardSheetMapper.java`** — `PlayerCard` 与 `PlayerCardSheet` 间的双向映射（Jackson）。`fromPlayerCard()` 领域对象转 sheet；`fromJson()` 反序列化并迁移旧版属性结构；`toJson()` 序列化；`normalize()` 兜底初始化集合；`toSkillValue()` 抽取技能的成功/困难/极难率。

### 3.11 前端 `src/frontend/`

- **`index.html`** — SPA 入口 HTML，挂载点 `#app`，以 ES module 加载 `main.js`。
- **`package.json`** — 依赖项。
- **`vite.config.js`** — Vite 配置，启用 Vue 插件；开发服务器 `0.0.0.0:5173`，将 `/api` 代理到 `http://localhost:8080`（可由 `VITE_API_TARGET` 覆盖），`changeOrigin` 开启。
- **`src/main.js`** — 创建 Vue 应用，依次装入 Pinia、Vue Router、Element Plus，挂载到 `#app`。
- **`src/App.vue`** — 根组件，仅含 `<router-view>` 与全局样式。
- **`src/router/index.js`** — 路由：`/login`（公开）、`/`（主页）、`/profile`、`/admin`、`/room/:roomId`、`/card/:cardId`（均需登录）；全局前置守卫拦截未登录访问 `meta.auth` 路由并重定向到 `/login`。
- **`src/stores/user.js`** — Pinia 用户态。state：`token`（持久化到 localStorage）、`user`；action：`setLogin`（存 token 与用户）、`logout`（清空）。
- **`src/api/index.js`** — Axios 实例（`baseURL=/api`）。请求拦截器注入 `Bearer` token；响应拦截器统一提取错误文案弹 `ElMessage`，遇 401 自动登出并跳登录页。
- **`src/views/LoginView.vue`** — 登录/注册/初始化管理员三合一页，调用 `/users/login`、`/users/register`、`/users/admin/initialize`。
- **`src/views/HomeView.vue`** — 主页仪表盘，双栏展示“我的房间”和“我的角色卡”，支持建房/按码加入/建卡，跳转房间或卡详情；数据来自 `/rooms/user/{id}`、`/player-cards/user/{id}`。
- **`src/views/RoomView.vue`** — 房间主界面（消息列表 + 输入区 + 侧栏）。支持文本/私聊/明骰/暗骰/理智/技能检定多种消息，可见/公开/分页三种查看模式；侧栏含房间设置（房主可改名/描述/标签/关闭/删除）、绑卡、成员管理；每 3 秒轮询拉取消息。
- **`src/views/PlayerCardView.vue`** — 角色卡编辑器，分总览/技能/战斗/故事/JSON 多个 Tab，技能与属性自动算困难/极难值；数据来自 `/player-cards/{id}` 与 `/player-cards/{id}/sheet`。
- **`src/views/ProfileView.vue`** — 个人资料页，分资料/登录名/密码三块，调用 `/users/me/profile`、`/users/me/login-name`、`/users/me/password`。
- **`src/views/AdminView.vue`** — 管理后台，三块：用户表（封禁/恢复/删除/改密）、房间表（关闭/删除）、邀请码（生成/停用/删除），数据来自 `/users`、`/rooms/admin`、`/invite-codes`。

### 3.12 数据库 `src/database/schema.sql`

建表脚本，开启外键，建 6 张表与多个索引。

---

## 4. 后端的 REST API List

所有前缀为 `/api`。请求头基本携带了 `Authorization: Bearer <token>`，方便鉴权。
鉴权：
- **公开** = 无需登录；
- **登录** = 任意登录用户；
- **本人/管理员** = 资源属主或管理员；
- **管理员** = 仅管理员。

### 4.1 用户 `UserController` — `/api/users`

| 方法 | 路径 | 鉴权 | 说明 | 请求体 |
| --- | --- | --- | --- | --- |
| POST | `/register` | 公开 | 凭邀请码注册 | `{loginName, password, nickname, inviteCode}` |
| POST | `/login` | 公开 | 登录，返回 `{token, user}` | `{loginName, password}` |
| POST | `/admin/initialize` | 公开 | 初始化首个管理员（已存在则拒绝） | `{loginName, password, nickname}` |
| POST | `/logout` | 登录 | 退出登录 | — |
| GET | `/me` | 登录 | 当前用户信息 | — |
| PATCH | `/me/profile` | 登录 | 改昵称/头像/简介/邮箱 | `{nickname, avatarUrl, description, email}` |
| PATCH | `/me/login-name` | 登录 | 改登录名 | `{loginName}` |
| PATCH | `/me/password` | 登录 | 改自己密码（验旧密码） | `{oldPassword, newPassword}` |
| GET | `/{userId}` | 本人/管理员 | 查指定用户 | — |
| GET | `` (根) | 管理员 | 用户列表 | — |
| PATCH | `/{userId}/password` | 本人/管理员 | 重置指定用户密码 | `{password}` |
| POST | `/{targetUserId}/suspend` | 管理员 | 封禁用户 | — |
| POST | `/{targetUserId}/recover` | 管理员 | 恢复用户 | — |
| DELETE | `/{targetUserId}` | 管理员 | 删除用户 | — |

### 4.2 房间 `RoomController` — `/api/rooms`

| 方法 | 路径 | 鉴权 | 说明 | 请求体 |
| --- | --- | --- | --- | --- |
| POST | `` (根) | 登录 | 建房（生成 8 位房间码） | `{name}` |
| POST | `/join` | 登录 | 按房间码加入 | `{roomCode}` |
| POST | `/{roomId}/leave` | 登录 | 离开房间（房主不可离开） | — |
| GET | `/{roomId}` | 登录(成员) | 查房间（须已加入） | — |
| GET | `/user/{userId}` | 本人/管理员 | 某用户的房间列表 | — |
| GET | `/admin` | 管理员 | 全部房间 | — |
| GET | `/{roomId}/members` | 登录(成员) | 成员列表 | — |
| PATCH | `/{roomId}/name` | 登录(房主) | 改房间名 | `{name}` |
| PATCH | `/{roomId}/description` | 登录(房主) | 改房间描述 | `{description}` |
| POST | `/{roomId}/tags` | 登录(房主) | 添加标签 | `{tag}` |
| DELETE | `/{roomId}/tags` | 登录(房主) | 删除标签 | `{tag}` |
| POST | `/{roomId}/close` | 登录(房主) | 关闭房间 | — |
| DELETE | `/{roomId}` | 登录(房主) | 删除房间 | — |
| POST | `/admin/{roomId}/close` | 管理员 | 管理员关闭房间 | — |
| DELETE | `/admin/{roomId}` | 管理员 | 管理员删除房间 | — |
| PATCH | `/{roomId}/members/{targetUserId}/role` | 登录(房主) | 改成员角色（不可转移房主） | `{role}` |
| POST | `/{roomId}/members/{targetUserId}/mute` | 登录(房主) | 禁言成员 | — |
| POST | `/{roomId}/members/{targetUserId}/unmute` | 登录(房主) | 解除禁言 | — |
| PUT | `/{roomId}/members/card` | 登录(成员) | 绑定自己的角色卡 | `{cardId}` |
| PATCH | `/{roomId}/members/me/nickname` | 登录(成员) | 改房内昵称 | `{nickname}` |

### 4.3 消息 `MessageController` — `/api/rooms/{roomId}/messages`

| 方法 | 路径 | 鉴权 | 说明 | 请求体 / 查询参数 |
| --- | --- | --- | --- | --- |
| POST | `` (根) | 登录(成员) | 发文本消息 | `{content}` |
| POST | `/private` | 登录(成员) | 发私聊消息 | `{receiverId, content}` |
| POST | `/dice` | 登录(成员) | 发明骰消息 | `{content}`（骰子表达式） |
| POST | `/dice/private` | 登录(成员) | 发暗骰消息（仅自己可见） | `{content}` |
| POST | `/skill-check` | 登录(成员) | 发技能检定消息 | `{content}` |
| GET | `` (根) | 登录(成员) | 获取对自己可见的全部消息 | — |
| GET | `/public` | 登录(成员) | 仅获取公开消息 | — |
| GET | `/paged` | 登录(成员) | 分页获取可见消息 | `?page=1&size=50` |
| GET | `/private` | 登录(成员) | 与某用户的私聊记录 | `?userBId=` |
| DELETE | `/{messageId}` | 登录 | 删除自己发的消息 | — |

### 4.4 掷骰 `DiceController` — `/api/dice`

| 方法 | 路径 | 鉴权 | 说明 | 请求体 |
| --- | --- | --- | --- | --- |
| POST | `/roll` | 登录 | 单次掷骰，如 `2d6+3` | `{expression}` |
| POST | `/repeat` | 登录 | 重复掷骰，如 `3#1d100` | `{expression}` |
| POST | `/san-check` | 登录 | 理智检定，如 `1/1d6 60` | `{args}` |
| POST | `/skill-check` | 登录 | 按角色卡技能检定 | `{cardId, skillName}` |

### 4.5 角色卡 `PlayerCardController` — `/api/player-cards`

| 方法 | 路径 | 鉴权 | 说明 | 请求体 |
| --- | --- | --- | --- | --- |
| POST | `` (根) | 登录 | 建卡（返回卡面 sheet） | `{name}` |
| GET | `/{cardId}` | 本人/管理员 | 获取角色卡领域对象 | — |
| GET | `/{cardId}/sheet` | 本人/管理员 | 获取结构化卡面 sheet | — |
| PUT | `/{cardId}/sheet` | 本人/管理员 | 更新卡面 sheet | `{sheet}` |
| GET | `/{cardId}/json` | 本人/管理员 | 获取原始 JSON 卡面 | — |
| PUT | `/{cardId}/json` | 本人/管理员 | 整体替换 JSON 卡面 | `{sheet}` |
| GET | `/user/{userId}` | 本人/管理员 | 某用户的角色卡列表 | — |
| PATCH | `/{cardId}/basic` | 本人/管理员 | 改基础信息（名称/时代） | `{name, era}` |
| DELETE | `/{cardId}` | 本人/管理员 | 删除角色卡 | — |

### 4.6 邀请码 `InviteCodeController` — `/api/invite-codes`

| 方法 | 路径 | 鉴权 | 说明 | 请求体 |
| --- | --- | --- | --- | --- |
| POST | `` (根) | 管理员 | 生成邀请码 | `{usageLimit, expirationTimestampMillis}` |
| GET | `` (根) | 管理员 | 邀请码列表 | — |
| POST | `/{code}/disable` | 管理员 | 停用邀请码 | — |
| DELETE | `/{code}` | 管理员 | 删除邀请码 | — |

### 4.7 错误响应

所有接口出错时由 `GlobalExceptionHandler` 返回统一结构：

```json
{ "error": "错误描述文字" }
```

状态码映射：
- 参数错误 → 400；
- 未登录/登录过期 → 401；
- 权限不足 → 403；
- 未知异常 → 500
目前还没有进一步区分精细的错误。
---

## 5. 数据库

SQLite，建表脚本 `src/database/schema.sql`，启动时由 `AppConfig` → `DatabaseInitializer` 自动建表。

| 表 | 作用 | 关键约束 |
| --- | --- | --- |
| `users` | 账号、密码哈希、昵称、角色、状态、资料 | `login_name` 唯一；`role`/`status` 用 CHECK 限枚举 |
| `invite_codes` | 邀请码、上限、已用、状态、过期、创建者 | `code` 唯一；`usage_limit>0`；外键指向 `users` |
| `rooms` | 房间码、房主、名称、描述、状态、标签 | `room_code` 唯一；`tags_json` 存 JSON 数组 |
| `room_members` | 成员、角色、禁言、绑卡、房内昵称、加入/离开时间 | 部分唯一索引保证同房同人仅一条活跃记录 |
| `messages` | 房间消息、收发者、类型、可见性、内容 | CHECK 保证公开无接收者/私密有接收者 |
| `player_cards` | 角色卡名称、时代、状态、JSON 数据 | `status` 枚举含 ACTIVE/ARCHIVED/DELETED |

所有外键关系（用户↔房间↔成员↔消息↔角色卡）由 `PRAGMA foreign_keys=ON` 强制，并对登录名、房间码、消息房间 id 等高频查询字段建索引。软删除策略：用户/房间/角色卡用 `status=DELETED`，房间成员用 `left_at` 时间戳。

## 6. 技术栈

| 技术 | 版本 | 用途 |
| --- | --- | --- |
| Java | 17 | 后端语言 |
| Spring Boot Web | 3.5.2 | REST API / Web 容器 |
| SQLite JDBC | 3.53.1.0 | 数据库驱动 |
| BCrypt (favre) | 0.10.2 | 密码哈希 |
| Vue | 3.5 | 前端框架 |
| Vue Router | 4.5 | 前端路由 |
| Pinia | 2.3 | 前端状态管理 |
| Axios | 1.7 | HTTP 请求 |
| Element Plus | 2.9 | UI 组件库 |
| Vite | 6.0 | 前端构建/开发服务器 |
| Caddy | — | 上线期静态托管 + 反向代理 |

## 一些缺点
1. **依赖注入手写化**：`AppConfig` 用构造器手动装配所有 Bean，没有用 `@Service`/`@Repository` 注解扫描。我自己写的时候，觉得还是很清晰（其实是一开始不知道 Spring Boot 还有自动装配这种东西），但确实不够惯用，且改动后需要重启服务。后续可考虑改为注解式自动装配。
2. **会话存在内存**：`AuthService` 的 token 存内存 Map，后端重启即失效，确实不方便多实例/迁移等等。
3. **单例 Connection**：`AppConfig` 注入的是单个 SQLite `Connection`，多线程并发写可能产生锁竞争（目前还没有遇到）。后续可改为连接池或每次操作打开新连接。
5. **角色卡用 JSON 列存储**：`card_json` 单列存整张卡，扩展规则模板很灵活；代价是无法用 SQL 直接查询卡内字段。目前没想到特别好的方案。
6. **暗骰可见性**：当前暗骰仅掷骰者自己可见，尚未实现“仅 GM 可见”，但由于房间简化了初始写的GM/房主/用户/游客的机制，schema 的 `visibility/receiver_id` 已为此预留空间。但是还没有实现。
7. **轮询机制**：`RoomView.vue` 每 3 秒轮询消息，没用websocket，还没学明白🫡，可能后面可以改进。
8. **尚无自动化测试**：仓库无 JUnit 用例，目前是手动测试为主。

## 8. 部署与环境变量

他人克隆后无需改代码，仅靠环境变量即可启动：

| 变量 | 默认值 | 作用 |
| --- | --- | --- |
| `DICE_ROOM_SITE` | `localhost` | Caddy 站点域名 + 后端 CORS 白名单（如 `dice.tz.kg`） |
| `DICE_ROOM_BACKEND` | `127.0.0.1:8080` | Caddy 反代的后端地址 |
| `DICE_ROOM_WEBROOT` | `src/frontend/dist` | Caddy 静态资源根目录 |
| `VITE_API_TARGET` | `http://localhost:8080` | 前端开发期代理目标 |
| `DICE_ROOM_ADMIN_LOGIN_NAME` / `_PASSWORD` / `_NICKNAME` | — | `AdminInitialize.initializeFromEnv()` 读取的首管理员凭证 |

一键启动：

```bash
DICE_ROOM_SITE=example.com ./run.sh
```

## AI usage
本项目后端基本上是我自己写的，后端的 REST API 设计、数据库 schema、掷骰算法、backend、database、repository、service 等都是我独立完成的，没有直接使用 AI 生成的代码。

不过对于playercard的内容，以及一些重复的setter和getter，后端我都使用了copilot tab补全。

功能设计上，我一开始和codex进行了比较详细的讨论，比如五个模块的解耦、REST API 的设计、数据库 schema 的设计等，后续在实现过程中也会就一些细节问题和codex进行讨论，寻求一些建议和思路。

总的来说，后端的核心逻辑和设计都是我自己完成的，AI主要在一些重复性的代码生成和细节实现上提供了帮助。

在前端上，由于我前端经验较少，上课所说的前端设计并不能有效地覆盖我的需求，且美化工作量大，而本次需求大量的交互设计，我以往的astro经验不能使用，然后AI推荐我使用了 Vue + Axios + Pinia + Element Plus，但是 Vue 的学习曲线又比较陡峭，所以我在前端的开发过程中大量依赖了 AI 的帮助。前端的页面结构、组件设计、状态管理、API 调用等都是在和 AI 的交互中逐步完善的。尤其是在实现一些复杂的交互逻辑和 UI 细节时，AI 提供了很多有用的建议和代码示例，帮助我更快地实现了功能。

但是由于后端 rest API 是我本人设计的，所以前端对接上还算比较顺利，主要的挑战还是在于和 AI 对齐需求上。

所以大致上可以说，逻辑和后端设计是我自己完成的，前端实现上 AI 提供了大量帮助，尤其是在一些细节和交互逻辑的实现上。






