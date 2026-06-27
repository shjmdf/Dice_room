# Dice Room
**⚠️本Readme由Chatgpt 5.5 High 生成，不保证与代码完全对应**
Dice Room 是一个面向桌面跑团场景的掷骰房间应用。它包含 Java/Spring Boot 后端、SQLite 数据库、Vue 3 前端，以及可选的 Caddy 生产部署入口。

## 功能概览

- 用户注册、登录、个人资料和密码管理
- 首个管理员初始化，管理员管理用户、房间和邀请码
- 邀请码注册机制
- 房间创建、加入、成员管理、标签、公开消息和私聊
- COC 风格掷骰、技能检定、SAN 检定
- 玩家角色卡创建、编辑、绑定到房间成员

## 技术栈

- 后端：Java 17、Spring Boot Web 3.5.2、SQLite JDBC 3.53.1.0、BCrypt 0.10.2
- 前端：Node.js、npm、Vite 6、Vue 3、Vue Router、Pinia、Axios、Element Plus
- 数据库：SQLite
- 生产入口：Caddy，可提供静态前端、HTTPS、`/api/*` 反向代理

## 目录结构

```text
.
├── Caddyfile                  # Caddy 生产入口配置
├── configure.sh               # 交互式配置脚本
├── pom.xml                    # 后端 Maven 配置
├── run.sh                     # 构建前端、启动后端和 Caddy
└── src/
    ├── app/                   # Spring Boot 启动和 Bean 配置
    ├── backend/               # 领域模型与掷骰/角色卡逻辑
    ├── controller/            # REST API
    ├── database/schema.sql    # SQLite 建表脚本
    ├── frontend/              # Vue 前端
    ├── repository/            # SQLite 访问层
    └── service/               # 业务服务
```

## 依赖准备

后端开发和运行需要：

- JDK 17
- Maven 3.8+，或其他可运行本项目 `pom.xml` 的 Maven 版本

前端开发和构建需要：

- Node.js，建议 18+
- npm

生产部署如果使用本仓库的 `Caddyfile` 和 `run.sh`，还需要：

- Caddy 2
- 一个解析到服务器的域名，如果需要公网 HTTPS

## 快速配置

推荐先运行交互式配置脚本：

```bash
./configure.sh
```

脚本会询问：

- Caddy 站点地址，本地默认是 `http://localhost:8088`，真实部署可填写自己的域名
- 后端监听地址和端口
- Caddy 反向代理目标
- 前端构建产物目录
- 前端开发服务器监听地址和端口
- 前端开发期 API 代理目标

配置完成后，生产运行使用：

```bash
./run.sh
```

## 本地开发

如果已经运行过 `./configure.sh`，可以直接按生成的本地配置启动。

启动后端：

```bash
source .env
mvn -Dserver.address="$DICE_ROOM_SERVER_ADDRESS" -Dserver.port="$DICE_ROOM_SERVER_PORT" exec:java
```

启动前端开发服务器：

```bash
cd src/frontend
npm install
npm run dev
```

如果不使用交互式配置，也可以直接指定端口：

```bash
mvn -Dserver.address=127.0.0.1 -Dserver.port=8081 exec:java
```

```bash
cd src/frontend
DICE_ROOM_FRONTEND_PORT=5174 VITE_API_TARGET=http://localhost:8081 npm run dev
```

前端开发服务器请求相对路径 `/api`，由 Vite 代理到后端。

## 生产部署

生产推荐拓扑：

```text
浏览器
  │
  │ https://your-domain.com
  ▼
Caddy :80/:443
  ├─ 静态前端：src/frontend/dist
  └─ /api/* -> Spring Boot 127.0.0.1:8080
       └─ SQLite
```

先确认域名 DNS 已解析到服务器，然后执行：

```bash
./configure.sh
./run.sh
```

也可以不用交互式脚本，直接用环境变量运行：

```bash
DICE_ROOM_SITE=your-domain.com \
DICE_ROOM_SERVER_ADDRESS=127.0.0.1 \
DICE_ROOM_SERVER_PORT=8080 \
./run.sh
```

`run.sh` 会执行三件事：

