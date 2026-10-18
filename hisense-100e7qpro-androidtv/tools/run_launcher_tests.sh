#!/usr/bin/env bash
# Serves vidaa-launcher/ locally and drives it with headless Chromium (Playwright),
# simulating the remote's arrow / OK / Back keys at 1920x1080 and 1280x720.
# Screenshots land in the directory given as $1 (default: a temp dir).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${1:-$(mktemp -d)}"
python3 -m http.server 8765 --bind 127.0.0.1 --directory "$ROOT/vidaa-launcher" >/dev/null 2>&1 &
SERVER=$!
trap 'kill $SERVER' EXIT
sleep 1
NODE_PATH="$(npm root -g)" node "$ROOT/tools/test_launcher.js" "$OUT"
echo "screenshots: $OUT"
