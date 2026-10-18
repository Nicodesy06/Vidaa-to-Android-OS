# tools/

Non-invasive, **read-only** diagnostics. Nothing here writes to the TV, touches
flash, or attempts any boot/unlock operation. These exist to retire the
`UNVERIFIED`/`BLOCKED` facts in [`../research/hardware.md`](../research/hardware.md)
safely.

## `vidaa_probe.py` — read-only VIDAA MQTT probe
Collects device identity, the real HDMI/TV **source list**, volume and tv-state over
the TV's local MQTT control interface (TCP 36669), plus optional LAN SSDP discovery.

- **Read-only by design:** only `sourcelist` / `getvolume` / `gettvstate` queries and
  PIN pairing. No source-switch, no key-send, no setting change — there is no
  mutating code path.
- **Safe default:** without `--connect` it is a dry run and touches no network.
- Protocol basis: community `hisensetv`/`pyvidaa` ([`../SOURCES.md`](../SOURCES.md)
  [S-40][S-41]); **UNVERIFIED on the 100E7Q PRO** (creds/port/topics are
  firmware-dependent).

```bash
pip install paho-mqtt
python3 vidaa_probe.py --discover                       # find TVs (read-only)
python3 vidaa_probe.py --host <IP> --connect --pair     # one-time PIN pairing
python3 vidaa_probe.py --host <IP> --connect            # read-only queries
```

### Planned (not yet built; all read-only)
- `pkg_inspect.py` — unpack a stock `.pkg`/`.tar.gz` in an **isolated dir** and list
  partitions/headers without modifying anything (Phase 2).
- `gpl_scan.py` — scan a local clone of `SmartTV-SeriesVidaaU7` for kernel version,
  MT9618 DTS, defconfig and tvconfig/tuner drivers (Phase 2).

> Any tool that could modify the TV is explicitly **out of scope** until the recovery
> path is VERIFIED and the user authorises the specific step
> ([`../docs/recovery-plan.md`](../docs/recovery-plan.md)).
