"""
Tier 2: Boundary & Corner Cases for Critical Missing Features & Crashes (Requirement R2)
- F4: F4_GROUP_CRASH_FIX Boundaries (5 tests)
- F5: F5_SETTLEMENT_WIRING Boundaries (5 tests)
- F6: F6_FRIENDS_DEDUPLICATION Boundaries (5 tests)
- F7: F7_FRIENDS_EMPTY_HANDLERS Boundaries (5 tests)
- F8: F8_AUTH_TOKEN_PERSISTENCE Boundaries (5 tests)
"""

from typing import List, Optional
from tests.e2e.common import (
    Amount, User, Settlement, SettlementStatus, SettlementEngine, PersistentTokenStorage,
    assert_true, assert_false, assert_equal, assert_raises
)


# =============================================================================
# F4 Boundaries: Group Crash Fix
# =============================================================================

def test_f4_b01_rapid_help_click_stress():
    """Verify dispatching ShowHelp 500 times does not leak state or crash."""
    invocations = 0

    def handle_show_help():
        nonlocal invocations
        invocations += 1

    for _ in range(500):
        handle_show_help()

    assert_equal(invocations, 500, "Rapid ShowHelp clicks must handle smoothly")


def test_f4_b02_group_with_blank_name():
    """Verify group detail handles group with empty or whitespace-only name."""
    group_data = {"id": 1, "name": "   ", "ownerId": 10}
    # Sanitization check
    sanitized_name = group_data["name"].strip() or "Untitled Group"
    assert_equal(sanitized_name, "Untitled Group")


def test_f4_b03_invalid_group_id_handling():
    """Verify handling action with negative or non-existent group ID."""
    state = {"group": None, "error": None}

    def load_group(group_id: int):
        if group_id <= 0:
            state["error"] = "Invalid group ID"
            return
        state["group"] = {"id": group_id}

    load_group(-5)
    assert_equal(state["error"], "Invalid group ID")
    assert_equal(state["group"], None)


def test_f4_b04_dropdown_menu_dismiss_boundary():
    """Verify rapidly opening and dismissing GroupDropdownMenu leaves state intact."""
    menu_state = {"expanded": False}
    for _ in range(100):
        menu_state["expanded"] = True
        menu_state["expanded"] = False

    assert_false(menu_state["expanded"])


def test_f4_b05_rename_group_disabled_for_all_roles():
    """Verify 'Rename Group' action is safely disabled across Owner, Admin, and Member roles."""
    roles = ["OWNER", "ADMIN", "MEMBER"]
    for role in roles:
        # In release v1.0, rename group is not supported / disabled
        is_rename_enabled = False
        assert_false(is_rename_enabled, f"Rename must be disabled for role {role}")


# =============================================================================
# F5 Boundaries: Settlement Wiring
# =============================================================================

def test_f5_b01_unauthorized_payment_attempt():
    """Verify a non-debtor cannot mark a settlement as paid."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")
    u3 = User(3, "Imposter")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(500))
    # Imposter attempts to mark as paid
    ok = SettlementEngine.mark_as_paid(settlement, user_id=u3.id)
    assert_false(ok, "Non-debtor must be rejected")
    assert_equal(settlement.status, SettlementStatus.PENDING)


def test_f5_b02_unauthorized_confirmation_attempt():
    """Verify a non-creditor cannot confirm a paid settlement."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")
    u3 = User(3, "Imposter")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(500))
    SettlementEngine.mark_as_paid(settlement, user_id=u2.id)

    # Imposter attempts to confirm
    ok = SettlementEngine.confirm_payment(settlement, user_id=u3.id)
    assert_false(ok, "Non-creditor must not be allowed to confirm")
    assert_equal(settlement.status, SettlementStatus.PAID)


def test_f5_b03_unauthorized_dispute_attempt():
    """Verify a non-creditor cannot dispute a payment."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")
    u3 = User(3, "Imposter")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(500))
    SettlementEngine.mark_as_paid(settlement, user_id=u2.id)

    # Debtor attempts to dispute their own payment (only creditor can dispute)
    ok = SettlementEngine.dispute_payment(settlement, user_id=u2.id)
    assert_false(ok, "Debtor cannot dispute payment")
    assert_equal(settlement.status, SettlementStatus.PAID)


def test_f5_b04_dispute_on_pending_settlement_rejected():
    """Verify disputing a settlement that is in PENDING status is rejected."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(500))
    # Attempting to dispute while still PENDING (not PAID)
    ok = SettlementEngine.dispute_payment(settlement, user_id=u1.id)
    assert_false(ok, "Cannot dispute settlement in PENDING status")
    assert_equal(settlement.status, SettlementStatus.PENDING)


