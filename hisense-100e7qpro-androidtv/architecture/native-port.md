# Architecture A — Native Android TV Replacement

Replace VIDAA entirely; the TV boots AOSP-based Android TV directly.

## Prerequisites
- Bootloader unlockable / signable for *this* board (signed Download Agent or
  relaxed verification) — **UNVERIFIED, assume LOCKED** ([S-33], see
  [`../research/bootloader.md`](../research/bootloader.md)).
- Full MT9618 BSP with **Android-ABI HALs** (TV Input, audio/eARC, video+HDR/DV,
  panel/MEMC, CEC) — **not published**.
- Kernel matching an AOSP TV target + SELinux policy + vendor modules.
- A **verified** stock-recovery path before any write.

## Evidence of feasibility
- **Silicon-level: VERIFIED.** Android TV runs on MT9618 (Hisense U8N [S-30], TCL
  [S-22]).
- **Board-level: no evidence.** No AOSP port exists for any VIDAA MT9618 TV.

## Technical problems (blockers)
1. **Proprietary TV HALs** (TV Input/HDMI+tuner, audio/eARC, HDR/DV, panel
   middleware) have **no open implementation** and are built for a specific
   userland. → `research/android-compatibility.md` §2. **BLOCKED.**
2. **Bootloader/secure-boot** likely signed; no DA. **BLOCKED until measured.**
3. **DRM L1 / Google cert / Play** not portable. **BLOCKED by policy.**
4. **Brick risk is maximal** — full flash of a €-thousands 100" panel.

## OEM features preserved
- Only those for which a working Android HAL can be produced. With generic AOSP:
  effectively **none of the TV-specific ones** (no HDMI-input switching as a TV
  source, no tuner, no panel calibration/HDR pipeline, no eARC). → unacceptable
  against the project's hard requirement (HDMI sources + remote + audio + panel
  must work).

## Still missing
- Android-ABI MT9618 TV HALs; signed bootloader; SELinux policy; DRM keybox;
  verified recovery.

## Device risk
- **HIGH / CRITICAL** (full replacement of boot + system on costly hardware).

## Qualitative success probability
- **LOW.** Silicon works, but the TV-making HALs are proprietary and unobtainable
  through legitimate open channels, and the boot chain is likely locked. Not the
  recommended first path. Would only become credible if (a) the bootloader is
  measured unlocked **and** (b) a legitimate, board-matching Android BSP becomes
  available — neither is in hand.
