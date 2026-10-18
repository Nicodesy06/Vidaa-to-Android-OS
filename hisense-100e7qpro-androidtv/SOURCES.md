# SOURCES

Central registry of every external source used in this project. Each entry has a
stable ID (cited as `[S-nn]` across the docs), the link, why it matters, its
limitations, and a verification status:

- **VERIFIED** — confirmed by primary documentation, source code, or a datasheet.
- **PLAUSIBLE** — technical hypothesis supported by indirect evidence.
- **UNVERIFIED** — claim seen but not independently confirmed.
- **BLOCKED** — a concrete obstacle has been identified here.

> Research snapshot date: **2026-10-18**. All "last updated" dates are as reported
> by the source at that time.

---

## Hardware / platform

| ID | Source | Relevance | Status |
|----|--------|-----------|--------|
| S-01 | [displayspecifications.com — 100E7Q Pro](https://www.displayspecifications.com/en/model/7aaf40e6) | SoC listed as MediaTek Pentonic 700 (MT9618), 4×Cortex-A73 @1.4 GHz, Mali-G52 MC1 | UNVERIFIED (aggregator) |
| S-02 | [displayspecifications.com — E7Q Pro Europe news](https://www.displayspecifications.com/en/news/6cf6dd2) | Confirms EU E7Q Pro = MT9618, VIDAA **U8.5**, Hi-View AI engine | PLAUSIBLE |
| S-03 | [Hisense India — 100E7Q Pro](https://www.hisense-india.com/tv-series/uled-qled-tv/hisense-253-cm-100-inch-qled-ultra-hd-4k-smart-tv-100e7q-pro) | Official listing: "Quad Core / MT9618", 144 Hz | PLAUSIBLE (official, IN region) |
| S-04 | [Hisense ES — Ficha Técnica E7Q PRO (PDF)](https://www.hisense.es/wp-content/uploads/2024/03/Ficha-Tecnica-E7Q-PRO.pdf) | EU/ES datasheet: panel, audio, HDMI, tuner, HDR formats | UNVERIFIED (not yet parsed) |
| S-05 | [MediaTek — Pentonic 700 product page](https://www.mediatek.com/products/pentonic/700) | Official: 4K120 MEMC, 4K120 TCON, AI engine | VERIFIED |
| S-06 | [FlatpanelsHD — Pentonic 700 announcement](https://www.flatpanelshd.com/news.php?subaction=showfull&id=1660900600) | 4K@144, AV1, VVC, Dolby Vision, HDR10+; 12 nm | VERIFIED |
| S-07 | [displayspecifications.com — E7Q (standard) Europe](https://www.displayspecifications.com/en/news/1010dd3) | **Contrast model**: standard E7Q = MT9603 + Cortex-A53, VIDAA U9 — NOT the same platform | PLAUSIBLE |

## Firmware / OS architecture

| ID | Source | Relevance | Status |
|----|--------|-----------|--------|
| S-10 | [Spyrosoft — What is VIDAA OS](https://spyro-soft.com/blog/media-and-entertainment/what-is-vidaa-os-a-comprehensive-guide-to-your-smart-tv-experience) | VIDAA is Linux-based, **not Android**; apps are HTML5 web apps | PLAUSIBLE |
| S-11 | [Hisense ES firmware downloads (example: NU9700)](https://www.hisense.es/descargas-firmware/nu9700/) | Official firmware: `*.tar.gz` → `upgrade_loader.pkg`, USB FAT32 + power-button update flow | VERIFIED (procedure) |
| S-12 | [VIDAA WebApp Development Guide (PDF)](https://www.vidaa.com/wp-content/uploads/2020/12/WebApp_Development_Guide_for_VIDAA.pdf) | Confirms the app model is HTML5/web, not native Android | VERIFIED |

## Open-source / GPL kernel sources

| ID | Source | Relevance | Status |
|----|--------|-----------|--------|
| S-20 | [opensource-hisense (GitHub org)](https://github.com/opensource-hisense?tab=repositories) | Hisense GPL compliance org — the primary legal channel for kernel source | VERIFIED |
| S-21 | [opensource-hisense/SmartTV-SeriesVidaaU7](https://github.com/opensource-hisense/SmartTV-SeriesVidaaU7) | **"MT9618 U7"** — GPL source bucket for the E7Q Pro's SoC family; README lists model **100U7N** among others; top level is `oss/` + README | VERIFIED (exists); kernel version UNVERIFIED |
| S-22 | [TCLOpenSource/TCL_Kernel_OpenSource](https://github.com/TCLOpenSource/TCL_Kernel_OpenSource) | Independent vendor's **MT9618 kernel 5.15.180 for Android U** — proof the SoC has a modern Android kernel tree | PLAUSIBLE (reported) |
| S-23 | [opensource-hisense/SmartTV-Series9 (beep.c, GPLv2)](https://github.com/opensource-hisense/SmartTV-Series9/blob/master/kernel/linux-3.18/sound/ppc/beep.c) | Confirms these repos really carry GPL kernel trees | VERIFIED |

## Bootloader / root / boot-chain (Android-variant MT9618)

| ID | Source | Relevance | Status |
|----|--------|-----------|--------|
| S-30 | [XDA — Root MT9618 Hisense U8N (RSAG7.820.13493)](https://xdaforums.com/t/root-guide-android-tv-how-to-root-mt9618-hisense-tvs-using-mainboard-part-rsag7-820-13493-65u8n-55u8n-etc.4744258/) | Root achieved on **Android** MT9618 models; build STT2.230310.002, kernel 5.4.286; method is mainboard/ROM-specific | PLAUSIBLE (forum, HTTP 403 on direct fetch — not yet fully read) |
| S-31 | [Magisk issue #9108 — Hisense mtk9618 kernel panic](https://github.com/topjohnwu/Magisk/issues/9108) | Reports bootloader **unlocked** + "skip of checking verification" in UART on an MT9618 Hisense board; Magisk patch → kernel panic | PLAUSIBLE |
| S-32 | [XDA — Hisense U8G Bootloader Unlock](https://xdaforums.com/t/hisense-u8g-bootloader-unlock-and-probably-others.4465683/) | mtkclient + empty vbmeta flow on an older Hisense Android model; stock `.pkg` restored bricked units | PLAUSIBLE |
| S-33 | [Wikipedia — Bootloader unlocking](https://en.wikipedia.org/wiki/Bootloader_unlocking) | Modern MediaTek requires an **OEM-signed Download Agent** not normally distributed; recent SoCs use UEFI | PLAUSIBLE (general) |
| S-34 | [wiki.exploitee.rs — Hisense Android TV](https://wiki.exploitee.rs/index.php/Hisense_Android_TV) | Aggregated Hisense TV hardware-hacking notes | UNVERIFIED (not yet read) |

## OEM control / community tooling

| ID | Source | Relevance | Status |
|----|--------|-----------|--------|
| S-40 | [hisensetv (readthedocs)](https://hisensetv.readthedocs.io/en/stable/api.html) | VIDAA control is **MQTT on TCP 36669**, user `hisenseservice` / pass `multimqttservice`, PIN pairing; exposes `get_sources`/`set_source`, power, volume, keys | VERIFIED (library API) |
| S-41 | [warrenrees/myhisense-tv (pyvidaa)](https://github.com/warrenrees/myhisense-tv) | Python lib + MQTT bridge; SSDP discovery, PIN pairing, input switching | PLAUSIBLE |
| S-42 | [warrenrees/ha_vidaatv](https://github.com/warrenrees/ha_vidaatv) | Home Assistant integration; HDMI/TV/app switching + remote keys | PLAUSIBLE |
| S-43 | [camellan/vidaa-appstore](https://github.com/camellan/vidaa-appstore) | Sideload path via JS `Hisense_installApp`; availability is firmware-version dependent | PLAUSIBLE |
| S-44 | [Jellyfin forum — install on VIDAA thread](https://forum.jellyfin.org/t-install-jellyfin-on-vidaaos-based-tv-hisense-sharp-toshiba-etc) | Field reports that `Hisense_installApp` appears/disappears across firmware (N0519 works, P0603 missing, O0904 works) | UNVERIFIED (forum) |

## Cleaning up VIDAA (ads, privacy, custom launcher)

| ID | Source | Relevance | Status |
|----|--------|-----------|--------|
| S-50 | [SmartWorld — pubblicità non saltabili su TV Hisense](https://www.smartworld.it/tecnologia/hisense-vidaa-tv-pubblicita-obbligatoria.html) | Ads at power-on / HDMI switch; settings toggle reportedly not always effective; Nexxen ad partner with ACR access | PLAUSIBLE (press, user reports) |
| S-51 | [TechSpot — Hisense TVs showing non-skippable ads](https://www.techspot.com/news/111653-hisense-tvs-caught-showing-non-skippable-ads-when.html) | Reports beyond Spain; VIDAA renamed Home OS, also on Akai/Schneider/Loewe | PLAUSIBLE |
| S-52 | [Lemmy thread (piefed mirror) on TV ads](https://piefed.jeena.net/post/385585) | User report: DNS blocking on VIDAA leaves a "VIDAA" placeholder in the home ad slot | UNVERIFIED (single user) |
| S-53 | [Xataka Home — Hisense: "prueba temporal"](https://www.xatakahome.com/televisores/se-trato-prueba-temporal-hisense-aclara-polemica-anuncios-sus-televisores-espana) | Hisense statement that forced ads were a temporary test in Spain | VERIFIED (company statement as reported) |
| S-54 | [Tech-ish — how to block Hisense ads](https://tech-ish.com/2026/03/17/hisense-is-testing-unskippable-ads-on-its-tvs-heres-how-to-block-them/) | AdGuard public DNS 94.140.14.14 / 94.140.15.15; already-downloaded ads persist | PLAUSIBLE |
| S-55 | [LazyAdmin — block ads on your smart TV](https://lazyadmin.nl/home-network/how-to-block-ads-on-your-smart-tv/) | Domain lists for Samsung/LG/Roku, none for VIDAA | VERIFIED (absence of a VIDAA list) |
| S-56 | [Microimpuls — Hisense VIDAA debug guide](https://microimpuls.com/en/docs/smarty/extra/hisense-vidaa-debug-guide) | `hisense://debug` in the TV browser installs a web app by URL; no firmware versions stated | PLAUSIBLE (third-party docs) |

## To still acquire (non-invasive)

- S-04 full parse of the EU datasheet (HDMI count, eARC, VRR/ALLM, HDR formats, tuner types).
- S-21 `oss/` contents → exact kernel version + whether MT9618 BSP/device-tree is present.
- S-30 full method text (currently 403 via automated fetch; open in a browser).
- Whether the **100E7Q PRO** model string itself appears in any GPL README (S-21) or firmware index (S-11).
