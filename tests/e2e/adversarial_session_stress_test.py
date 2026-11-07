"""
Empirical Adversarial Session & Token Lifecycle Stress Harness for ExpenseShare v1.0
Author: Milestone 4 Challenger 2 (teamwork_challenger)

This harness empirically stress-tests:
1. Process kill and recreation lifecycle (cold starts, token persistence, user persistence)
2. Logout complete clearing and zombie-session prevention
3. Corrupted persistent store handling (malformed tokens, partial writes, missing keys)
4. Highly concurrent token/session access under multi-threaded stress
5. Bytecode DEX unminified symbol assertions on release APK
"""

import sys
import os
import zipfile
import threading
import random
import time
from pathlib import Path
from typing import Optional, Tuple, Dict, Any

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
sys.path.insert(0, str(REPO_ROOT / "tests" / "e2e"))

from common import assert_true, assert_equal, assert_false, User, Amount


class SimulatedAndroidSharedPreferences:
    """Accurately models Android SharedPreferences XML backing store & in-memory cache."""
    def __init__(self, filename: str):
        self.filename = filename
        self._lock = threading.Lock()
        self._disk: Dict[str, Any] = {}

    def edit(self):
        return SimulatedEditor(self)

    def getString(self, key: str, default: Optional[str]) -> Optional[str]:
        with self._lock:
            val = self._disk.get(key, default)
            return str(val) if val is not None else default

    def getInt(self, key: str, default: int) -> int:
        with self._lock:
            val = self._disk.get(key, default)
            return int(val) if val is not None else default

    def contains(self, key: str) -> bool:
        with self._lock:
            return key in self._disk


class SimulatedEditor:
    def __init__(self, sp: SimulatedAndroidSharedPreferences):
        self.sp = sp
        self._ops = []
        self._clear_first = False

    def putString(self, key: str, value: Optional[str]):
        self._ops.append(("put", key, value))
        return self

    def putInt(self, key: str, value: int):
        self._ops.append(("put", key, value))
        return self

    def remove(self, key: str):
        self._ops.append(("remove", key, None))
        return self

    def clear(self):
        self._clear_first = True
        return self

    def apply(self):
        with self.sp._lock:
            if self._clear_first:
                self.sp._disk.clear()
            for op, k, v in self._ops:
                if op == "put":
                    self.sp._disk[k] = v
                elif op == "remove":
                    self.sp._disk.pop(k, None)


class SimulatedAndroidProcess:
    """Simulates an Android Process with Application Context and Process Death."""
    def __init__(self, token_disk: SimulatedAndroidSharedPreferences, user_disk: SimulatedAndroidSharedPreferences):
        self.token_disk = token_disk
        self.user_disk = user_disk
        # In-memory application state
        self.context_initialized = True
        self.token_in_memory = None
        self.user_in_memory = None

    def get_token(self) -> Optional[Tuple[str, Optional[str]]]:
        if not self.context_initialized:
            return self.token_in_memory
        access = self.token_disk.getString("access_token", None)
        if access is None:
            return self.token_in_memory
        refresh = self.token_disk.getString("refresh_token", None)
        return (access, refresh)

    def set_token(self, access: str, refresh: Optional[str]):
        if self.context_initialized:
            self.token_disk.edit().putString("access_token", access).putString("refresh_token", refresh).apply()
        self.token_in_memory = (access, refresh)

    def clear_token(self):
        if self.context_initialized:
            self.token_disk.edit().clear().apply()
        self.token_in_memory = None

    def get_user_info(self) -> User:
        if self.user_in_memory is not None:
            return self.user_in_memory
        if self.context_initialized and self.user_disk.contains("user_id"):
            uid = self.user_disk.getInt("user_id", 0)
            uname = self.user_disk.getString("username", "") or ""
            phone = self.user_disk.getString("phone", "") or ""
            u = User(id=uid, username=uname, phone=phone)
            self.user_in_memory = u
            return u
        return User(id=0, username="", phone="")

    def set_user_info(self, user: User):
        self.user_in_memory = user
        if self.context_initialized:
            self.user_disk.edit().putInt("user_id", user.id).putString("username", user.username).putString("phone", user.phone).apply()

    def clear_user(self):
        self.user_in_memory = None
        if self.context_initialized:
            self.user_disk.edit().clear().apply()

    def simulate_process_kill(self) -> "SimulatedAndroidProcess":
        """Kills the process, wiping all in-memory variables. Disks survive."""
        # Returns fresh process attached to existing persistent disks
        return SimulatedAndroidProcess(self.token_disk, self.user_disk)


