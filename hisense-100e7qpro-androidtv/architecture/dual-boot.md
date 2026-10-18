# Architecture C — Dual Boot / Preserved OEM Environment

Boot either VIDAA (stock) or Android TV, selectable, keeping stock intact as a
fallback.

## Prerequisites
- Bootloader that can present a **boot selection** and chain-load a second OS
  (A/B slots, or a modified boot partition). **UNVERIFIED, assume LOCKED** ([S-33]).
- Enough free **storage** for a second system + userdata. eMMC size **BLOCKED**
  (unknown) — see [`../research/hardware.md`](../research/hardware.md) §1.
- Secure-boot that either permits a second signed image or is relaxed. **UNVERIFIED.**
- Everything Architecture A needs for the Android side (TV HALs) — so it inherits
  A's **BLOCKED** HAL problem *on top of* the boot-selection problem.

## Evidence of feasibility
- MediaTek platforms often have **A/B slots + AVB** ([S-32], [S-30]) — a structural
  pre-condition that *may* exist. **PLAUSIBLE.**
- No evidence of a user-selectable dual-boot on any Hisense VIDAA TV. **UNVERIFIED.**

## Technical problems
1. Requires **modifying the boot/partition layout** → needs an unlocked/signable
   bootloader (same gate as A). **BLOCKED until measured.**
2. **Storage pressure:** a 100" TV's eMMC is sized for one OS; a second full Android
   system may not fit. **BLOCKED (size unknown).**
3. Inherits **all of A's HAL blockers** for the Android slot.
4. Anti-rollback / AVB may reject a modified boot/misc partition.

## OEM features preserved
- **Stock VIDAA slot: 100%** (untouched) — this is the attraction: the original TV
  remains fully usable.
- **Android slot: same as Architecture A** → currently near-zero TV features.

## Still missing
- Confirmed unlockable boot chain; confirmed spare storage; a working Android TV
  image (= Architecture A's missing pieces).

## Device risk
- **HIGH.** Touches boot/misc/partition table; a mistake here is the classic
  hard-brick. Mitigated only by a **verified** `.pkg` recovery ([S-11], [S-32]).

## Qualitative success probability
- **LOW** today — it needs *both* a penetrable boot chain *and* a working Android
  image, and currently has neither confirmed. Its one real virtue (keeping stock
  intact) is better achieved, risk-free, by **Architecture B0** until the boot chain
  is actually characterised. Revisit only if the bootloader is measured unlockable
  **and** Architecture A's HAL problem is solved.
