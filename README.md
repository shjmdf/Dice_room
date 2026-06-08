# Dice_room

## 本地开发

后端：

```bash
mvn exec:java
```

前端开发服务器：

```bash
cd src/frontend
npm run dev
```

默认端口：

- 后端：`http://localhost:8080`
- 前端开发：`http://localhost:5173`

## Caddy 部署方式

Caddy 用来对外提供一个统一入口：

- 静态前端：`src/frontend/dist`
- 后端接口：`/api/*` 反向代理到 `127.0.0.1:8080`

先构建前端：

```bash
cd src/frontend
npm run build
```

启动后端：

```bash
cd /root/dice_room
mvn exec:java
```

启动 Caddy：

```bash
cd /root/dice_room
caddy run
```

如果要绑定域名：

```bash
DICE_ROOM_SITE=your-domain.com caddy run
```

如果后端不是 `8080`：

```bash
DICE_ROOM_BACKEND=127.0.0.1:8081 caddy run
```

如果项目不在 `/root/dice_room`：

```bash
DICE_ROOM_WEBROOT=/path/to/dice_room/src/frontend/dist caddy run
```

创建服务
新建：
```bash 
nano /etc/systemd/system/dice-room.service
```
写入：
```bash
[Unit]
Description=Dice Room
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
WorkingDirectory=/root/dice_room
Environment=DICE_ROOM_SITE=dice.tz.kg
ExecStart=/root/dice_room/run.sh
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```
然后执行：
```bash
systemctl daemon-reload
systemctl enable dice-room
systemctl start dice-room
```
查看状态：
```bash
systemctl status dice-room
```
看日志：
```bash
journalctl -u dice-room -f
```
停止但不删除
```bash
systemctl stop dice-room
```
取消开机自启
```bash
systemctl disable dice-room
```
彻底删除这个持久化服务
```bash
systemctl stop dice-room
systemctl disable dice-room
rm /etc/systemd/system/dice-room.service
systemctl daemon-reload
systemctl reset-failed
```