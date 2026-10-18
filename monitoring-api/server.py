#!/usr/bin/env python3
"""
MLHK Central Monitoring API (Python 3 implementation)
Lightweight service health monitoring for MLHK TV+ Launcher.
Uses only Python standard library.
"""

import json
import os
import socket
import sys
import threading
import time
from datetime import datetime, timezone
from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.request
import urllib.error
import ssl

CONFIG_FILE = os.path.join(os.path.dirname(__file__), "config.json")

def load_config():
    try:
        with open(CONFIG_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"Error loading config: {e}")
        return {
            "server": {"port": 8080, "api_key": "mlhk-secure-key", "check_interval_seconds": 30},
            "services": []
        }

config = load_config()

cached_status = {
    "status": "grey",
    "message": "Initialiseren...",
    "timestamp": datetime.now(timezone.utc).isoformat(),
    "services": []
}

def check_http_service(svc):
    start = time.time()
    url = svc.get("url")
    timeout = svc.get("timeout_ms", 3500) / 1000.0
    expected = svc.get("expected_status", [200])
    reject_unauth = svc.get("reject_unauthorized", True)

    ctx = None
    if not reject_unauth:
        ctx = ssl.create_default_context()
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE

    req = urllib.request.Request(
        url,
        headers={"User-Agent": "MLHK-Monitor/1.0", **(svc.get("headers", {}))},
        method=svc.get("method", "GET")
    )

    try:
        with urllib.request.urlopen(req, timeout=timeout, context=ctx) as res:
            latency = int((time.time() - start) * 1000)
            code = res.getcode()
            is_ok = code in expected
            return {
                "id": svc["id"],
                "name": svc["name"],
                "status": "up" if is_ok else "degraded",
                "latencyMs": latency,
                "lastCheck": datetime.now(timezone.utc).isoformat(),
                "message": f"HTTP {code}"
            }
    except urllib.error.HTTPError as e:
        latency = int((time.time() - start) * 1000)
        is_ok = e.code in expected
        return {
            "id": svc["id"],
            "name": svc["name"],
            "status": "up" if is_ok else ("down" if e.code >= 500 else "degraded"),
            "latencyMs": latency,
            "lastCheck": datetime.now(timezone.utc).isoformat(),
            "message": f"HTTP {e.code}"
        }
    except Exception as e:
        latency = int((time.time() - start) * 1000)
        return {
            "id": svc["id"],
            "name": svc["name"],
            "status": "down",
            "latencyMs": latency,
            "lastCheck": datetime.now(timezone.utc).isoformat(),
            "message": str(e)
        }

def check_tcp_service(svc):
    start = time.time()
    host = svc.get("host")
    port = svc.get("port")
    timeout = svc.get("timeout_ms", 2500) / 1000.0

    try:
        with socket.create_connection((host, port), timeout=timeout):
            latency = int((time.time() - start) * 1000)
            return {
                "id": svc["id"],
                "name": svc["name"],
                "status": "up",
                "latencyMs": latency,
                "lastCheck": datetime.now(timezone.utc).isoformat(),
                "message": "TCP Open"
            }
    except Exception as e:
        latency = int((time.time() - start) * 1000)
        return {
            "id": svc["id"],
            "name": svc["name"],
            "status": "down",
            "latencyMs": latency,
            "lastCheck": datetime.now(timezone.utc).isoformat(),
            "message": str(e)
        }

def run_health_checks():
    global cached_status, config
    config = load_config()
    services = config.get("services", [])
    results = []

    for svc in services:
        if svc.get("type") == "tcp":
            results.append(check_tcp_service(svc))
        else:
            results.append(check_http_service(svc))

    critical_down = False
    any_issues = False

    for res in results:
        svc_cfg = next((s for s in services if s["id"] == res["id"]), {})
        is_crit = svc_cfg.get("critical", True)
        if res["status"] == "down":
            any_issues = True
            if is_crit:
                critical_down = True
        elif res["status"] == "degraded":
            any_issues = True

    overall = "green"
    msg = "Alles online"
    if not results:
        overall = "grey"
        msg = "Geen diensten"
    elif critical_down:
        overall = "red"
        msg = "Kritieke dienst offline"
    elif any_issues:
        overall = "orange"
        msg = "Aandacht vereist"

    cached_status = {
        "status": overall,
        "message": msg,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "services": results
    }
    print(f"[{cached_status['timestamp']}] Python Monitor: {overall.upper()} - {msg}")

def monitor_loop():
    while True:
        try:
            run_health_checks()
        except Exception as e:
            print(f"Monitor error: {e}")
        interval = config.get("server", {}).get("check_interval_seconds", 30)
        time.sleep(interval)

class StatusHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        auth = self.headers.get("Authorization", "").replace("Bearer ", "").strip()
        api_key = self.headers.get("X-API-Key", "").strip()
        expected_key = config.get("server", {}).get("api_key")

        if self.path.startswith("/api/status") or self.path.startswith("/status"):
            if expected_key and auth != expected_key and api_key != expected_key:
                self.send_response(401)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(b'{"error": "Unauthorized"}')
                return

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(json.dumps(cached_status, indent=2).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

def main():
    t = threading.Thread(target=monitor_loop, daemon=True)
    t.start()
    port = config.get("server", {}).get("port", 8080)
    server = HTTPServer(("0.0.0.0", port), StatusHandler)
    print(f"MLHK Python Monitoring API running on port {port}")
    server.serve_forever()

if __name__ == "__main__":
    main()
