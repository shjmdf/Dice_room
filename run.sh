#!/usr/bin/env bash
export DICE_ROOM_SITE="${DICE_ROOM_SITE:-localhost}"

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$PROJECT_DIR/src/frontend"
BACKEND_LOG="$PROJECT_DIR/target/dice-room-backend.log"

export DICE_ROOM_BACKEND="${DICE_ROOM_BACKEND:-127.0.0.1:8080}"
export DICE_ROOM_WEBROOT="${DICE_ROOM_WEBROOT:-$FRONTEND_DIR/dist}"

BACKEND_PID=""

cleanup() {
    if [[ -n "$BACKEND_PID" ]] && kill -0 "$BACKEND_PID" 2>/dev/null; then
        kill "$BACKEND_PID"
    fi
}
trap cleanup EXIT

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
npm run build

cd "$PROJECT_DIR"
mkdir -p target
mvn exec:java > "$BACKEND_LOG" 2>&1 &
BACKEND_PID="$!"

echo "后端启动中，日志：$BACKEND_LOG"
sleep 3

if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo "后端启动失败，请查看日志：$BACKEND_LOG"
    exit 1
fi

echo "Caddy site: $DICE_ROOM_SITE"
echo "Caddy backend: $DICE_ROOM_BACKEND"
echo "Caddy webroot: $DICE_ROOM_WEBROOT"
caddy run --config "$PROJECT_DIR/Caddyfile"
