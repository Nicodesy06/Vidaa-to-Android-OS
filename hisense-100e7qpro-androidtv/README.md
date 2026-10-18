# Hisense 100E7Q PRO — Native Android TV Port & OEM Feature Preservation

Engineering research project: investigate a **real, evidence-based path** to running
Android TV on the Hisense 100E7Q PRO (EU) while preserving the original Hisense
functions (HDMI sources, tuner, audio, remote, panel/HDR). Every claim is tagged
**VERIFIED / PLAUSIBLE / UNVERIFIED / BLOCKED** and backed by a source in
[`SOURCES.md`](SOURCES.md). Nothing here has been run against the physical TV; all
findings are from public sources and GPL releases.

> ⚠️ **Hardware-safety first.** This phase is research + read-only diagnostics only.
> No flashing, no bootloader changes, no partition writes. See
> [`docs/recovery-plan.md`](docs/recovery-plan.md).

## TL;DR verdict (as of 2026-10-18)

| Question | Answer | Confidence |
|----------|--------|------------|
| Does Android TV run on this SoC (MT9618/Pentonic 700)? | **Yes** — Hisense/TCL ship MT9618 under Android | **VERIFIED (silicon-level)** [S-30][S-22] |
| Is the EU 100E7Q PRO Android or VIDAA? | **VIDAA U8.5** (Linux, not Android) on MT9618 | PLAUSIBLE [S-02] |
| Can we get a **full-feature pure-AOSP** replacement? | **No, not through legitimate open channels** — TV HALs (HDMI-in/tuner), HDR/DV, Widevine L1, Play are proprietary/licensed | **BLOCKED** [S-33, android-compat §2/§5] |
| Can we preserve HDMI switching / remote / volume / power **now, risk-free**? | **Yes** — via the OEM MQTT control plane (port 36669) | **VERIFIED (API)** [S-40] |
| Is a native/dual-boot flash even possible? | **Unknown** — bootloader likely signed; must be *measured*, not assumed | CONDITIONAL [S-33] |

**Recommended path: Architecture B (Hybrid), starting with the zero-risk B0 control
bridge**, while non-invasively measuring the boot chain to see if deeper options ever
open. A full native replacement (Architecture A) and dual-boot (C) are currently
**blocked** on proprietary HALs + an unmeasured, probably-locked bootloader — not
declared impossible, but not feasible on present evidence.

## The core insight
The MT9618 is a **dual-personality** TV SoC: the *same silicon* ships as Android TV
(Hisense U8N, TCL) **and** as VIDAA (this TV). So the hard question is not "can the
chip run Android" (it can) but "can we obtain the **board-specific TV HALs and an
unlocked boot chain** for *this* VIDAA unit" — and those are the real blockers.

## Repository map
```
research/   hardware · firmware · github-findings · bootloader · android-compatibility
architecture/ native-port(A) · hybrid-port(B, recommended) · dual-boot(C) · oem-compatibility(OCL)
compatibility/ oem-feature-matrix   ← per-feature preservability A/B/C
android/    HisenseOcl               ← OCL implemented: Android TV TvInputService (Hybrid B)
vidaa-launcher/                      ← Apple-TV-style launcher, runs as a VIDAA web app
tools/      vidaa_probe.py (READ-ONLY) · run_android_tests.sh · check_android_build.sh
            serve_launcher.sh · run_launcher_tests.sh
poc/        emulation-first PoCs     ← nothing touches the TV flash
docs/       roadmap · blockers · recovery-plan
SOURCES.md  every source, with status + limitations
```

## What exists as working code (not just docs)
The Hybrid (B) OEM Compatibility Layer is **implemented and tested off-device** in
[`android/`](android/): an Android TV `TvInputService` that imports the panel's
sources (HDMI 1–4 / tuner) as channels and switches inputs + forwards remote keys
over the TV's MQTT control plane, with a dependency-free MQTT client and a
backend seam for a future native-HAL path. Verified by a JVM protocol test
(`tools/run_android_tests.sh`) and a real Android-14 framework type-check
(`tools/check_android_build.sh`). Not yet run on the TV — the control-plane
credentials are UNVERIFIED on the E7Q Pro, so probe read-only first.

## How to read this project
1. [`SOURCES.md`](SOURCES.md) — the evidence base.
2. [`research/hardware.md`](research/hardware.md) — what the device is (and what's still BLOCKED).
3. [`research/android-compatibility.md`](research/android-compatibility.md) — the AOSP gap analysis.
4. [`architecture/hybrid-port.md`](architecture/hybrid-port.md) — the recommended route.
5. [`compatibility/oem-feature-matrix.md`](compatibility/oem-feature-matrix.md) — feature-by-feature.
6. [`docs/roadmap.md`](docs/roadmap.md) + [`docs/blockers.md`](docs/blockers.md) + [`docs/recovery-plan.md`](docs/recovery-plan.md).

## Cleaning up VIDAA instead of replacing it
If the goal is a cleaner TV rather than Android: [`docs/clean-vidaa.md`](docs/clean-vidaa.md)
covers privacy/ad settings and network-level ad blocking, and
[`vidaa-launcher/`](vidaa-launcher/) is an Apple-TV-style launcher installable as a
VIDAA web app (tested in headless Chromium, not yet on the TV). Uninstalling system
apps or replacing the boot home screen is not possible without modifying firmware.

## What you can do safely today
- Run the read-only probe to collect real device facts:
  `python3 tools/vidaa_probe.py --discover` then `--host <IP> --connect --pair`.
- Fill the open on-device items in `research/hardware.md` §6 (About screen, service
  menu, EPREL label) — all read-only.

## Honest scope statement
This project distinguishes **proven-impossible** (none here), **undocumented**
(much — no prior VIDAA-MT9618 Android port exists), and **unsolved-but-approachable**
(the boot-chain measurement + OCL research). It will not call the port feasible
without evidence, nor call it impossible merely because no ready guide exists.

## Google services / DRM caveat
AOSP Android TV ≠ Google-certified. Expect **no Play Store / Chromecast / Widevine
L1** on any custom build; sideloaded APKs + Widevine L3 (SD) are the realistic
ceiling. Assessed in [`research/android-compatibility.md`](research/android-compatibility.md) §5.
