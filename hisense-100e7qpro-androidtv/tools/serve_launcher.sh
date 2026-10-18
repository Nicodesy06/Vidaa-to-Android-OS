#!/usr/bin/env bash
# Serves the launcher on your home network so the TV can load it.
# Run on any always-on machine (PC, NAS, Raspberry Pi) in the same LAN as the TV,
# then install http://<this-machine-ip>:8080/ on the TV (see vidaa-launcher/README.md).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PORT="${1:-8080}"
echo "Launcher su http://$(hostname -I 2>/dev/null | awk '{print $1}'):$PORT/  (Ctrl+C per fermare)"
exec python3 -m http.server "$PORT" --directory "$ROOT/vidaa-launcher"
