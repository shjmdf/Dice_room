#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$PROJECT_DIR/src/frontend"
BACKEND_LOG="$PROJECT_DIR/target/dice-room-backend.log"
ENV_FILE="$PROJECT_DIR/.env"

if [[ -f "$ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
fi

export DICE_ROOM_SITE="${DICE_ROOM_SITE:-http://localhost:8088}"
export DICE_ROOM_SERVER_ADDRESS="${DICE_ROOM_SERVER_ADDRESS:-127.0.0.1}"
export DICE_ROOM_SERVER_PORT="${DICE_ROOM_SERVER_PORT:-8080}"
export DICE_ROOM_BACKEND="${DICE_ROOM_BACKEND:-$DICE_ROOM_SERVER_ADDRESS:$DICE_ROOM_SERVER_PORT}"
export DICE_ROOM_WEBROOT="${DICE_ROOM_WEBROOT:-$FRONTEND_DIR/dist}"

BACKEND_PID=""

stop_project_listener() {
    local port="$1"
    local pid
    local cwd

    if ! command -v lsof >/dev/null 2>&1; then
        return
    fi

    while read -r pid; do
        if [[ -z "$pid" ]]; then
            continue
        fi
        cwd="$(readlink "/proc/$pid/cwd" 2>/dev/null || true)"
        if [[ "$cwd" == "$PROJECT_DIR"* ]]; then
            kill "$pid" 2>/dev/null || true
        fi
    done < <(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
}

cleanup() {
    if [[ -n "$BACKEND_PID" ]] && kill -0 "$BACKEND_PID" 2>/dev/null; then
        kill "$BACKEND_PID" 2>/dev/null || true
    fi
    stop_project_listener "$DICE_ROOM_SERVER_PORT"
}
trap cleanup EXIT INT TERM

require_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "缺少命令：$1"
        exit 1
    fi
}

require_command npm
require_command mvn
require_command caddy

cd "$FRONTEND_DIR"
if [[ ! -d node_modules ]]; then
    if [[ -f package-lock.json ]]; then
        npm ci
    else
        npm install
    fi
fi
npm run build

cd "$PROJECT_DIR"
mkdir -p target
mvn -Dserver.address="$DICE_ROOM_SERVER_ADDRESS" -Dserver.port="$DICE_ROOM_SERVER_PORT" compile exec:java > "$BACKEND_LOG" 2>&1 &
BACKEND_PID="$!"

echo "后端启动中，日志：$BACKEND_LOG"
sleep 3

if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo "后端启动失败，请查看日志：$BACKEND_LOG"
    exit 1
fi

echo "Caddy site: $DICE_ROOM_SITE"
echo "Backend listen: $DICE_ROOM_SERVER_ADDRESS:$DICE_ROOM_SERVER_PORT"
echo "Caddy backend: $DICE_ROOM_BACKEND"
echo "Caddy webroot: $DICE_ROOM_WEBROOT"
caddy run --config "$PROJECT_DIR/Caddyfile"
