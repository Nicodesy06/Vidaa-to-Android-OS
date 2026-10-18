# Bootloader & Secure-Boot Analysis

> **This is the gating technical risk for the whole project.** No bootloader
> operation has been performed; this is a research-only assessment of what the boot
> chain *probably* is and how to characterise it **non-invasively**. Status tags per
> [`SOURCES.md`](../SOURCES.md).

## 1. Expected boot chain (PLAUSIBLE, MT9618 family)

```
BROM (mask ROM, MTK)                 ← immutable; speaks the MTK download protocol
  └─ preloader                       ← first flash stage; verified by BROM fuses
       └─ LK / "Little Kernel" or UEFI   ← MTK bootloader; checks boot/vbmeta
            └─ boot.img (kernel+ramdisk)
                 └─ AVB/vbmeta verifies system/vendor/dtbo
```

- Recent MediaTek SoCs use **UEFI** and require an **OEM-signed Download Agent (DA)**
  to talk to BROM/preloader; the signed DA is **not publicly distributed** ([S-33]).
  This is the primary obstacle to low-level flashing/unlock on modern parts.
- The Android MT9618 cousins show an **AVB/vbmeta** step ([S-32] empty-vbmeta flow,
  [S-31] "skip of checking verification" in UART).

## 2. What the evidence says about *lockedness* — and its limits

| Evidence | What it shows | Does it apply to E7Q Pro? |
|----------|---------------|---------------------------|
| [S-31] one MT9618 Hisense board: **bootloader unlocked**, verification skipped | *Some* MT9618 Hisense boards ship relaxed | **No — different model.** Data point only |
| [S-30] U8N rooted, but "only the RSAG7.820.13493 ROM I have" | Board/ROM-specific; not generic | **No** |
| [S-32] U8G mtkclient + empty vbmeta worked | Older SoC had an accessible DA path | **No — older SoC** |
| [S-33] modern MTK needs signed DA | Default expectation = **locked** | **Baseline assumption** |

**Net:** The honest baseline is **"assume locked / signed until measured."** The
unlocked data points are encouraging but come from *other models*; projecting them
onto the E7Q Pro would violate the project's own evidence rule. **Status: UNVERIFIED
→ treat as BLOCKED for any write operation until measured.**

## 3. Non-invasive characterisation plan (read-only, no writes, no unlock attempts)

Ordered least-risk first. None of these writes to flash or attempts an unlock.

1. **On-screen evidence (zero risk):**
   - About / legal screen → build ID, kernel, VIDAA version.
   - Watch for a Developer-mode / ADB-over-network toggle (some VIDAA builds expose
     limited ADB). If present, `adb shell getprop` reveals `ro.boot.*`,
     `ro.secureboot.*`, verified-boot state — **read-only**.
2. **Network control probe (zero flash risk):** MQTT 36669 ([S-40]) — confirms the
   device identity and input list; see [`tools/vidaa_probe.py`](../tools/vidaa_probe.py).
3. **UART console (low risk, read-only):** if service pads are accessible, capturing
   the boot log reveals the bootloader (LK vs UEFI), AVB state, and whether
   verification is enforced — **capture only, send no commands.** Requires opening
   the back; on a 100" panel this is a two-person, high-care operation — defer.
4. **BROM handshake *detection only* (no DA upload):** observing whether the SoC
   enumerates in BROM/preloader mode indicates the low-level protocol is reachable.
   **Do not upload a Download Agent, do not read/write partitions** at this stage —
   that crosses into invasive territory and needs explicit authorisation.

> Everything beyond step 4 (uploading a DA, dumping partitions, unlocking, flashing)
> is **invasive** and **out of scope** until (a) the above data is collected and
> (b) the user explicitly authorises it with a *verified* recovery path in hand.

## 4. Decision gate

```
Is a signed DA / unlock available for THIS board?
  ├─ measured NO  → native/dual-boot flashing is BLOCKED.
  │                 Only non-flash options remain (Hybrid-control, fallbacks).
  └─ measured YES → proceed to firmware dump + emulation POC first,
                    never direct device flashing, until recovery is proven.
```

## 5. Current verdict

- **Bootloader state of the EU 100E7Q PRO: UNVERIFIED; working assumption = LOCKED
  (signed DA required).**
- No basis yet to attempt any unlock. The next legitimate steps are all read-only
  (§3 steps 1–2 first; 3–4 only with the back-panel caveat and authorisation).
- This single unknown determines whether architectures **A (native)** and
  **C (dual-boot)** are even reachable — see
  [`architecture/`](../architecture/) and [`docs/blockers.md`](../docs/blockers.md).
