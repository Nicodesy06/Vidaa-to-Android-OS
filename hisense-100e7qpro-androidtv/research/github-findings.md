# GitHub / Community Findings

Projects, repos and threads evaluated for direct reuse. Each is classified as
**WORKING** (verified to do something useful), **POC** (proof-of-concept /
partial), **REFERENCE** (useful info, not runnable here), or **ABANDONED/N-A**.
Status tags per [`SOURCES.md`](../SOURCES.md).

## A. Directly reusable now (OEM control, non-invasive)

### `hisensetv` + `pyvidaa` / `ha_vidaatv` — VIDAA network control — **WORKING**
- [S-40] [S-41] [S-42]. Control VIDAA over **MQTT, TCP 36669**, user
  `hisenseservice` / pass `multimqttservice`, 4-digit PIN pairing, SSDP discovery.
- Exposes `get_sources` / `set_source(id,name)` (HDMI/TV input switching), power,
  volume, and arbitrary remote keys.
- **Relevance:** this is a real, testable, read-mostly IPC surface into the TV that
  works *regardless of which OS controls the panel*. It is the backbone of the
  Hybrid architecture (B) and of our diagnostic tool
  [`tools/vidaa_probe.py`](../tools/vidaa_probe.py).
- **Limitations:** control-plane only — it switches inputs and sends keys; it does
  **not** expose panel calibration, tuner scan tables, or HDMI EDID internals. It
  depends on the VIDAA daemon being alive, so it cannot help a *replacement* OS
  unless that daemon (or an equivalent) is preserved. Firmware can change the
  credentials/port. **Verification: library API VERIFIED; behaviour on E7Q Pro
  UNVERIFIED (not tested on device).**

### `camellan/vidaa-appstore` — web-app sideload — **POC / fragile**
- [S-43] [S-44]. Uses the JS hook `Hisense_installApp` from the VIDAA browser to
  sideload web apps.
- **Relevance:** a *fallback-tier* capability (install extra VIDAA apps). **Not**
  the project goal (not Android, not APKs).
- **Limitations:** presence of `Hisense_installApp` is firmware-version dependent
  (works N0519/O0904, missing P0603 per field reports); hosting domains come and
  go. **UNVERIFIED on E7Q Pro.**

## B. Platform / kernel sources — **REFERENCE (high value)**

### `opensource-hisense/SmartTV-SeriesVidaaU7` — "MT9618 U7" GPL — **REFERENCE**
- [S-20] [S-21]. The legally-mandated GPL source bucket for this SoC family;
  README lists a 100" MT9618 ("100U7N"). Contains `oss/`.
- **Relevance:** the only *legitimate* route to the kernel that drives this exact
  silicon. Essential for any native/hybrid attempt.
- **Limitations:** GPL ⊄ full BSP. No MTK proprietary HALs, no picture middleware,
  no signing keys, bootloader likely absent. Kernel version not yet confirmed.

### `TCLOpenSource/TCL_Kernel_OpenSource` — MT9618 Android-U kernel 5.15.180 — **REFERENCE**
- [S-22]. A *second vendor's* MT9618 kernel, built for **Android U**.
- **Relevance:** demonstrates the MT9618 has an Android-oriented kernel tree and
  shows which drivers an Android build enables on this SoC — a cross-check against
  the Hisense GPL tree.
- **Limitations:** TCL board glue (panel/tuner/Wi-Fi) differs; not directly
  bootable on Hisense hardware.

## C. Bootloader / boot-chain intelligence (Android MT9618 cousins) — **REFERENCE**

### XDA "Root MT9618 Hisense (U8N, RSAG7.820.13493)" — **POC (other model)**
- [S-30]. Root on **Android** MT9618 Hisense TVs; build STT2.230310.002, kernel
  5.4.286; author states the method is tied to *that one mainboard's* ROM files.
- **Relevance:** strongest evidence the MT9618 Android boot chain is penetrable on
  *some* Hisense boards, and gives a concrete kernel version.
- **Limitations:** model/mainboard/build-specific; **not** applicable as-is to the
  VIDAA E7Q Pro; direct automated fetch returned HTTP 403 — full method text still
  to be read in a browser. **BLOCKED for our model until the E7Q Pro board is
  characterised.**

### Magisk issue #9108 — "Hisense mtk9618 kernel panic" — **REFERENCE**
- [S-31]. Reporter: bootloader **unlocked**, UART shows "skip of checking
  verification"; patched boot image → kernel panic.
- **Relevance:** (1) at least one MT9618 Hisense board ships with an unlocked
  bootloader and relaxed verification; (2) naive image patching is fragile on these
  boards — a warning for our risk model.
- **Limitations:** different model; a single data point; **UNVERIFIED** for E7Q Pro.

### XDA "Hisense U8G Bootloader Unlock" — **REFERENCE**
- [S-32]. Older Hisense Android model: mtkclient + empty `vbmeta`; crucially, users
  **recovered bricked units with the stock `.pkg`** — validates a recovery path.
- **Limitations:** older SoC, not MT9618; general technique, not a recipe here.

### Wikipedia — bootloader unlocking / Android boot — **REFERENCE**
- [S-33]. Modern MediaTek needs an **OEM-signed Download Agent** (not distributed);
  recent SoCs use UEFI. Sets realistic expectation: **BLOCKED** path unless the
  E7Q Pro bootloader turns out to be unlocked/relaxed (to be measured, not assumed).

## D. Not applicable / not found

- **No ready-made Android TV port for any VIDAA MT9618 TV** was found. (Absence of a
  guide ≠ proven impossible — see [`docs/blockers.md`](../docs/blockers.md).)
- **No VIDAA "jailbreak"/root for the E7Q Pro** specifically was found.
- `wiki.exploitee.rs` Hisense Android TV page [S-34] — not yet read; queue for the
  bootloader pass.
- Sony BRAVIA REST API, generic Android-box custom-ROM guides — **N/A** (wrong
  platform).

## E. Consolidated takeaways

1. **Control plane into VIDAA is real and testable today** (MQTT 36669) → build on
   it now, non-invasively.
2. **Kernel source for the exact SoC exists under GPL** → native/hybrid is not a
   blank page, but GPL ≠ full BSP.
3. **Android runs on MT9618 in the wild** (Hisense U8N, TCL) → silicon-level
   feasibility of Android TV is VERIFIED; board-level feasibility for *this* unit is
   the open question.
4. **Boot chain is the gating risk** → one MT9618 board was unlocked, but modern MTK
   generally isn't; must be *measured* on the E7Q Pro, never assumed.
