#!/usr/bin/env python3
"""
Post-release health and artifact verification script for ExpenseShare.
"""

import sys
import json
import urllib.request
import subprocess
import argparse

SERVER_URL = "https://expenseshare-server.liara.run/"
WEB_URL = "https://cloner93.github.io/ExpenseShare/"

def check_url(url: str, expected_codes=(200, 404)) -> bool:
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=10) as resp:
            code = resp.getcode()
    except urllib.error.HTTPError as e:
        code = e.code
    except Exception as e:
        print(f"  [-] Error connecting to {url}: {e}")
        return False

    if code in expected_codes:
        print(f"  [+] {url} responded with HTTP {code} (OK)")
        return True
    else:
        print(f"  [-] {url} unexpected HTTP {code}")
        return False

def check_github_release(tag: str):
    tag_name = tag if tag.startswith("v") else f"v{tag}"
    print(f"Checking GitHub Release for tag '{tag_name}'...")
    try:
        result = subprocess.run(
            ["gh", "release", "view", tag_name, "--json", "url,assets"],
            capture_output=True,
            text=True,
            check=True
        )
        data = json.loads(result.stdout)
        print(f"  [+] Release URL: {data.get('url')}")
        assets = data.get("assets", [])
        if not assets:
            print("  [-] No assets found attached to the release.")
            return False
        
        print(f"  [+] Found {len(assets)} release assets:")
        for a in assets:
            size_mb = a.get("size", 0) / (1024 * 1024)
            print(f"      - {a.get('name')} ({size_mb:.2f} MB)")
        return True
    except subprocess.CalledProcessError as e:
        print(f"  [-] Failed to query release via gh CLI: {e.stderr.strip()}")
        return False
    except FileNotFoundError:
        print("  [-] gh CLI is not available in PATH.")
        return False

def main():
    parser = argparse.ArgumentParser(description="Verify ExpenseShare release health and artifacts.")
    parser.add_argument("--tag", help="Release tag to inspect (e.g. v1.0.1)")
    args = parser.parse_args()

    print("Checking live endpoints:")
    server_ok = check_url(SERVER_URL, expected_codes=(200, 404))
    web_ok = check_url(WEB_URL, expected_codes=(200,))

    release_ok = True
    if args.tag:
        release_ok = check_github_release(args.tag)

    if server_ok and web_ok and release_ok:
        print("\nAll health checks passed!")
    else:
        print("\nSome checks had warnings or failures.")

if __name__ == "__main__":
    main()
