#!/usr/bin/env python3
"""
Version bumping script for ExpenseShare multiplatform project.
Updates Android, Desktop (JVM), iOS, and Server version declarations.
"""

import sys
import re
import argparse
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[4]

ANDROID_CONVENTION = REPO_ROOT / "build-logic/convention/src/main/kotlin/org/milad/expense_share/plugins/AndroidApplicationConventionPlugin.kt"
DESKTOP_BUILD = REPO_ROOT / "composeApp/build.gradle.kts"
SERVER_BUILD = REPO_ROOT / "server/build.gradle.kts"
IOS_CONFIG = REPO_ROOT / "iosApp/Configuration/Config.xcconfig"

def read_file(path: Path) -> str:
    if not path.is_file():
        raise FileNotFoundError(f"File not found: {path}")
    return path.read_text(encoding="utf-8")

def write_file(path: Path, content: str):
    path.write_text(content, encoding="utf-8")

def get_current_versions():
    # Read Android
    android_content = read_file(ANDROID_CONVENTION)
    vc_match = re.search(r'versionCode\s*=\s*(\d+)', android_content)
    vn_match = re.search(r'versionName\s*=\s*"([^"]+)"', android_content)
    
    current_code = int(vc_match.group(1)) if vc_match else 1
    current_name = vn_match.group(1) if vn_match else "1.0.0"
    
    return current_name, current_code

def bump(new_version: str, new_code: int, dry_run: bool = False):
    print(f"Bumping ExpenseShare to version: {new_version} (build {new_code})")
    
    # 1. Android
    android_content = read_file(ANDROID_CONVENTION)
    new_android = re.sub(r'(versionCode\s*=\s*)\d+', rf'\g<1>{new_code}', android_content)
    new_android = re.sub(r'(versionName\s*=\s*)"[^"]+"', rf'\g<1>"{new_version}"', new_android)
    if not dry_run:
        write_file(ANDROID_CONVENTION, new_android)
    print(f"  [x] Android: versionCode={new_code}, versionName=\"{new_version}\"")

    # 2. Desktop
    desktop_content = read_file(DESKTOP_BUILD)
    new_desktop = re.sub(r'(packageVersion\s*=\s*)"[^"]+"', rf'\g<1>"{new_version}"', desktop_content)
    if not dry_run:
        write_file(DESKTOP_BUILD, new_desktop)
    print(f"  [x] Desktop: packageVersion=\"{new_version}\"")

    # 3. Server
    server_content = read_file(SERVER_BUILD)
    new_server = re.sub(r'(^version\s*=\s*)"[^"]+"', rf'\g<1>"{new_version}"', server_content, flags=re.MULTILINE)
    if not dry_run:
        write_file(SERVER_BUILD, new_server)
    print(f"  [x] Server: version=\"{new_version}\"")

    # 4. iOS
    if IOS_CONFIG.is_file():
        ios_content = read_file(IOS_CONFIG)
        new_ios = re.sub(r'(CURRENT_PROJECT_VERSION\s*=\s*)\d+', rf'\g<1>{new_code}', ios_content)
        new_ios = re.sub(r'(MARKETING_VERSION\s*=\s*)[^\n\r]+', rf'\g<1>{new_version}', new_ios)
        if not dry_run:
            write_file(IOS_CONFIG, new_ios)
        print(f"  [x] iOS: CURRENT_PROJECT_VERSION={new_code}, MARKETING_VERSION={new_version}")

    if dry_run:
        print("\n(Dry-run mode: no files were modified)")

def main():
    parser = argparse.ArgumentParser(description="Bump ExpenseShare versions across all targets.")
    parser.add_argument("version", nargs="?", help="New semantic version (e.g. 1.0.2)")
    parser.add_argument("--build", type=int, help="New build/version code (integer). Defaults to current + 1")
    parser.add_argument("--dry-run", action="store_true", help="Preview version changes without writing to disk")
    args = parser.parse_args()

    curr_name, curr_code = get_current_versions()
    print(f"Current version: {curr_name} (build {curr_code})")

    if not args.version:
        print("Please provide a new version, e.g.: python3 bump_version.py 1.0.2")
        sys.exit(1)

    target_code = args.build if args.build is not None else curr_code + 1
    bump(args.version, target_code, dry_run=args.dry_run)

if __name__ == "__main__":
    main()
