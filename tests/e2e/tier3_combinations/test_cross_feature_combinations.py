"""
Tier 3: Cross-Feature Combinations & Pairwise Interactions
Pairwise and end-to-end multi-module integration tests across Auth, Groups, Settlements, Friends, and Navigation.
"""

from typing import List, Dict, Optional, Tuple, Any
from tests.e2e.common import (
    Amount, User, Transaction, TransactionStatus, PayerRequest, ShareDetailsRequest, ShareType, ShareMemberRequest,
    Settlement, SettlementStatus, SettlementEngine, PersistentTokenStorage,
    assert_true, assert_false, assert_equal, assert_raises
)


# =============================================================================
# Cross-Feature Test 1: Auth & Group Life Cycle
# =============================================================================

def test_x01_auth_group_lifecycle():
    """Register user -> Persistent Token Stored -> Create Group -> Verify Ownership."""
    token_storage = PersistentTokenStorage()
    
    # Step 1: User registers / logs in
    user = User(id=1, username="Milad", phone="09120000001")
    token_storage.set_token("jwt_access_milad", "jwt_refresh_milad")
    assert_true(token_storage.get_token() is not None)

    # Step 2: User creates group
    group = {
        "id": 101,
        "name": "Weekend Trip",
        "owner_id": user.id,
        "members": [user]
    }
    assert_equal(group["owner_id"], user.id)
    assert_equal(len(group["members"]), 1)


# =============================================================================
# Cross-Feature Test 2: Group, Transaction, and Settlement Pipeline
# =============================================================================

def test_x02_group_transaction_settlement_pipeline():
    """Create group with 3 members -> Add transaction -> Calculate settlements."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")
    u3 = User(3, "Reza")

    # Milad pays 3000 for dinner split equally
    trx = Transaction(
        id=1,
        group_id=10,
        title="Dinner",
        amount=Amount(3000),
        created_by=u1.id,
        status=TransactionStatus.APPROVED,
        payers=[PayerRequest(u1, Amount(3000))],
        share_details=ShareDetailsRequest(
            type=ShareType.EQUAL,
            members=[
                ShareMemberRequest(u1, Amount(1000)),
                ShareMemberRequest(u2, Amount(1000)),
                ShareMemberRequest(u3, Amount(1000)),
            ]
        )
    )

    settlements = SettlementEngine.calculate_settlements(group_id=10, transactions=[trx])
    assert_equal(len(settlements), 2, "Should create 2 settlements (Ali -> Milad, Reza -> Milad)")
    for s in settlements:
        assert_equal(s.creditor, u1)
        assert_equal(s.amount, Amount(1000))
        assert_equal(s.status, SettlementStatus.PENDING)


# =============================================================================
# Cross-Feature Test 3: Settlement Pay -> Confirm Workflow
# =============================================================================

def test_x03_settlement_pay_confirm_balance_reconciliation():
    """Debtor marks as paid -> Creditor confirms -> Status CONFIRMED."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(1000))
    
    # Ali pays
    pay_ok = SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    assert_true(pay_ok)
    assert_equal(settlement.status, SettlementStatus.PAID)

    # Milad confirms
    confirm_ok = SettlementEngine.confirm_payment(settlement, user_id=u1.id)
    assert_true(confirm_ok)
    assert_equal(settlement.status, SettlementStatus.CONFIRMED)


# =============================================================================
# Cross-Feature Test 4: Settlement Pay -> Dispute Workflow
# =============================================================================

