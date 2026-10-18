# OEM Feature Preservation Matrix

Per-feature preservability under each architecture. Columns:
- **Confirmed?** — is the feature itself confirmed present on the E7Q Pro
  (datasheet/official) vs. hypothetical.
- **A / B / C** — preservability under each architecture:
  ✅ realistic · 🟡 plausible, needs work · 🔴 blocked/unlikely · — n/a.
- **Evidence / gap** — what supports it and what's missing.

> Reminder: no feature below has been tested on the device. "Confirmed present"
> means confirmed by spec/official source, not by on-device test.

## HDMI & sources
| Feature | Confirmed? | A | B | C | Evidence / gap |
|---------|-----------|---|---|---|----------------|
| HDMI device detect | spec-level | 🔴 | 🟡 | 🔴 | B via OEM driver; A/C need TV Input HAL (none) |
| Input switch HDMI 1–4 | spec-level | 🔴 | ✅ | 🔴 | MQTT `set_source` VERIFIED [S-40] |
| Source select by remote | spec-level | 🔴 | ✅ | 🔴 | CEC/MQTT keys [S-40] |
| HDMI-CEC | likely | 🔴 | 🟡 | 🔴 | kernel CEC driver in GPL [S-21] |
| HDMI-ARC/eARC | datasheet TBD (S-04) | 🔴 | 🟡 | 🔴 | audio DSP/eARC proprietary |
| HDCP | yes (QLED 4K) | 🔴 | 🟡 | 🔴 | tied to DRM/TEE path |
| VRR / ALLM / 144 Hz game | reported, conflicting | 🔴 | 🟡 | 🔴 | needs panel/T-CON middleware |

## Traditional TV
| Feature | Confirmed? | A | B | C | Evidence / gap |
|---------|-----------|---|---|---|----------------|
| Integrated tuner (DVB-T2/C) | EU expectation (S-04 TBD) | 🔴 | 🟡 | 🔴 | TunerAdapter UNVERIFIED |
| Satellite (DVB-S2) | TBD | 🔴 | 🟡 | 🔴 | depends on model variant |
| Channel scan | with tuner | 🔴 | 🟡 | 🔴 | VIDAA tuner IPC unmapped |
| Channel change / EPG | with tuner | 🔴 | 🟡 | 🔴 | same |
| Audio/subtitle tracks | with tuner | 🔴 | 🟡 | 🔴 | same |

## Picture quality
| Feature | Confirmed? | A | B | C | Evidence / gap |
|---------|-----------|---|---|---|----------------|
| HDR10 / HLG | likely | 🔴 | 🟡 | 🔴 | decoder HDR path proprietary |
| HDR10+ / Dolby Vision | claimed (S-02) | 🔴 | 🔴 | 🔴 | DV = licensed TA; not portable |
| Brightness/contrast/color cal | yes | 🔴 | 🟡 | 🔴 | tvconfig LUTs reusable as data |
| AI upscaling / MEMC | yes (Hi-View) | 🔴 | 🔴 | 🔴 | MTK middleware, not portable |
| Local dimming (direct-LED) | yes | 🔴 | 🟡 | 🔴 | backlight driver + tables |
| 144 Hz high refresh | official IN [S-03] | 🔴 | 🟡 | 🔴 | T-CON timing control |

## Audio & peripherals
| Feature | Confirmed? | A | B | C | Evidence / gap |
|---------|-----------|---|---|---|----------------|
| Built-in speakers / volume | yes | 🔴 | ✅ | 🔴 | volume via MQTT [S-40]; routing needs audio HAL |
| Multichannel / passthrough | partial | 🔴 | 🟡 | 🔴 | ALSA/eARC mapping UNVERIFIED |
| Dolby Atmos | claimed, conflicting | 🔴 | 🔴 | 🔴 | licensed + DSP |
| Original Hisense remote | yes | 🔴 | ✅ | 🔴 | IR/CEC + MQTT keys [S-40] |
| Wi-Fi / BT | yes | 🟡 | ✅ | 🟡 | standard once kernel driver up |
| USB / Ethernet | yes | 🟡 | ✅ | 🟡 | standard kernel |
| Standby / wake / power | yes | 🔴 | ✅ | 🔴 | power via MQTT [S-40]; deep-standby needs driver |

## Reading of the matrix
- **Architecture B is the only column with ✅ on the hard-requirement rows**
  (HDMI input switch, source-by-remote, volume, remote, power). Those are achievable
  **now** via the proven MQTT control plane, with no device risk (variant **B0**).
- The 🟡 rows in B (tuner, eARC, local dimming, HDR10) are the research frontier —
  reachable **if** VIDAA's deeper IPC/driver contracts prove reusable.
- The 🔴-heavy A and C columns reflect the missing proprietary TV HALs and the
  locked-boot assumption; DV/Atmos/MEMC are 🔴 everywhere (licensed/proprietary).