1. 在 `src/frontend` 下安装缺失的前端依赖并运行 `npm run build`
2. 在项目根目录后台启动后端
3. 使用 `Caddyfile` 启动 Caddy

Caddy 会使用 `DICE_ROOM_SITE` 作为站点名。使用真实域名时，Caddy 会自动申请和续期 HTTPS 证书。

## 配置项

| 变量 | 默认值 | 作用 |
| --- | --- | --- |
| `DICE_ROOM_SITE` | `http://localhost:8088` | Caddy 站点地址，同时用于后端 CORS 白名单；真实部署可填自己的域名 |
| `DICE_ROOM_SERVER_ADDRESS` | `127.0.0.1` | 后端监听地址 |
| `DICE_ROOM_SERVER_PORT` | `8080` | 后端监听端口 |
| `DICE_ROOM_BACKEND` | `${DICE_ROOM_SERVER_ADDRESS}:${DICE_ROOM_SERVER_PORT}` | Caddy 将 `/api/*` 反向代理到的后端地址 |
| `DICE_ROOM_WEBROOT` | `src/frontend/dist` | Caddy 提供的前端静态文件目录 |
| `DICE_ROOM_FRONTEND_HOST` | `0.0.0.0` | 前端开发服务器监听地址 |
| `DICE_ROOM_FRONTEND_PORT` | `5173` | 前端开发服务器端口 |
| `VITE_API_TARGET` | `http://localhost:${DICE_ROOM_SERVER_PORT}` | 前端开发服务器的 `/api` 代理目标 |

## 管理员初始化

管理员不是写死的。

首次部署时，打开登录页里的“初始化管理员”，或直接调用：

```http
POST /api/users/admin/initialize
Content-Type: application/json

{
  "loginName": "admin",
  "password": "change-me",
  "nickname": "Admin"
}
```

后端会检查数据库中是否已经存在管理员：

- 没有管理员：创建首个管理员
- 已有管理员：拒绝再次初始化

后续普通用户需要邀请码注册。管理员登录后可以在管理后台创建邀请码。

## 域名和端口关系

前端使用相对路径 `/api`，所以部署到自己的域名时通常不需要重新改代码或重新配置 API 域名。

生产环境需要保持这几个关系一致：

- `DICE_ROOM_SITE=your-domain.com`
- 用户访问 `https://your-domain.com`
- Caddy 匹配 `your-domain.com`
- Caddy 将 `/api/*` 转发到 `DICE_ROOM_BACKEND`
- 后端 CORS 允许 `https://your-domain.com`

默认建议让后端监听本机地址，由 Caddy 对公网暴露 `80/443`。

## systemd 示例

如果要作为 Linux 服务运行，可以创建：

```ini
[Unit]
Description=Dice Room
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
WorkingDirectory=/opt/dice_room
Environment=DICE_ROOM_SITE=your-domain.com
Environment=DICE_ROOM_SERVER_ADDRESS=127.0.0.1
Environment=DICE_ROOM_SERVER_PORT=8080
ExecStart=/opt/dice_room/run.sh
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

然后执行：

```bash
sudo systemctl daemon-reload
sudo systemctl enable dice-room
sudo systemctl start dice-room
sudo systemctl status dice-room
```

查看日志：

```bash
sudo journalctl -u dice-room -f
```

## 停止和清理

查看会执行哪些停止和清理动作：

```bash
./stop_and_clean.sh
```

实际停止 Dice Room，并清理本项目的前端依赖和构建产物：

```bash
./stop_and_clean.sh --apply
```

同时移除本地运行数据和本地配置：

```bash
./stop_and_clean.sh --apply --remove-data --remove-local-config
```

如果使用了 systemd 服务，可以同时停用并移除服务文件：

```bash
sudo ./stop_and_clean.sh --apply --remove-systemd
```

如果确实要卸载系统级依赖，可以显式加上：

```bash
sudo ./stop_and_clean.sh --apply --remove-system-packages
```

系统级依赖可能被其他项目共用，所以脚本不会默认卸载 Java、Maven、Node.js、npm 或 Caddy。

## 许可证

本仓库目前尚未声明开源许可证。
