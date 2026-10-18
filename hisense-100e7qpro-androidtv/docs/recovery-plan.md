# Recovery & Rollback Plan

> **Read this before anything else that touches the TV.** The device is a costly,
> fully-working 100" panel. A *theoretical* rollback is not a *verified* one — this
> document separates the two and gates every invasive step behind a proven recovery.

## 0. Current rollback status: **THEORETICAL (not yet verified)**
No recovery has been exercised on this unit. Until §3 is completed, treat **every**
write/flash/unlock as unrecoverable and therefore out of scope.

## 1. Known-good recovery mechanism (from official sources)
- Hisense ships firmware as `*.tar.gz` → `upgrade_loader.pkg`; update/recover via
  **FAT32 USB with only that file**, hold remote **power** while reconnecting mains,
  TV flashes and reboots ([S-11]).
- Independent reports confirm a bricked Hisense unit was **restored with the stock
  `.pkg`** ([S-32]) — evidence the mechanism *can* recover, on *those* models.

## 2. Pre-conditions before ANY invasive action (all must be ✅)
- [ ] Exact EU model + firmware build string recorded (from About screen).
- [ ] The **matching** stock `.pkg` for *this* model+region downloaded and checksummed
      and kept offline (two copies).
- [ ] A full, read-only **dump of every partition** taken and archived (only possible
      once a safe read path exists; never as part of a write op).
- [ ] UART/serial console access confirmed (for diagnosis if a flash fails).
- [ ] The §1 USB recovery **tested once on a non-destructive re-flash of the same
      stock firmware** → this is what turns rollback from THEORETICAL to **VERIFIED**.
- [ ] Explicit user authorisation for the specific invasive step.

> If any box is unchecked, do **not** proceed to writes. Stay in read-only/Hybrid-B0.

## 3. How to make rollback VERIFIED (non-destructive)
Re-flash the *same* stock firmware via the §1 USB method and confirm the TV returns
to a normal boot. Successful round-trip = the recovery path works on this unit. Only
then is a reversible experiment justifiable.

## 4. Read-only discovery (safe, do these first)
None of these writes to the TV:
1. **About / open-source-licence screen** → model, build, kernel, VIDAA version.
2. **MQTT probe on 36669** ([S-40]) with [`../tools/vidaa_probe.py`](../tools/vidaa_probe.py)
   → device identity + real input list. Read-only (`get_*` only).
3. **Service/info menu** (model-specific) → *view only*, change nothing.
4. **Developer/ADB toggle** if present → `getprop` only (verified-boot state, etc.).
5. **Regulatory/EPREL label** → confirm exact SKU.

## 5. Emulated-first rule for anything risky
Any boot/HAL/image work is prototyped in **emulation/QEMU or on a sacrificial dev
board**, never first on the TV ([`../poc/`](../poc/)). The TV is the *last* place a
change is tried, and only after §2–§3 are green.

## 6. Hard stops (never do, in this phase)
- No bootloader unlock, no partition writes, no DA upload, no AVB/verity disable, no
  `.pkg` from a *different* model, no factory-partition writes — until §2–§3 complete
  **and** the user explicitly authorises that exact step.