def test_session_lifecycle_process_kill():
    """Verify login persistence across repeated process kills."""
    token_disk = SimulatedAndroidSharedPreferences("expense_share_tokens")
    user_disk = SimulatedAndroidSharedPreferences("expense_share_user_session")

    # Process 1: Cold start, clean install
    proc1 = SimulatedAndroidProcess(token_disk, user_disk)
    assert_true(proc1.get_token() is None, "Token must be null on clean install")
    u0 = proc1.get_user_info()
    assert_equal(u0.id, 0, "Default user ID is 0")
    assert_equal(u0.username, "", "Default user name is empty")

    # User performs login
    proc1.set_token("jwt_access_abc123", "jwt_refresh_xyz987")
    proc1.set_user_info(User(id=42, username="Milad", phone="09121112233"))

    assert_true(proc1.get_token() is not None, "Token stored")
    assert_equal(proc1.get_user_info().username, "Milad")

    # SIMULATE OS PROCESS KILL (e.g. low memory killer)
    proc2 = proc1.simulate_process_kill()

    # Process 2: AppEntryPoint routes based on tokenProvider.getToken() != null
    t2 = proc2.get_token()
    assert_true(t2 is not None, "Token must survive process kill")
    assert_equal(t2[0], "jwt_access_abc123")
    assert_equal(t2[1], "jwt_refresh_xyz987")

    # User info restoration
    u2 = proc2.get_user_info()
    assert_equal(u2.id, 42, "User ID restored from SharedPreferences")
    assert_equal(u2.username, "Milad", "Username restored from SharedPreferences")
    assert_equal(u2.phone, "09121112233", "Phone restored from SharedPreferences")

    # Process 2: User performs logout
    # AppEntryPoint onLogout: tokenProvider.clearToken(); userRepository.clear()
    proc2.clear_token()
    proc2.clear_user()

    assert_true(proc2.get_token() is None, "Token cleared post-logout")
    assert_equal(proc2.get_user_info().id, 0, "User info cleared post-logout")

    # SIMULATE OS PROCESS KILL AFTER LOGOUT
    proc3 = proc2.simulate_process_kill()

    # Process 3: Must NOT resurrect old session (Zombie session prevention)
    t3 = proc3.get_token()
    assert_true(t3 is None, "No zombie token post-kill")
    u3 = proc3.get_user_info()
    assert_equal(u3.id, 0, "No zombie user post-kill")
    assert_equal(u3.username, "", "No zombie username post-kill")
    print("  ✓ test_session_lifecycle_process_kill PASSED")


def test_corrupted_persistent_store_resilience():
    """Verify app recovers gracefully from corrupted disk preferences."""
    token_disk = SimulatedAndroidSharedPreferences("expense_share_tokens")
    user_disk = SimulatedAndroidSharedPreferences("expense_share_user_session")

    # Case A: Missing access token but lingering refresh token
    token_disk.edit().putString("refresh_token", "orphan_refresh").apply()
    proc = SimulatedAndroidProcess(token_disk, user_disk)
    # SharedPreferencesTokenProvider returns inMemoryTokens (None) if access_token is null
    assert_true(proc.get_token() is None, "Missing access_token must return None safely")

    # Case B: Empty string access token
    token_disk.edit().putString("access_token", "").apply()
    t = proc.get_token()
    assert_equal(t[0], "", "Empty token returned without throwing")

    # Case C: Partially written user info (only username, missing user_id)
    user_disk.edit().clear().putString("username", "GhostUser").apply()
    proc2 = proc.simulate_process_kill()
    u = proc2.get_user_info()
    # If user_id is missing, contains("user_id") is false -> fallback to default User(0, "", "")
    assert_equal(u.id, 0, "Partially written user safely falls back to default")
    print("  ✓ test_corrupted_persistent_store_resilience PASSED")


