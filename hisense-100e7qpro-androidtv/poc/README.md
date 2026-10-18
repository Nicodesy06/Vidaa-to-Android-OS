# poc/

Proof-of-concept area. **Rule: every risky idea is prototyped here in emulation or on
a sacrificial dev board first — never first on the TV** (see
[`../docs/recovery-plan.md`](../docs/recovery-plan.md) §5).

## Current status
Empty of device-touching code by design. The only PoC that is safe to run against
real hardware today is the read-only [`../tools/vidaa_probe.py`](../tools/vidaa_probe.py).

## Planned PoCs (gated by the roadmap)
| PoC | What it proves | Gate before it runs on hardware |
|-----|----------------|---------------------------------|
| `ocl-control-bridge/` | Android launcher ↔ OEM control (input/power/volume) via MQTT | none — control plane only (Phase 3b) |
| `qemu-mt9618-kernel/` | GPL kernel boots to console in emulation | none — emulation only (Phase 4) |
| `android-tis-shim/` | Android `TvInputService` → OCL InputAdapter | emulation first; device only after Phase 3 gate |

Nothing that writes TV flash, unlocks a bootloader, or disables verity will be placed
here until recovery is VERIFIED and the step is explicitly authorised.