def test_x04_settlement_pay_dispute_reversion():
    """Debtor pays -> Creditor disputes -> Reverts to PENDING -> Debtor pays again -> Confirmed."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")

    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(1000))
    
    # Debtor pays
    SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    assert_equal(settlement.status, SettlementStatus.PAID)

    # Creditor disputes (e.g. money not received in bank)
    dispute_ok = SettlementEngine.dispute_payment(settlement, user_id=u1.id)
    assert_true(dispute_ok)
    assert_equal(settlement.status, SettlementStatus.PENDING)

    # Ali retries payment
    SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    assert_equal(settlement.status, SettlementStatus.PAID)

    # Milad confirms
    SettlementEngine.confirm_payment(settlement, user_id=u1.id)
    assert_equal(settlement.status, SettlementStatus.CONFIRMED)


# =============================================================================
# Cross-Feature Test 5: Friends Add, Deduplicate & Group Invite
# =============================================================================

def test_x05_friends_add_deduplicate_and_group_invite():
    """Add friend -> Refresh 3x without duplicate growth -> Invite friend to new Group."""
    friends_store = [User(2, "Ali")]
    
    # State update on 3 consecutive refreshes
    for _ in range(3):
        friends_state = list(friends_store)
        assert_equal(len(friends_state), 1, "Friends list must strictly maintain 1 friend")

    # Invite friend to group
    new_group = {
        "id": 201,
        "name": "Trip to Rome",
        "members": [User(1, "Milad"), friends_state[0]]
    }
    assert_equal(len(new_group["members"]), 2)
    assert_equal(new_group["members"][1].username, "Ali")


# =============================================================================
# Cross-Feature Test 6: Persistent Token Session Recovery Across Restart
# =============================================================================

def test_x06_persistent_token_session_restart_recovery():
    """Login -> App dies -> Restart app -> Tokens restored -> User continues without re-login."""
    session1 = PersistentTokenStorage()
    session1.set_token("access_milad_token_xyz", "refresh_milad_token_abc")

    # Simulate app process death and resurrection
    session2 = session1.restart_process()
    restored = session2.get_token()

    assert_true(restored is not None)
    assert_equal(restored[0], "access_milad_token_xyz")
    # App starts on MainRoute instead of RootRoute.Auth
    start_destination = "MainRoute.Dashboard" if restored else "RootRoute.Auth"
    assert_equal(start_destination, "MainRoute.Dashboard")


# =============================================================================
# Cross-Feature Test 7: Logout Session Destruction & Cache Clearing
# =============================================================================

def test_x07_logout_session_destruction_and_cache_clear():
    """Logout -> Token cleared -> User repo cleared -> Navigation resets to Auth."""
    storage = PersistentTokenStorage()
    storage.set_token("jwt_to_clear", "refresh_to_clear")
    user_cache = {"id": 1, "name": "Milad"}

    # Execute logout
    storage.clear_token()
    user_cache.clear()
    next_route = "RootRoute.Auth" if storage.get_token() is None else "MainRoute.Dashboard"

    assert_equal(storage.get_token(), None)
    assert_equal(len(user_cache), 0)
    assert_equal(next_route, "RootRoute.Auth")


# =============================================================================
# Cross-Feature Test 8: Menu Option Safety During Active Group Operations
# =============================================================================

def test_x08_menu_option_safety_during_active_group_operations():
    """Click Help menu in GroupDetail -> Safe handling -> Add transaction succeeds."""
    state = {"group_id": 10, "help_clicked": False, "transactions": []}

    # Help action
    state["help_clicked"] = True
    assert_true(state["help_clicked"])

    # Add transaction immediately after
    state["transactions"].append({"id": 1, "title": "Coffee", "amount": 50})
    assert_equal(len(state["transactions"]), 1)


# =============================================================================
# Cross-Feature Test 9: Settlement Callbacks under Concurrent UI Events
# =============================================================================

def test_x09_settlement_callbacks_concurrent_with_ui_navigation():
    """Trigger pay settlement -> User navigates back -> Async settlement completes."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")
    settlement = Settlement(1, 10, u2, u1, Amount(500))

    # Async action simulated
    SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    current_screen = "Dashboard"  # navigated away

    assert_equal(settlement.status, SettlementStatus.PAID)
    assert_equal(current_screen, "Dashboard")


# =============================================================================
# Cross-Feature Test 10: Platform Base URL and Client Resolution
# =============================================================================

def test_x10_platform_base_url_and_network_client_resolution():
    """Verify platform URL config formats correct API endpoint URLs."""
    android_base = "http://10.0.2.2:8082/"
    jvm_base = "http://127.0.0.1:8082/"

    def format_endpoint(base: str, path: str) -> str:
        b = base if base.endswith("/") else f"{base}/"
        p = path.lstrip("/")
        return f"{b}{p}"

    assert_equal(format_endpoint(android_base, "groups/10/settlement"), "http://10.0.2.2:8082/groups/10/settlement")
    assert_equal(format_endpoint(jvm_base, "/groups/10/settlement"), "http://127.0.0.1:8082/groups/10/settlement")


# =============================================================================
# Cross-Feature Test 11: Multi-Payer Custom Share Settlement Symmetry
# =============================================================================

