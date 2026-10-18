# Roadmap

Evidence-driven, non-destructive-first. Each phase lists its gate: you do not enter
the next phase until the gate is green. Phases 1–3 carry **zero device risk**.

## Phase 1 — Hardware Discovery  *(status: in progress, non-invasive)*
- [x] Identify SoC family (MT9618 / Pentonic 700) — PLAUSIBLE ([S-01..03]).
- [x] Confirm EU E7Q Pro = VIDAA U8.5 on MT9618, distinct from std. E7Q (MT9603) ([S-02][S-07]).
- [ ] On-device: exact model, firmware build, kernel, VIDAA version (About screen).
- [ ] RAM/eMMC size, mainboard RSAG part (service menu / teardown photo).
- [ ] Parse EU datasheet S-04 (HDMI count, eARC, VRR/ALLM, HDR formats, tuner).
- **Gate → 2:** model + build + kernel version known.

## Phase 2 — Firmware & Source Analysis  *(non-invasive)*
- [ ] Browse `SmartTV-SeriesVidaaU7` `oss/` → kernel version, MT9618 DTS/defconfig,
      tvconfig/tuner drivers ([S-21]).
- [ ] Cross-read TCL MT9618 Android kernel for driver parity ([S-22]).
- [ ] Obtain matching stock `.pkg`, unpack **read-only** in an isolated dir; document
      container + partition list.
- **Gate → 3:** partition model understood; stock `.pkg` archived + checksummed.

## Phase 3 — Boot Feasibility  *(read-only measurement only)*
- [ ] On-screen/ADB: verified-boot state, `ro.boot.*` (if ADB exists).
- [ ] (With care, back-panel) UART boot-log capture → LK vs UEFI, AVB enforcement.
- [ ] BROM/preloader **detection only** (no DA upload, no reads/writes).
- **Gate → invasive phases:** bootloader state measured; **recovery VERIFIED**
  (recovery-plan §3). If locked and no legitimate unlock → **A/C are blocked**;
  continue Hybrid-B only.

## Phase 3b — Hybrid B0 deliverable  *(zero risk, do in parallel from now)*
- [x] Read-only MQTT probe [`../tools/vidaa_probe.py`](../tools/vidaa_probe.py).
- [x] Implement the OCL as a real Android TV `TvInputService` with a swappable
      backend — [`../android/`](../android/) (`HisenseOcl`).
- [x] Verify off-device: JVM protocol test vs a fake broker + Android-14 framework
      type-check (`../tools/run_android_tests.sh`, `../tools/check_android_build.sh`).
- [ ] Map the real input/source list + key set from the device (needs the TV).
- [ ] Build the APK in an AOSP tree / with the Android SDK and sideload-test it.
- **Outcome:** Android-TV front-end that switches real HDMI sources + controls
  panel/volume/power via OEM control, VIDAA untouched. The honest "works now" result.
  *Code exists and passes off-device checks; on-device validation is the open step.*

## Phase 4 — Android TV bring-up  *(emulation first; only if Phase 3 gate green)*
- [ ] Choose AOSP TV target matching the kernel (11/12 for 5.4; 13/14 for 5.15).
- [ ] Kernel + console boot in QEMU; then display/input/audio HAL skeletons.
- **Gate → 5:** Android boots to a console/UI in emulation.

## Phase 5 — OEM Integration (OCL)
- [ ] Implement OCL adapters: Input/Power/CEC (realistic), then Tuner/Panel/Audio
      (research-gated) — [`../architecture/oem-compatibility.md`](../architecture/oem-compatibility.md).
- **Gate → 6:** HDMI switch + remote + audio + basic panel work in the target env.

## Phase 6 — Validation
- [ ] OEM hard-requirements pass (matrix rows marked ✅/🟡 in
      [`../compatibility/oem-feature-matrix.md`](../compatibility/oem-feature-matrix.md)).
- [ ] Stability/thermals/standby; DRM assessed separately (expect L3).

## Phase 7 — Recovery & Rollback (continuous)
- [ ] Keep recovery **VERIFIED** before every write; never flash without it
      ([`recovery-plan.md`](recovery-plan.md)).

---

### Honest critical-path summary
- **Achievable now, no risk:** Phases 1–3 (research/measure) + **Phase 3b (Hybrid B0)**.
- **Gated on an unmeasured fact:** Phases 4–6 (need the Phase-3 boot gate).
- **Not achievable through open channels regardless:** full picture middleware,
  Dolby Vision/Atmos, Widevine L1, Play/certification (see
  [`blockers.md`](blockers.md)).
