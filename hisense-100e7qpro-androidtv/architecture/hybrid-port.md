# Architecture B — Hybrid: Android TV front-end + preserved OEM back-end

**The most promising architecture given the evidence.** Keep the VIDAA kernel and
MediaTek/Hisense drivers that already drive the panel, HDMI inputs, tuner, audio
and CEC; run an Android TV *environment* as the primary UI; bridge the two with an
**OEM Compatibility Layer** (see
[`oem-compatibility.md`](oem-compatibility.md)).

## Variants (decreasing invasiveness)

- **B0 — No-reflash control bridge (buildable today, zero device risk).** Leave
  VIDAA fully intact. Run the Android TV *experience* on an external player (or, if
  ever allowed, a sideloaded VIDAA container) and drive the TV's inputs/power/volume
  through the **MQTT 36669** control plane ([S-40]). This does not replace the OS but
  **proves the OEM control surface end-to-end** and delivers an Android TV launcher
  that can switch real HDMI sources and control the panel. It is the honest
  "works now" deliverable and the fallback baseline.
- **B1 — Containerised Android on the VIDAA kernel.** Run an Android userland in a
  container/namespace on the *existing* Linux kernel, with a shim exposing VIDAA
  services (input switch, tuner, volume, CEC) to Android via the compatibility
  layer. Avoids needing Android-ABI TV HALs. Requires on-device code execution
  (developer/ADB or a controlled rootfs addition) — **UNVERIFIED** if possible on
  this build.
- **B2 — Android userland, VIDAA drivers.** Replace userland with Android but keep
  the VIDAA kernel + proprietary driver blobs, adapting them behind Android HAL
  interfaces. Most capable, most work, needs reflash → higher risk.

## Prerequisites
- A reusable VIDAA service/IPC surface for inputs/tuner/panel. **Partly VERIFIED**
  (MQTT 36669 control plane [S-40]); deeper panel/tuner IPC **UNVERIFIED**.
- On-device execution (for B1/B2): developer mode/ADB or a signed rootfs delta.
  **UNVERIFIED.**
- GPL kernel ([S-21]) to understand the driver/IPC contracts.

## Evidence of feasibility
- Control plane is real and testable ([S-40], [S-41], [S-42]). **VERIFIED.**
- Containerising Android on an embedded Linux kernel is a known pattern (generic),
  but **UNVERIFIED** on this locked appliance.

## Technical problems
- VIDAA's picture/tuner middleware is **binary and linked against VIDAA userland**;
  a container may not satisfy its library/IPC expectations ([research/firmware.md] §5).
- B1/B2 still need a way to run our code on the device (gated by boot/dev-mode).
- Input latency / compositor ownership: who owns the framebuffer (VIDAA vs Android).

## OEM features preserved
- **Highest of the three.** HDMI input switching, power/volume, CEC and remote keys
  are reachable **today** via the control plane (B0). B1/B2 can additionally expose
  tuner and panel features **if** their IPC is reusable. This is the only
  architecture that can satisfy the project's hard OEM requirement with current
  evidence.

## Still missing
- Proof that on-device code execution is possible without an unlock (B1/B2).
- Mapping of VIDAA's internal IPC for tuner/panel beyond the MQTT control plane.

## Device risk
- **B0: NONE** (network/control only). **B1: LOW–MED** (no reflash if via dev mode).
- **B2: MED–HIGH** (reflash of userland).

## Qualitative success probability
- **B0: HIGH** (deliverable now). **B1: MEDIUM** (depends on dev-mode/exec).
- **B2: LOW–MEDIUM** (depends on reflash + driver ABI). Recommended sequence:
  **B0 now → characterise exec surface → B1 → only then weigh B2.**
