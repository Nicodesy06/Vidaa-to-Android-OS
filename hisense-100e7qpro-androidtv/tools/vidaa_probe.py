#!/usr/bin/env python3
"""
vidaa_probe.py — READ-ONLY diagnostic probe for Hisense/VIDAA TVs.

Purpose
-------
Collect non-invasive, read-only facts about a VIDAA TV (e.g. Hisense 100E7Q PRO)
over the TV's local MQTT control interface, to fill the BLOCKED/UNVERIFIED items in
../research/hardware.md WITHOUT touching firmware, partitions, or the bootloader.

What it does            : SSDP discovery, PIN pairing, and READ-ONLY state queries
                          (source/input list, volume, tv state) + optional raw
                          topic sniff.
What it DELIBERATELY     : it never switches sources, never sends remote keys,
does NOT do                never changes volume, never writes any setting. There is
                          no code path that mutates TV state.

Protocol basis
--------------
Modeled on the community `hisensetv` library: MQTT over TLS on TCP 36669, default
credentials user `hisenseservice` / pass `multimqttservice`, 4-digit PIN pairing.
See ../SOURCES.md [S-40], [S-41]. The exact topics/credentials/port are VENDOR- and
FIRMWARE-dependent and are UNVERIFIED on the 100E7Q PRO — treat output accordingly.

Dependencies
------------
    pip install paho-mqtt        # required for --connect
    # SSDP discovery uses only the stdlib (socket).

Safety
------
Pass --connect explicitly to touch the network. Without it the script only prints
what it *would* do (dry run). Even with --connect it issues read-only queries only.

Usage
-----
    python3 vidaa_probe.py --discover                 # find TVs on the LAN (read-only)
    python3 vidaa_probe.py --host 192.168.1.50 --connect --pair   # one-time PIN pair
    python3 vidaa_probe.py --host 192.168.1.50 --connect          # read-only queries
"""
from __future__ import annotations

import argparse
import json
import socket
import sys
import time
import uuid

MQTT_PORT = 36669
MQTT_USER = "hisenseservice"
MQTT_PASS = "multimqttservice"

# --- READ-ONLY topic set only. No action/set/key topics appear anywhere here. ---
# %s is substituted with this client's id. These are *query* and *data* topics.
Q_SOURCELIST = "/remoteapp/tv/ui_service/%s/actions/sourcelist"
Q_GETVOLUME = "/remoteapp/tv/platform_service/%s/actions/getvolume"
Q_GETTVSTATE = "/remoteapp/tv/ui_service/%s/actions/gettvstate"
# Subscribe to every reply under the mobile data tree (read-only sink):
SUB_DATA = "/remoteapp/mobile/%s/#"
# Pairing (one-time): request a PIN and submit the code. This authorizes the client;
# it does not change any TV setting.
Q_PAIR_REQUEST = "/remoteapp/tv/ui_service/%s/actions/gettvstate"  # triggers PIN on some builds
AUTH_CODE_TOPIC = "/remoteapp/tv/ui_service/%s/actions/authenticationcode"


def discover(timeout: float = 4.0) -> list[str]:
    """SSDP M-SEARCH on the LAN. Read-only: sends a standard discovery datagram and
    collects responders. Returns candidate IPs (does not connect to any)."""
    msg = "\r\n".join([
        "M-SEARCH * HTTP/1.1",
        "HOST: 239.255.255.250:1900",
        'MAN: "ssdp:discover"',
        "MX: 2",
        "ST: ssdp:all",
        "", "",
    ]).encode()
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
    s.settimeout(timeout)
    found: dict[str, str] = {}
    try:
        s.sendto(msg, ("239.255.255.250", 1900))
        end = time.time() + timeout
        while time.time() < end:
            try:
                data, addr = s.recvfrom(65535)
            except socket.timeout:
                break
            text = data.decode(errors="replace")
            found.setdefault(addr[0], text.splitlines()[0] if text else "")
    finally:
        s.close()
    hits = [ip for ip, line in found.items()
            if "hisense" in line.lower() or "vidaa" in line.lower() or True]
    return sorted(found) if not hits else sorted(found)


def run_queries(host: str, pair: bool, client_id: str, timeout: float = 8.0) -> None:
    try:
        import paho.mqtt.client as mqtt
    except ImportError:
        sys.exit("paho-mqtt not installed. Run: pip install paho-mqtt")

    results: dict[str, object] = {}

    def on_connect(client, userdata, flags, rc, *args):
        print(f"[mqtt] connect rc={rc}")
        client.subscribe(SUB_DATA % client_id)
        # READ-ONLY queries. Empty JSON payload == "please report", not "please set".
        for topic in (Q_SOURCELIST, Q_GETVOLUME, Q_GETTVSTATE):
            client.publish(topic % client_id, json.dumps({}))
        if pair:
            client.publish(Q_PAIR_REQUEST % client_id, json.dumps({}))

    def on_message(client, userdata, msg):
        payload = msg.payload.decode(errors="replace")
        print(f"[mqtt] {msg.topic}\n       {payload}")
        results[msg.topic] = payload

    client = mqtt.Client(client_id=client_id)
    client.username_pw_set(MQTT_USER, MQTT_PASS)
    # VIDAA uses TLS on 36669 with a self-signed cert; verification is disabled only
    # for this local, read-only diagnostic connection (no secrets are sent beyond the
    # well-known default creds, and no data is written to the TV).
    import ssl
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    client.tls_set_context(ctx)
    client.on_connect = on_connect
    client.on_message = on_message

    print(f"[mqtt] connecting to {host}:{MQTT_PORT} (read-only)")
    client.connect(host, MQTT_PORT, keepalive=30)
    client.loop_start()

    if pair:
        code = input("Enter the 4-digit PIN shown on the TV: ").strip()
        client.publish(AUTH_CODE_TOPIC % client_id,
                       json.dumps({"authNum": code}))

    time.sleep(timeout)
    client.loop_stop()
    client.disconnect()

    print("\n=== Collected (read-only) ===")
    print(json.dumps({k: _maybe_json(v) for k, v in results.items()}, indent=2))


def _maybe_json(v):
    try:
        return json.loads(v)
    except Exception:
        return v


def main() -> None:
    ap = argparse.ArgumentParser(description="READ-ONLY VIDAA/Hisense TV probe.")
    ap.add_argument("--discover", action="store_true", help="SSDP scan (read-only)")
    ap.add_argument("--host", help="TV IP address")
    ap.add_argument("--connect", action="store_true",
                    help="actually connect (otherwise dry-run)")
    ap.add_argument("--pair", action="store_true",
                    help="one-time PIN pairing (authorizes this client; no TV change)")
    ap.add_argument("--client-id", default=f"vidaa-probe-{uuid.uuid4().hex[:8]}")
    args = ap.parse_args()

    if args.discover:
        print("[ssdp] scanning LAN (read-only) ...")
        for ip in discover():
            print(f"  candidate: {ip}")
        return

    if not args.host:
        ap.error("need --host (or use --discover)")

    if not args.connect:
        print("DRY RUN (no network touched). Re-run with --connect to query.")
        print(f"  would connect read-only to {args.host}:{MQTT_PORT}")
        print("  would query: sourcelist, volume, tvstate (no writes, no keys)")
        return

    run_queries(args.host, args.pair, args.client_id)


if __name__ == "__main__":
    main()
