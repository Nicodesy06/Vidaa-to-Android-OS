# Android TV / AOSP Compatibility Assessment

> How close can an AOSP-based Android TV get to running on this MT9618 board, and
> what must be supplied. Status tags per [`SOURCES.md`](../SOURCES.md).

## 1. The decisive fact and its boundary

- **Android TV demonstrably runs on MT9618 silicon** — Hisense ships MT9618 under
  Android (U8N: build STT2.230310.002, kernel 5.4.286, [S-30]); TCL ships an MT9618
  Android-U kernel 5.15.180 ([S-22]). **Silicon-level Android support: VERIFIED.**
- **But AOSP ≠ a working TV.** The parts that make a TV a TV on MediaTek are in a
  **proprietary BSP/HAL layer** delivered to licensees under NDA: panel/T-CON driver,
  backlight & local-dimming, MEMC, HDR/Dolby-Vision tone-mapping, the video decoder
  HAL, the **TV Input HAL** (HDMI inputs + tuner), CEC, audio DSP/passthrough, and
  the DRM trusted apps. Generic AOSP contains **none** of these. **Status: this gap
  is the project's core engineering problem.**

## 2. AOSP layer-by-layer compatibility matrix

| Layer | Needed for TV | Can AOSP/open supply it? | Status |
|-------|---------------|--------------------------|--------|
| Bootloader (LK/UEFI) | boot the kernel | No — board-signed, keys held by OEM | BLOCKED ([S-33]) |
| Kernel + core SoC drivers | CPU/GPU/mem/display pipe | Partly — Hisense GPL ([S-21]) / TCL ([S-22]) kernels exist | PLAUSIBLE |
| GKI/ABI + vendor modules | Android kernel contract | Must be reconciled; MTK vendor modules are partly binary | UNVERIFIED |
| Gralloc / Mali GPU HAL | compositing | Mali userspace blobs exist for Android; need the matching version | PLAUSIBLE |
| **TV Input HAL** (HDMI in + tuner) | HDMI switching, live TV | **No open impl for MT9618** — must adapt vendor TIS | BLOCKED |
| HDMI-CEC HAL | one-remote control | AOSP has the framework; needs the SoC CEC driver bound | UNVERIFIED |
| Audio HAL (incl. ARC/eARC, Atmos passthrough) | sound | AOSP generic won't drive the TV DSP/eARC | BLOCKED |
| Video decode (OMX/Codec2) + HDR/DV | playback, HDR | MTK Codec2 + DV TA are proprietary | BLOCKED |
| Panel/MEMC/local-dimming middleware | picture quality | Proprietary, VIDAA-linked | BLOCKED |
| DRM (Widevine L1, PlayReady) | premium streaming | Keybox provisioned per-device; not portable | BLOCKED (see §5) |
| SELinux policy | boot at all under enforcing | Must be authored for the board | UNVERIFIED |

**Reading of the matrix:** the CPU/GPU/kernel/compositor row is **PLAUSIBLY**
bringable with open + available blobs. Everything that makes it a *television*
(TV Input HAL, audio/eARC, HDR/DV, panel middleware) is **BLOCKED** on proprietary
MediaTek+Hisense components that are not published and are built against a specific
userland. This is why a pure-AOSP replacement scores low — see
[`architecture/native-port.md`](../architecture/native-port.md).

## 3. Which AOSP target version?

- Pick the **Android TV version whose expected kernel matches the SoC's kernel** to
  minimise driver back/forward-porting:
  - If the board is kernel **5.4.x** (as the Android cousin, [S-30]) → **Android 11/12
    TV** era aligns best.
  - If **5.15.x** (TCL's tree, [S-22]) → **Android 13/14 TV**.
- The E7Q Pro kernel is **UNVERIFIED** (action item S-21). **Do not choose a target
  until the kernel version is known** — choosing wrong doubles the HAL/ABI work.
- "Modern and stable" (user's preference) realistically means **Android 13/14 TV**
  *iff* a 5.15 kernel is viable; otherwise an 11/12-era build is the pragmatic target.

## 4. The realistic sourcing strategy for the blocked HALs

There is no open substitute for the MT9618 TV HALs. The only plausible legitimate
sources, in priority order:
1. **Hisense GPL kernel ([S-21])** — gives the *kernel-side* drivers (display pipe,
   CEC, some I²C/panel glue). Legitimate and in-hand.
2. **A matching Hisense/TCL *Android* MT9618 BSP** — would provide Android-ABI HALs,
   but these are **NDA/proprietary**; using another model's vendor image is exactly
   the "flash similar firmware" step the project forbids without proven board
   compatibility. **Status: BLOCKED / do-not-assume.**
3. **A VIDAA→Android shim** (Hybrid, arch B): keep the VIDAA kernel+drivers and
   expose them to an Android userland via adapters — avoids needing Android-ABI TV
   HALs at all. Most promising; see [`architecture/hybrid-port.md`](../architecture/hybrid-port.md).

## 5. Google services / DRM — assess separately (per the brief)

- **AOSP Android TV ≠ Google-certified Android TV.** Without Google's license you get
  AOSP TV (sideloaded APKs, Leanback launcher) but **no Play Store, no Play
  certification, no "works with Google" entitlements**. This is a business/licensing
  gate, not solvable technically. **Status: BLOCKED by policy.**
- **Widevine L1 / PlayReady:** the device's keybox is provisioned into the original
  firmware/TEE and is **not portable** to a custom build; best case after a port is
  **Widevine L3 (software)** → SD-only on Netflix/Disney+/etc. **Status: L1 BLOCKED,
  L3 PLAUSIBLE.**
- **Chromecast built-in:** requires Google licensing → **BLOCKED** on AOSP.
- Consequence for the user's success criteria #2 ("install compatible APKs"):
  achievable for **sideloaded** APKs; **not** for Play-delivered or L1-DRM apps.

## 6. Bring-up order if a native/hybrid path opens (and only then)

1. Kernel + console boot (emulated/serial) → 2. Display/compositor (SurfaceFlinger on
Mali) → 3. Input (IR/CEC remote) → 4. Audio HAL → 5. **TV Input HAL (HDMI + tuner)** →
6. Video decode + HDR → 7. Panel middleware/calibration → 8. Networking/BT →
9. Standby/power. Each step gated by the OEM-feature matrix in
[`compatibility/oem-feature-matrix.md`](../compatibility/oem-feature-matrix.md).
