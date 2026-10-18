# OEM Compatibility Layer (OCL) — design

A shim that lets an Android TV environment keep driving the Hisense/MediaTek
hardware by reusing what the OEM stack already exposes. Central to Architecture B
([`hybrid-port.md`](hybrid-port.md)).

## Principle
Do **not** try to reimplement MediaTek's panel/MEMC/HDR/tuner stacks in open code
(impossible without the NDA BSP — [`../research/android-compatibility.md`](../research/android-compatibility.md) §2).
Instead, **adapt** Android's HAL/framework calls onto whatever OEM control surfaces
exist, newest-evidence-first.

## Known & candidate OEM surfaces

| Surface | What it controls | Evidence | Status |
|---------|------------------|----------|--------|
| **MQTT control (TCP 36669)** | input/source switch, power, volume, keys | [S-40] | **VERIFIED** API |
| SSDP discovery + PIN pairing | device handshake | [S-41] | PLAUSIBLE |
| VIDAA internal IPC (tuner scan, EPG, panel LUTs) | live TV, picture modes | — | **UNVERIFIED** (needs kernel/GPL study [S-21]) |
| `factory`/`tvconfig` partition data | panel gamma/VCOM, dimming, WB, HDR LUTs, tuner region | inferred [research/firmware.md §2] | PLAUSIBLE (as data) |
| Kernel driver nodes (`/dev`, sysfs) for CEC/display | CEC, display pipe | GPL kernel [S-21] | PLAUSIBLE |

## Proposed OCL shape (Architecture B1/B2)

```
Android TV framework
  │  TvInputService / AudioManager / HdmiControlManager / MediaCodec
  ▼
OEM Compatibility Layer (adapters)
  ├─ InputAdapter    → MQTT 36669 set_source/get_sources  [VERIFIED surface]
  ├─ PowerAdapter    → MQTT power / kernel power nodes
  ├─ CecAdapter      → kernel CEC driver node (from GPL kernel)
  ├─ TunerAdapter    → VIDAA tuner IPC            [UNVERIFIED — research needed]
  ├─ PanelAdapter    → tvconfig LUTs + MTK driver ioctls [UNVERIFIED]
  └─ AudioAdapter    → ALSA/eARC via kernel        [UNVERIFIED]
  ▼
VIDAA kernel + MediaTek/Hisense driver blobs (reused in place)
```

## What is realistic per adapter (today)

- **InputAdapter / PowerAdapter / volume / keys:** realisable now — the control
  plane is proven ([S-40]). This alone satisfies "switch to HDMI sources from the
  Android launcher" and "use the original remote" (via CEC/MQTT).
- **CecAdapter:** PLAUSIBLE from the GPL kernel's CEC driver.
- **TunerAdapter / PanelAdapter / AudioAdapter(eARC/Atmos):** **UNVERIFIED** — these
  depend on VIDAA-internal IPC/driver contracts not yet mapped. They are the research
  targets that decide whether live-TV, full HDR pipeline and eARC survive a port.

## Dependencies / risks
- Needs on-device code execution for anything beyond the network control plane
  (gated by boot/dev-mode — see [`../research/bootloader.md`](../research/bootloader.md)).
- OEM blobs built against VIDAA userland may not load under Android libc/linker
  without a compatibility container.
- Firmware updates can change MQTT credentials/port or remove sideload hooks.

## Verdict
The OCL is **partially buildable now** (control plane) and **well-defined as a
research program** for the rest. It is what makes Architecture B the realistic route
to preserving OEM features — provided the deeper IPC surfaces (tuner/panel/audio)
turn out to be reusable, which is the key open investigation.
