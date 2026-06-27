#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_DIR/.env"
DRY_RUN=1
REMOVE_DATA=0
REMOVE_LOCAL_CONFIG=0
REMOVE_SYSTEMD=0
REMOVE_SYSTEM_PACKAGES=0

if [[ -f "$ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
fi

SERVICE_NAME="${DICE_ROOM_SERVICE_NAME:-dice-room}"

usage() {
    cat <<EOF
Usage: ./stop_and_clean.sh [options]

Stops Dice Room processes/services and removes local project dependencies.

Safe default:
  ./stop_and_clean.sh
      Dry-run only. Prints what would be done.

Options:
  --apply
      Actually run the stop and cleanup commands.

  --remove-data
      Also remove runtime data: dice_room.db and local Caddy data.

  --remove-local-config
      Also remove generated local config: .env and src/frontend/.env.local.

  --remove-systemd
      Disable and remove /etc/systemd/system/${SERVICE_NAME}.service.

  --remove-system-packages
      Try to uninstall caddy, maven, nodejs, npm, and openjdk packages.
      This is intentionally opt-in because these packages may be shared.

  -h, --help
      Show this help.

Examples:
  ./stop_and_clean.sh
  ./stop_and_clean.sh --apply
  ./stop_and_clean.sh --apply --remove-data --remove-local-config
  ./stop_and_clean.sh --apply --remove-systemd
EOF
}

run() {
    if [[ "$DRY_RUN" -eq 1 ]]; then
        printf '[dry-run] %q' "$1"
        shift
        for arg in "$@"; do
            printf ' %q' "$arg"
        done
        printf '\n'
    else
        "$@"
    fi
}

run_may_fail() {
    if [[ "$DRY_RUN" -eq 1 ]]; then
        run "$@"
        return 0
    fi

    "$@" || true
}

remove_path() {
    local path="$1"
    if [[ -e "$path" || -L "$path" ]]; then
        run rm -rf "$path"
    fi
}

stop_project_listener() {
    local port="$1"
    local pid
    local cwd

    if ! command_exists lsof; then
        return
    fi

    while read -r pid; do
        if [[ -z "$pid" ]]; then
            continue
        fi
        cwd="$(readlink "/proc/$pid/cwd" 2>/dev/null || true)"
        if [[ "$cwd" == "$PROJECT_DIR"* ]]; then
            run_may_fail kill "$pid"
        fi
    done < <(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

while [[ "$#" -gt 0 ]]; do
    case "$1" in
        --apply)
            DRY_RUN=0
            ;;
        --remove-data)
            REMOVE_DATA=1
            ;;
        --remove-local-config)
            REMOVE_LOCAL_CONFIG=1
            ;;
        --remove-systemd)
            REMOVE_SYSTEMD=1
            ;;
        --remove-system-packages)
            REMOVE_SYSTEM_PACKAGES=1
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
    shift
done

if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "Dry-run mode. Re-run with --apply to make changes."
else
    echo "Apply mode. Stopping and cleaning Dice Room."
fi

if command_exists systemctl; then
    run_may_fail systemctl stop "$SERVICE_NAME"
fi

if command_exists pkill; then
    run_may_fail pkill -f "$PROJECT_DIR/run.sh"
    run_may_fail pkill -f "app.DiceRoomApplication"
    run_may_fail pkill -f "caddy run --config $PROJECT_DIR/Caddyfile"
fi

stop_project_listener "${DICE_ROOM_SERVER_PORT:-8080}"

remove_path "$PROJECT_DIR/src/frontend/node_modules"
remove_path "$PROJECT_DIR/src/frontend/dist"
remove_path "$PROJECT_DIR/src/frontend/.vite"
remove_path "$PROJECT_DIR/target"
remove_path "$PROJECT_DIR/build"
remove_path "$PROJECT_DIR/out"

if [[ "$REMOVE_LOCAL_CONFIG" -eq 1 ]]; then
    remove_path "$PROJECT_DIR/.env"
    remove_path "$PROJECT_DIR/src/frontend/.env.local"
fi

if [[ "$REMOVE_DATA" -eq 1 ]]; then
    remove_path "$PROJECT_DIR/dice_room.db"
    remove_path "$PROJECT_DIR/dice_room.db-journal"
    remove_path "$PROJECT_DIR/dice_room.db-wal"
    remove_path "$PROJECT_DIR/dice_room.db-shm"
    remove_path "$PROJECT_DIR/caddy"
fi

if [[ "$REMOVE_SYSTEMD" -eq 1 && -d /etc/systemd/system ]]; then
    run_may_fail systemctl stop "$SERVICE_NAME"
    run_may_fail systemctl disable "$SERVICE_NAME"
    run rm -f "/etc/systemd/system/${SERVICE_NAME}.service"
    run_may_fail systemctl daemon-reload
    run_may_fail systemctl reset-failed
fi

if [[ "$REMOVE_SYSTEM_PACKAGES" -eq 1 ]]; then
    if command_exists apt-get; then
        run apt-get remove -y caddy maven nodejs npm openjdk-17-jdk openjdk-17-jre
        run apt-get autoremove -y
    elif command_exists dnf; then
        run dnf remove -y caddy maven nodejs npm java-17-openjdk java-17-openjdk-devel
    elif command_exists yum; then
        run yum remove -y caddy maven nodejs npm java-17-openjdk java-17-openjdk-devel
    else
        echo "No supported package manager found for system package removal." >&2
    fi
fi

echo "Done."
