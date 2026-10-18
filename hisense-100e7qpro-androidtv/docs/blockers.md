# Blockers Register

Concrete obstacles, each classified. A blocker is **BLOCKED (proven)** only when a
source demonstrates it; **CONDITIONAL** when it depends on an unmeasured fact;
**OPEN** when it's just not yet investigated. This is the honest "what stands in the
way" list the brief asks for — distinguishing *proven impossible* from *undocumented*
from *unsolved*.

| # | Blocker | Class | Why | Source | How to retire it |
|---|---------|-------|-----|--------|------------------|
| B1 | Bootloader likely signed / no public Download Agent | CONDITIONAL | Modern MTK needs OEM-signed DA | [S-33] | Measure on device (read-only boot-log/BROM detect) → `research/bootloader.md` §3 |
| B2 | No open MT9618 **TV Input HAL** (HDMI-in + tuner) for Android | BLOCKED (proven absent) | No published impl; proprietary BSP | `research/android-compatibility.md` §2 | Hybrid OCL adapters instead of AOSP HAL; or legitimate BSP (not available) |
| B3 | MTK picture middleware (MEMC/HDR/DV) not portable | BLOCKED | binary, VIDAA-linked, licensed | [research/firmware.md] §5 | Reuse in place (Hybrid) only |
| B4 | Dolby Vision / Atmos are licensed TAs | BLOCKED (policy) | per-device license + TA | [S-02] (claim) | Not retireable on a custom build |
| B5 | Widevine **L1** keybox not portable | BLOCKED (policy) | provisioned per-device/TEE | `android-compatibility.md` §5 | Accept L3 (SD) |
| B6 | Google Play / Chromecast / certification | BLOCKED (policy) | needs Google license | `android-compatibility.md` §5 | Sideload APKs only |
| B7 | RAM / eMMC size unknown | OPEN | no public spec | `research/hardware.md` §1 | On-device About/service menu; teardown photo |
| B8 | Mainboard RSAG part of EU 100E7Q PRO unknown | OPEN | not published | `research/hardware.md` §1 | On-device service menu / label |
| B9 | E7Q Pro kernel version unknown | OPEN | GPL `oss/` not yet read | [S-21] | Browse the GPL repo tree |
| B10 | On-device code execution (dev-mode/ADB) unconfirmed | OPEN | needed for Hybrid B1/B2 | `architecture/hybrid-port.md` | Check Settings for developer/ADB toggle |
| B11 | VIDAA tuner/panel/audio internal IPC unmapped | OPEN | only MQTT control plane known | `architecture/oem-compatibility.md` | Study GPL kernel + runtime (needs B10) |
| B12 | Anti-rollback / AVB may block custom boot | CONDITIONAL | AVB present on cousins | [S-31][S-32] | Measure; keep stock `.pkg` recovery |
| B13 | No existing AOSP port for any VIDAA MT9618 TV | OPEN (not a proof of impossibility) | none found | `research/github-findings.md` D | Doesn't block Hybrid B0; informs A/C risk |

## The one-line summary
- **Proven-blocked:** the AOSP-native TV HALs, DV/Atmos, Widevine L1, Play/cert
  (B2–B6). These make a *full-feature pure-AOSP replacement* not achievable through
  legitimate open channels today.
- **Conditional (measure, don't assume):** bootloader lock + AVB (B1, B12).
- **Open (just research):** hardware facts, kernel version, exec surface, deeper IPC
  (B7–B11, B13) — none blocks the risk-free Hybrid-B0 work.

**Therefore the project is neither "impossible" nor "ready":** a *risk-free partial*
(Hybrid B0 + diagnostics) is achievable now; a *full native port* is blocked on
proprietary/policy components and an unmeasured boot chain.