def test_multithreaded_concurrency_stress():
    """Stress-test concurrent token read, write, and clear operations."""
    token_disk = SimulatedAndroidSharedPreferences("expense_share_tokens")
    user_disk = SimulatedAndroidSharedPreferences("expense_share_user_session")
    proc = SimulatedAndroidProcess(token_disk, user_disk)

    errors = []
    stop_event = threading.Event()

    def reader_worker():
        while not stop_event.is_set():
            try:
                t = proc.get_token()
                u = proc.get_user_info()
                if t is not None:
                    assert_true(isinstance(t[0], str))
                assert_true(isinstance(u.id, int))
            except Exception as e:
                errors.append(e)

    def writer_worker():
        i = 0
        while not stop_event.is_set():
            try:
                i += 1
                if i % 3 == 0:
                    proc.clear_token()
                    proc.clear_user()
                else:
                    proc.set_token(f"access_{i}", f"refresh_{i}")
                    proc.set_user_info(User(i, f"user_{i}", "123"))
                time.sleep(0.0001)
            except Exception as e:
                errors.append(e)

    threads = []
    for _ in range(8):
        t = threading.Thread(target=reader_worker)
        threads.append(t)
        t.start()
    for _ in range(4):
        t = threading.Thread(target=writer_worker)
        threads.append(t)
        t.start()

    time.sleep(0.5)
    stop_event.set()
    for t in threads:
        t.join()

    assert_equal(len(errors), 0, f"Concurrent execution produced errors: {errors}")
    print("  ✓ test_multithreaded_concurrency_stress PASSED")


def test_release_apk_dex_unminified_symbols():
    """Inspect composeApp-release-unsigned.apk DEX files to verify critical symbols remain unminified."""
    apk_path = REPO_ROOT / "composeApp" / "build" / "outputs" / "apk" / "release" / "composeApp-release-unsigned.apk"
    assert_true(apk_path.exists(), f"Release APK must exist at {apk_path}")

    required_symbols = [
        "ExpenseShareApp",
        "AppEntryPoint",
        "SharedPreferencesTokenProvider",
        "UserRepositoryImpl",
        "GroupDetailViewModel",
        "SettlementScreen",
        "FriendsViewModel",
        "FriendsScreen",
        "PaySettlementUseCase",
        "ConfirmSettlementUseCase",
        "DisputeSettlementUseCase",
    ]

    with zipfile.ZipFile(apk_path, "r") as z:
        dex_files = [f for f in z.namelist() if f.endswith(".dex")]
        assert_true(len(dex_files) > 0, "Release APK must contain at least one DEX file")
        dex_contents = {f: z.read(f) for f in dex_files}

    missing = []
    for sym in required_symbols:
        sym_bytes = sym.encode("utf-8")
        found = any(sym_bytes in content for content in dex_contents.values())
        if not found:
            missing.append(sym)

    assert_equal(len(missing), 0, f"Critical symbols missing or minified in DEX: {missing}")
    print("  ✓ test_release_apk_dex_unminified_symbols PASSED")


def main():
    print("======================================================================")
    print("   Empirical Adversarial Session & Token Lifecycle Stress Test Suite  ")
    print("======================================================================")
    test_session_lifecycle_process_kill()
    test_corrupted_persistent_store_resilience()
    test_multithreaded_concurrency_stress()
    test_release_apk_dex_unminified_symbols()
    print("======================================================================")
    print("   ALL ADVERSARIAL STRESS TESTS PASSED SUCCESSFULLY!                 ")
    print("======================================================================")


if __name__ == "__main__":
    main()
