# Hardware Discovery — Hisense 100E7Q PRO (EU)

> Status legend: **VERIFIED / PLAUSIBLE / UNVERIFIED / BLOCKED**. Nothing here was
> read off the physical device — everything is from public sources. Items marked
> UNVERIFIED require the non-invasive on-device checks in
> [`docs/recovery-plan.md`](../docs/recovery-plan.md) and
> [`tools/`](../tools/) to confirm.

## 1. Platform summary (best current understanding)

| Attribute | Value | Status | Source |
|-----------|-------|--------|--------|
| SoC | MediaTek **Pentonic 700 = MT9618** | PLAUSIBLE | [S-01], [S-02], [S-03] |
| Process | 12 nm (TSMC) | VERIFIED | [S-06] |
| CPU | 4× ARM Cortex-**A73** @ ~1.4 GHz | UNVERIFIED | [S-01] |
| GPU | ARM **Mali-G52 MC1** | UNVERIFIED | [S-01] |
| OS (as shipped, EU) | **VIDAA U8.5** (Linux-based, not Android) | PLAUSIBLE | [S-02], [S-10] |
| Panel | 100" 4K QLED, direct-LED backlight | PLAUSIBLE | [S-02] |
| Refresh | 144 Hz (official IN); 120 Hz native + 144 Hz VRR mode reported by retailers | UNVERIFIED (conflicting) | [S-03] |
| HDR | Dolby Vision, HDR10+, HDR10, HLG (claimed) | UNVERIFIED | [S-02], S-04 |
| Audio | 2×~10–20 W; Dolby Atmos claimed | UNVERIFIED (conflicting) | S-04 |
| Tuner | DVB-T2/C/S2 (EU market expectation) | UNVERIFIED | S-04 |
| RAM / flash / eMMC | **unknown** | BLOCKED | — |
| Kernel version | **unknown** (likely 5.4.x, see §4) | UNVERIFIED | [S-30], [S-22] |
| Mainboard part (RSAG7.820.xxxxx) | **unknown** | BLOCKED | — |

**Why RAM/flash/mainboard are BLOCKED:** none of the retail or spec sources expose
them, and they cannot be assumed from the SoC. They are only obtainable from the
device (see §6) or a teardown photo of *this exact* model+region+panel revision.

## 2. The single most important finding

The MT9618 (Pentonic 700) is a **dual-personality TV SoC**: Hisense ships it both
under **VIDAA** (this TV, [S-02]) *and* under **Android TV / Google TV** (e.g. the
U8N line, which runs "Android TV OS Build STT2.230310.002, Kernel 5.4.286" per the
root community, [S-30]).

Consequences:
- **Android TV is known to boot and run a full TV stack on MT9618 silicon.** That
  removes the deepest uncertainty of any TV port — "does the SoC even have an
  Android BSP?" The answer is **yes, VERIFIED at the silicon level** ([S-30],
  [S-22] TCL's MT9618 Android-U kernel 5.15.180).
- It does **not** follow that the U8N firmware runs on *this* board. Mainboard part
  numbers, panel T-CON, backlight driver, tuner, Wi-Fi/BT module and DRAM training
  differ between models. The prompt's rule stands: **do not assume two MT9618 TVs
  share a mainboard or compatible firmware.** This is the central gap the project
  must close with evidence, not assumption.

## 3. SoC identity — confidence and the one caveat

- MT9618 ↔ "Pentonic 700" equivalence is stated by multiple independent sources
  ([S-02], [S-06] context, Kodi/XDA threads). **PLAUSIBLE→VERIFIED.**
- The *specific* MT9618 variant/stepping and its enabled feature fuses on the E7Q
  Pro are **UNVERIFIED**. MediaTek TV SoCs ship in sub-variants with different
  decoder/HDR/MEMC fuse maps; the E7Q Pro (direct-LED, positioned below the
  Mini-LED U-series) may have a reduced fuse set versus a U8N.
- **Name trap:** the *standard* **E7Q** (no "Pro") is a **different SoC — MT9603 +
  Cortex-A53, VIDAA U9** ([S-07]). Do not mix E7Q and E7Q **Pro** sources.

## 4. Kernel (inference, to be confirmed)

- Hisense's GPL bucket for this SoC is **`SmartTV-SeriesVidaaU7` = "MT9618 U7"**
  ([S-21]); its README lists a **100-inch model ("100U7N")**, i.e. a 100" MT9618
  panel exists in this exact GPL family. The kernel version inside `oss/` is not
  shown on the repo landing page → **UNVERIFIED**, action item S-21.
- The Android MT9618 cousins run **kernel 5.4.286** ([S-30]); TCL's MT9618 Android-U
  tree is **5.15.180** ([S-22]). A VIDAA U8.5 build from ~2024 most **PLAUSIBLY**
  sits on a 5.4.x or 5.10.x MediaTek LTS base.
- This matters because AOSP 13/14 for TV expects kernel ≥5.4 with specific GKI/ABI
  and vendor modules — see [`research/android-compatibility.md`](android-compatibility.md).

## 5. Similar-hardware map (for research only — NOT a flashing target)

| Device | SoC | OS | Why relevant | Don't |
|--------|-----|----|--------------| ----- |
| Hisense U8N (US) | MT9618 | Android TV | Proves Android TV stack exists for this SoC; has root notes [S-30] | …assume its `.pkg` fits the E7Q Pro board |
| Hisense "100U7N" (in GPL README) | MT9618 | VIDAA U7 | 100" MT9618 GPL source peer [S-21] | …assume identical to E7Q Pro |
| TCL MT9618 models | MT9618 | Android | Second-vendor MT9618 Android kernel [S-22] | …assume Hisense panel/tuner glue |
| Hisense U8G | older MTK | Android | Documented bootloader-unlock + recovery path [S-32] | …extrapolate boot-chain state to MT9618 |

## 6. How to obtain the BLOCKED facts **non-invasively** (no flashing)

All read-only; none modifies the TV. Full procedures in
[`docs/recovery-plan.md`](../docs/recovery-plan.md) §"Read-only discovery".

1. **Settings → System → About / legal / open-source licence** screen: exact model
   string, firmware build ID, and often the kernel/VIDAA version.
2. **Network-side MQTT query** on TCP **36669** ([S-40]) once paired: `get_sources`
   enumerates the real HDMI/TV input list this unit exposes — see
   [`tools/vidaa_probe.py`](../tools/vidaa_probe.py). Read-only.
3. **Service/factory info menu** (model-specific key combo) — *read-only display*,
   do not change values. Reports panel, mainboard rev, hours.
4. **EU regulatory label / EPREL energy-label QR** on the chassis and box: confirms
   exact model+region SKU and panel class.
5. **FCC/CE filings & teardown photos** of the matching SKU for DRAM/eMMC markings.

## 7. Open hardware questions (drive the next research pass)

- [ ] Exact RAM size and eMMC size/part. **(BLOCKED until §6)**
- [ ] Mainboard RSAG7.820.xxxxx of the EU 100E7Q PRO. **(BLOCKED until §6)**
- [ ] Kernel version + whether MT9618 device-tree/BSP is inside S-21 `oss/`.
- [ ] Does the EU datasheet (S-04) confirm HDMI 2.1 count, eARC, VRR/ALLM, DV?
- [ ] Is this unit's MT9618 fuse set equal to or reduced vs. the U8N?
