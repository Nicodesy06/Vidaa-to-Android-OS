#!/usr/bin/env bash
# Compiles the platform-independent part of HisenseOcl (MQTT client + VIDAA backend)
# and runs its plain-JVM tests. Needs only a JDK; no Android SDK.
set -euo pipefail
APP="$(cd "$(dirname "$0")/../android/packages/apps/HisenseOcl" && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" \
  "$APP"/src/it/nicodesy/hisenseocl/mqtt/*.java \
  "$APP"/src/it/nicodesy/hisenseocl/vidaa/*.java \
  "$APP"/test/it/nicodesy/hisenseocl/mqtt/*.java
java -cp "$OUT" it.nicodesy.hisenseocl.mqtt.VidaaBackendTest
