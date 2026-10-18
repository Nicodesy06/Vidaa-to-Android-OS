# Firmware & VIDAA OS Architecture

> Everything here is reconstructed from public sources and GPL releases. No image
> from *this* TV has been dumped or analysed. Status tags as in [`SOURCES.md`](../SOURCES.md).

## 1. What VIDAA actually is

- **Linux-based, not Android** ([S-10], [S-12]). There is no Android framework,
  no ART/Dalvik, no `system_server`, no Android HAL layer in the AOSP sense.
- **Apps are HTML5/JS web apps** rendered by an on-device web engine ([S-10],
  [S-12]). The "app ecosystem" therefore does **not** carry over to Android — it is
  not APKs. This is why "preserve VIDAA apps" is explicitly a non-goal.
- Userland is a classic embedded-Linux stack (very likely Buildroot/Yocto-style;
  **UNVERIFIED** which). The MT9618 GPL bucket is C/C++ with an `oss/` tree ([S-21]).
- Media/picture features (MEMC, AI upscale, HDR tone-mapping, local dimming) are
  implemented in **MediaTek proprietary middleware + kernel drivers**, not in
  portable open code — this is the crux of the OEM-preservation problem.

## 2. Likely partition / boot model (PLAUSIBLE, from the MTK TV + Android cousins)

MediaTek TV platforms and the Android MT9618 siblings ([S-30], [S-31], [S-32])
point to an A/B-ish, AVB-protected layout. For the VIDAA unit the concrete layout
is **UNVERIFIED** until a read-only dump exists, but expect roughly:

```
preloader / BROM  ──▶ Little Kernel / UEFI (MTK) ──▶ boot (kernel+ramdisk)
                                   │
         vbmeta (AVB) ───verifies──┘   dtbo, logo, tee, lk, misc,
                                       vendor/system/data (VIDAA rootfs),
                                       tvconfig/factory (panel+tuner calibration),
                                       recovery / upgrade
```

Key implications:
- **`vbmeta` / AVB chain of trust** is present on the Android cousins ([S-32]
  empty-vbmeta flow). A VIDAA build likely has an analogous verified-boot step.
  Whether it is *enforced* on retail VIDAA units is **UNVERIFIED** (one Android
  MT9618 board reported "skip of checking verification" in UART, [S-31] — but that
  is a different model).
- A **`factory`/`tvconfig` partition** almost certainly holds panel gamma/VCOM,
  backlight/local-dimming tables, tuner region data, white-balance and HDR LUTs.
  **Preserving or re-reading this partition is the single highest-value OEM asset**
  (see [`architecture/oem-compatibility.md`](../architecture/oem-compatibility.md)).

## 3. Official firmware distribution & update flow (VERIFIED procedure)

From Hisense ES firmware pages ([S-11]):
- Firmware ships as `*.tar.gz` (e.g. `HE70M9710UWTSG_E004_Hisense_H0323.tar.gz`)
  which unpacks to a single **`upgrade_loader.pkg`**.
- Update = FAT32 USB with only that file + power-button-held reconnect; the TV
  flashes and reboots.
- **This same mechanism is the primary stock-recovery path** (restore known-good
  firmware) and must be validated *before* any experiment — see
  [`docs/recovery-plan.md`](../docs/recovery-plan.md).

**Unknowns / to verify:**
- `.pkg` internal format: whether it is a MediaTek `upgrade` container (often a
  header + per-partition signed images) or a Hisense wrapper. **UNVERIFIED.**
- Whether downgrades are allowed (anti-rollback fuse). Field reports on the sideload
  function ([S-44]) show users *downgrading* VIDAA (N0519) — suggesting rollback is
  at least sometimes permitted, but this is **UNVERIFIED** for the E7Q Pro.

## 4. GPL source — what Hisense is legally obligated to publish

- Org: **`opensource-hisense`** ([S-20]) — 22 repos.
- Relevant bucket: **`SmartTV-SeriesVidaaU7` = "MT9618 U7"** ([S-21]), README lists
  many models incl. **100U7N** (a 100" MT9618). Top level `oss/` + README; **kernel
  version not shown on landing page** → action item.
- Sibling buckets confirm these repos genuinely carry GPL kernel trees ([S-23],
  Linux-3.18 for the old Series9).
- **What GPL gives us:** the kernel + any GPL/LGPL userland (bootloader LK is often
  BSD/MTK-proprietary and may be *absent*). **What it does NOT give us:** MediaTek
  proprietary HALs, the picture-quality middleware, the DRM TAs, or signing keys.
  So GPL source lets us *build a kernel* but not a *fully functional TV*.

## 5. What is reusable vs. not (first-pass judgement)

| Component | Reusable on Android? | Status | Note |
|-----------|----------------------|--------|------|
| Linux kernel + MT9618 drivers (GPL) | Partially | PLAUSIBLE | Kernel yes; but Android needs its own ABI/GKI + SELinux + binderized HALs |
| VIDAA web runtime / apps | **No** | VERIFIED | HTML5 app model, no Android equivalent |
| MTK picture/MEMC/HDR middleware | **No (binary, VIDAA-linked)** | PLAUSIBLE | Proprietary, built against VIDAA userland, not Android HAL ABI |
| `factory`/`tvconfig` calibration data | **As data, yes** | PLAUSIBLE | Re-readable tables; value depends on a driver that consumes them |
| MQTT control daemon (port 36669) | As a bridge, yes | VERIFIED | [S-40]; usable from *either* OS as an IPC endpoint |
| Bootloader (LK/UEFI) | Reuse in place only | UNVERIFIED | Likely signed; not rebuildable without keys |

## 6. Next firmware actions (all non-invasive)

- [ ] Parse EU datasheet S-04 for confirmed I/O (HDMI 2.1 count, eARC, tuner).
- [ ] Clone/browse S-21 `oss/`: record exact kernel version, look for `arch/arm64/
      boot/dts/mediatek/*mt9618*`, defconfig, and any `mtk_tvconfig`/`tvms` drivers.
- [ ] Obtain (do not flash) the matching EU `.pkg`, unpack read-only, document the
      container format and partition list. Keep in an isolated directory.
- [ ] Confirm VIDAA version string from the on-device About screen.