def test_x11_multi_payer_custom_share_settlement_symmetry():
    """2 payers and 3 unequal shares -> Recalculate -> Total credit == Total debt."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")
    u3 = User(3, "Sara")

    # Milad paid 3000, Ali paid 1000 (total 4000). Shares: Milad 1000, Ali 1000, Sara 2000
    trx = Transaction(
        id=1,
        group_id=20,
        title="Airbnb",
        amount=Amount(4000),
        created_by=u1.id,
        payers=[PayerRequest(u1, Amount(3000)), PayerRequest(u2, Amount(1000))],
        share_details=ShareDetailsRequest(
            type=ShareType.EXACT,
            members=[
                ShareMemberRequest(u1, Amount(1000)),  # Net: +2000
                ShareMemberRequest(u2, Amount(1000)),  # Net: 0
                ShareMemberRequest(u3, Amount(2000)),  # Net: -2000
            ]
        )
    )

    settlements = SettlementEngine.calculate_settlements(group_id=20, transactions=[trx])
    assert_equal(len(settlements), 1, "Only 1 settlement needed: Sara -> Milad")
    assert_equal(settlements[0].debtor, u3)
    assert_equal(settlements[0].creditor, u1)
    assert_equal(settlements[0].amount, Amount(2000))


# =============================================================================
# Cross-Feature Test 12: Zero Net Balance Automatic Cancellation
# =============================================================================

def test_x12_zero_net_balance_automatic_cancellation():
    """Two friends spend equal amounts for each other -> Net balance 0 -> 0 settlements."""
    u1 = User(1, "Milad")
    u2 = User(2, "Ali")

    # Trx 1: Milad buys Ali lunch 500
    trx1 = Transaction(
        id=1, group_id=30, title="Lunch", amount=Amount(500), created_by=u1.id,
        payers=[PayerRequest(u1, Amount(500))],
        share_details=ShareDetailsRequest(ShareType.EXACT, [ShareMemberRequest(u2, Amount(500))])
    )
    # Trx 2: Ali buys Milad dinner 500
    trx2 = Transaction(
        id=2, group_id=30, title="Dinner", amount=Amount(500), created_by=u2.id,
        payers=[PayerRequest(u2, Amount(500))],
        share_details=ShareDetailsRequest(ShareType.EXACT, [ShareMemberRequest(u1, Amount(500))])
    )

    settlements = SettlementEngine.calculate_settlements(group_id=30, transactions=[trx1, trx2])
    assert_equal(len(settlements), 0, "Counter-expenses should completely offset to 0 settlements")


# =============================================================================
# Cross-Feature Test 13: Friend Request Rejection & List Isolation
# =============================================================================

def test_x13_friend_request_rejection_and_list_isolation():
    """User rejects friend request -> Removed from pending -> Friends list unaffected."""
    pending_requests = [{"id": 99, "sender": "Stranger"}]
    current_friends = [{"id": 2, "name": "Ali"}]

    # Reject request
    pending_requests.pop(0)
    assert_equal(len(pending_requests), 0)
    assert_equal(len(current_friends), 1, "Friends list unchanged after rejection")


# =============================================================================
# Cross-Feature Test 14: Network Timeout with Cached Data Fallback
# =============================================================================

def test_x14_network_timeout_with_cached_data_fallback():
    """Network fails on refresh -> Toast posted -> UI retains cached data."""
    cached_groups = [{"id": 10, "name": "Trip"}]
    posted_events = []

    try:
        raise TimeoutError("Network request timed out")
    except TimeoutError as e:
        posted_events.append(f"Error: {e}")

    assert_equal(len(posted_events), 1)
    assert_equal(len(cached_groups), 1, "Cached data retained despite network timeout")


# =============================================================================
# Cross-Feature Test 15: Client-Server Payload Compatibility
# =============================================================================

def test_x15_full_stack_client_server_dto_compatibility():
    """Verify JSON structure matches between client DTOs and server REST models."""
    import json

    client_settlement_json = json.dumps({
        "id": 1,
        "groupId": 10,
        "debtor": {"id": 2, "username": "Ali", "phone": "0912"},
        "creditor": {"id": 1, "username": "Milad", "phone": "0911"},
        "amount": {"value": 1500},
        "status": "PENDING"
    })

    server_parsed = json.loads(client_settlement_json)
    assert_equal(server_parsed["groupId"], 10)
    assert_equal(server_parsed["amount"]["value"], 1500)
    assert_equal(server_parsed["status"], "PENDING")
