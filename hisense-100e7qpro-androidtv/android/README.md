# android/ — OEM Compatibility Layer (OCL) implementation

Real, compiling Android code for the **Hybrid (Architecture B)** path: an Android TV
`TvInputService` that exposes the Hisense panel's own sources (HDMI 1–4, tuner, AV…)
inside Android TV, driven through the TV's local OEM control plane.

This is the concrete form of
[`../architecture/oem-compatibility.md`](../architecture/oem-compatibility.md). It is
the part of the port that can be built and tested **without touching the TV firmware**.

## What it does
- Connects to the TV's local broker (MQTT/TLS, TCP 36669 — SOURCES.md [S-40]).
- One-time PIN pairing from a Leanback setup screen.
- Imports the TV's **source list** as Android TV channels (`ChannelSync`).
- Tuning a source-channel asks the panel to switch input (`OclTvInputService`).
- Forwards volume / channel / power keys to the panel while a source is on screen.

So from the Android TV launcher the user selects "HDMI 1" (etc.) like any other
channel and the real panel switches to it — satisfying success criteria #1, #3, #4
and part of #6 **in the hybrid model, with VIDAA still driving the glass**.

## Layout
```
packages/apps/HisenseOcl/
  src/.../mqtt/MiniMqttClient.java   dependency-free MQTT 3.1.1 (publish/subscribe)
  src/.../vidaa/                     VIDAA topic map + MQTT backend (TvControlBackend)
  src/.../tv/                        OclTvInputService, ChannelSync, OclConnection, RemoteKeys
  src/.../setup/SetupActivity.java   pair + import sources (Leanback)
  test/.../VidaaBackendTest.java     plain-JVM test vs a scripted fake broker
  Android.bp, AndroidManifest.xml, res/
device/hisense/e7qpro/               AOSP product stub (see below) — NOT a flashable image
```

## Design choice: backend is swappable
`TvControlBackend` is the seam. Today the only implementation is
`VidaaMqttBackend` (VIDAA owns the panel; we send it commands). A future **native**
port would add a backend sitting on the MediaTek TV Input / audio HALs, and
`OclTvInputService` / `ChannelSync` / the setup UI would not change. This is what
lets the same app serve the Hybrid path now and a native path later.

## Verification done (no device, no AOSP SDK needed)
- **JVM protocol test:** `../tools/run_android_tests.sh` compiles the MQTT client +
  VIDAA backend and runs them against a scripted fake broker — asserts the exact
  bytes (CONNECT with the default creds, SUBSCRIBE, source-list request topic, QoS-1
  reply delivery + PUBACK, change-source payload). **Passes.**
- **Framework type-check:** compiled against the real Android 14 framework
  (`org.robolectric:android-all:14`) with `javac -Xlint:all`; **0 warnings from
  project sources**, exit 0. See `../tools/check_android_build.sh`.

## What is NOT verified
- Nothing has run on the TV. The MQTT credentials/port/topics are the community
  values ([S-40]) and are **UNVERIFIED on the 100E7Q PRO** — a firmware change can
  move them. Use `../tools/vidaa_probe.py` (read-only) on the real unit first.
- `MiniMqttClient` covers only what this app needs (QoS 0 publish, QoS 0/1 receive);
  it is not a general MQTT library.

## Build
- In an AOSP tree: drop `packages/apps/HisenseOcl` in place and `m HisenseOcl`
  (or build the APK with Gradle + an Android SDK — not available in this
  environment, so the committed proof is the JVM test + framework type-check).