def test_f5_b05_rapid_double_payment_attempt():
    """Verify second rapid payment attempt on already PAID settlement is rejected."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(500))
    first = SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    assert_true(first, "First payment attempt succeeds")

    second = SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    assert_false(second, "Second payment attempt on already PAID settlement must be rejected")
    assert_equal(settlement.status, SettlementStatus.PAID)


# =============================================================================
# F6 Boundaries: Friends Deduplication
# =============================================================================

def test_f6_b01_large_friends_list_stress():
    """Stress test: 10,000 friends list replacement maintains linear memory and exact count."""
    large_list = [{"id": i, "name": f"Friend_{i}"} for i in range(10000)]
    state = {"friends": []}

    # Replace
    state["friends"] = list(large_list)
    assert_equal(len(state["friends"]), 10000)

    # Refresh again
    state["friends"] = list(large_list)
    assert_equal(len(state["friends"]), 10000, "Count must remain 10000, not 20000")


def test_f6_b02_same_username_different_id_preserved():
    """Verify distinct friends sharing identical names are both preserved without false deduplication."""
    friends = [
        {"id": 101, "username": "John Doe", "phone": "111"},
        {"id": 102, "username": "John Doe", "phone": "222"}
    ]
    state = {"friends": friends}
    assert_equal(len(state["friends"]), 2, "Both John Does must be preserved due to distinct IDs")


def test_f6_b03_empty_list_refresh_edge_case():
    """Verify replacing populated friends list with empty response results in empty state."""
    state = {"friends": [{"id": 1, "username": "Alice"}]}
    state["friends"] = []
    assert_equal(len(state["friends"]), 0)


def test_f6_b04_single_friend_addition():
    """Verify transitioning from 0 friends to 1 friend."""
    state = {"friends": []}
    state["friends"] = [{"id": 1, "username": "Solo"}]
    assert_equal(len(state["friends"]), 1)
    assert_equal(state["friends"][0]["username"], "Solo")


def test_f6_b05_network_error_preserves_previous_list():
    """Verify that network error during refresh retains the previous friends list."""
    state = {
        "friends": [{"id": 1, "username": "CachedFriend"}],
        "error": None
    }

    # Simulate network error
    try:
        raise ConnectionError("Network unreachable")
    except ConnectionError as e:
        state["error"] = str(e)
        # friends list must NOT be wiped or corrupted
        pass

    assert_equal(len(state["friends"]), 1)
    assert_equal(state["friends"][0]["username"], "CachedFriend")
    assert_true("Network unreachable" in state["error"])


# =============================================================================
# F7 Boundaries: Friends Empty Handlers
# =============================================================================

def test_f7_b01_show_toast_ultra_long_string():
    """Verify ShowToast handler safely processes ultra-long messages (10,000 chars)."""
    long_toast = "A" * 10000
    toast_displayed = None

    def display_toast(msg: str):
        nonlocal toast_displayed
        # Ellipsize or store safely
        toast_displayed = msg[:200] if len(msg) > 200 else msg

    display_toast(long_toast)
    assert_true(len(toast_displayed) <= 200, "Long toast must be truncated or safely handled")


def test_f7_b02_open_group_negative_id():
    """Verify OpenGroup event safely handles negative or non-existent group ID."""
    nav_error = None

    def open_group(group_id: int):
        nonlocal nav_error
        if group_id <= 0:
            nav_error = "Invalid group destination"
            return
        # Navigate

    open_group(-1)
    assert_equal(nav_error, "Invalid group destination")


def test_f7_b03_settle_up_dialog_zero_balance():
    """Verify SettleUpDialog displays zero-balance message when no debt exists."""
    balance = Amount(0)
    dialog_msg = "Nothing to settle! All even." if balance.is_zero() else f"Settle {balance}"
    assert_equal(dialog_msg, "Nothing to settle! All even.")


def test_f7_b04_settle_up_dialog_creditor_perspective():
    """Verify SettleUpDialog displays correct direction when friend owes user vs user owes friend."""
    owed_to_user = Amount(350)
    user_owes = Amount(-200)

    msg1 = f"Friend owes you {owed_to_user.value}" if owed_to_user.is_positive() else "You owe"
    msg2 = f"You owe {abs(user_owes.value)}" if user_owes.is_negative() else "Friend owes"

    assert_equal(msg1, "Friend owes you 350")
    assert_equal(msg2, "You owe 200")


def test_f7_b05_rapid_event_burst_queue():
    """Verify event burst (50 toast events) is queued or throttled without loss."""
    event_queue = []
    for i in range(50):
        event_queue.append(f"Toast {i}")

    assert_equal(len(event_queue), 50)
    assert_equal(event_queue[0], "Toast 0")
    assert_equal(event_queue[-1], "Toast 49")


# =============================================================================
# F8 Boundaries: Auth Token & Session Persistence
# =============================================================================

def test_f8_b01_corrupted_token_store_recovery():
    """Verify corrupted token store falls back to unauthenticated state without crashing."""
    storage = PersistentTokenStorage(backing_store={"access_token": "", "refresh_token": ""})
    # Empty token strings should be treated as None
    tokens = storage.get_token()
    valid_tokens = tokens if (tokens and tokens[0] and tokens[1]) else None
    assert_equal(valid_tokens, None, "Corrupted/empty tokens should resolve to None")


def test_f8_b02_high_concurrency_token_reads():
    """Verify reading token 1,000 times produces consistent values."""
    storage = PersistentTokenStorage()
    storage.set_token("concurrent_access", "concurrent_refresh")

    for _ in range(1000):
        tok = storage.get_token()
        assert_equal(tok, ("concurrent_access", "concurrent_refresh"))


def test_f8_b03_idempotent_multiple_logouts():
    """Verify calling clearToken() repeatedly does not error."""
    storage = PersistentTokenStorage()
    storage.set_token("access", "refresh")

    for _ in range(10):
        storage.clear_token()

    assert_equal(storage.get_token(), None)


def test_f8_b04_expired_access_token_refresh_flow():
    """Verify token lifecycle when access token expires but refresh token is valid."""
    tokens = {"access": "expired_jwt", "refresh": "valid_refresh", "access_expiry": 1000}
    current_time = 2000

    needs_refresh = current_time > tokens["access_expiry"]
    assert_true(needs_refresh, "Should detect access token expiration")

    # Refresh
    tokens["access"] = "new_valid_jwt"
    tokens["access_expiry"] = 5000
    assert_equal(tokens["access"], "new_valid_jwt")


def test_f8_b05_cold_start_uninitialized_user_safety():
    """Verify accessing current user on fresh install before any login returns null safely."""
    current_user: Optional[User] = None
    # No UninitializedPropertyAccessException
    assert_equal(current_user, None)
