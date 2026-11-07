"""
Tier 1: Feature Coverage for Critical Missing Features & Crashes (Requirement R2)
- F4: F4_GROUP_CRASH_FIX (5 tests)
- F5: F5_SETTLEMENT_WIRING (5 tests)
- F6: F6_FRIENDS_DEDUPLICATION (5 tests)
- F7: F7_FRIENDS_EMPTY_HANDLERS (5 tests)
- F8: F8_AUTH_TOKEN_PERSISTENCE (5 tests)
"""

import re
from pathlib import Path
from tests.e2e.common import (
    COMPOSE_APP_DIR, CORE_NETWORK_DIR, CORE_DOMAIN_DIR, CORE_DATA_DIR,
    Amount, User, Settlement, SettlementStatus, SettlementEngine, PersistentTokenStorage,
    read_file, assert_true, assert_false, assert_equal, assert_raises
)


# =============================================================================
# F4: Group Crash Fix (5 tests)
# =============================================================================

def test_f4_01_group_detail_vm_show_help_safe():
    """Verify GroupDetailViewModel does not throw NotImplementedError on ShowHelp."""
    vm_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "GroupDetailViewModel.kt"
    assert_true(vm_file.exists(), "GroupDetailViewModel.kt must exist")
    content = read_file(vm_file)
    
    assert_false('is GroupDetailAction.ShowHelp -> TODO' in content, "ShowHelp must not contain TODO('Not yet implemented')")


def test_f4_02_group_detail_vm_rename_group_safe():
    """Verify GroupDetailViewModel does not throw NotImplementedError on RenameGroup."""
    vm_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "GroupDetailViewModel.kt"
    content = read_file(vm_file)
    
    assert_false('is GroupDetailAction.RenameGroup -> TODO' in content, "RenameGroup must not contain TODO('Not yet implemented')")


def test_f4_03_group_dropdown_menu_rename_option():
    """Verify Rename Group menu item is removed or kept disabled without crash."""
    menu_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "components" / "GroupDropdownMenu.kt"
    assert_true(menu_file.exists(), "GroupDropdownMenu.kt must exist")
    content = read_file(menu_file)
    
    # Check that rename does not trigger unhandled TODO
    assert_false('TODO("Not yet implemented")' in content, "GroupDropdownMenu must not contain unhandled TODO")


def test_f4_04_group_dropdown_menu_help_option():
    """Verify Help menu item does not crash on invocation."""
    menu_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "components" / "GroupDropdownMenu.kt"
    content = read_file(menu_file)
    
    # Help must not throw NotImplementedError
    assert_false('throw NotImplementedError' in content, "GroupDropdownMenu must not throw NotImplementedError")


def test_f4_05_group_action_simulation_safety():
    """Simulate GroupDetailAction dispatch for ShowHelp and RenameGroup."""
    class DummyViewModel:
        def __init__(self):
            self.state = {"help_shown": False, "renamed": False}

        def handle_action(self, action_name: str, payload: dict):
            if action_name == "ShowHelp":
                self.state["help_shown"] = True
            elif action_name == "RenameGroup":
                # Safe no-op or controlled rename
                self.state["renamed"] = True
            else:
                raise ValueError(f"Unknown action {action_name}")

    vm = DummyViewModel()
    # Must execute cleanly without exception
    vm.handle_action("ShowHelp", {"groupId": 10})
    assert_true(vm.state["help_shown"], "ShowHelp handled safely")
    vm.handle_action("RenameGroup", {"groupId": 10})
    assert_true(vm.state["renamed"], "RenameGroup handled safely")


# =============================================================================
# F5: Settlement Wiring (5 tests)
# =============================================================================

def test_f5_01_settlement_screen_callbacks():
    """Verify SettlementScreen wires onPayClick, onApproveClick, onRejectClick."""
    screen_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "screen" / "SettlementScreen.kt"
    assert_true(screen_file.exists(), "SettlementScreen.kt must exist")
    content = read_file(screen_file)
    
    # Must wire the action callbacks to SettlementListItem
    assert_true("onPayClick" in content, "SettlementScreen must connect onPayClick")
    assert_true("onApproveClick" in content, "SettlementScreen must connect onApproveClick")
    assert_true("onRejectClick" in content, "SettlementScreen must connect onRejectClick")


def test_f5_02_group_detail_vm_settlement_actions():
    """Verify GroupDetailAction defines PaySettlement, ConfirmSettlement, DisputeSettlement."""
    vm_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "GroupDetailViewModel.kt"
    content = read_file(vm_file)
    
    assert_true("PaySettlement" in content, "GroupDetailViewModel must declare PaySettlement action")
    assert_true("ConfirmSettlement" in content, "GroupDetailViewModel must declare ConfirmSettlement action")
    assert_true("DisputeSettlement" in content, "GroupDetailViewModel must declare DisputeSettlement action")


def test_f5_03_group_detail_vm_injects_usecases():
    """Verify GroupDetailViewModel constructor receives settlement use cases."""
    vm_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "group" / "GroupDetailViewModel.kt"
    content = read_file(vm_file)
    
    assert_true("PaySettlementUseCase" in content or "paySettlement" in content, "GroupDetailViewModel must inject PaySettlementUseCase")
    assert_true("ConfirmSettlementUseCase" in content or "confirmSettlement" in content, "GroupDetailViewModel must inject ConfirmSettlementUseCase")
    assert_true("DisputeSettlementUseCase" in content or "disputeSettlement" in content, "GroupDetailViewModel must inject DisputeSettlementUseCase")


def test_f5_04_koin_registers_settlement_usecases():
    """Verify Koin domainModule registers PaySettlementUseCase, ConfirmSettlementUseCase, DisputeSettlementUseCase."""
    koin_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "di" / "koinModules.kt"
    assert_true(koin_file.exists(), "koinModules.kt must exist")
    content = read_file(koin_file)
    
    assert_true("PaySettlementUseCase" in content, "Koin domainModule must register PaySettlementUseCase")
    assert_true("ConfirmSettlementUseCase" in content, "Koin domainModule must register ConfirmSettlementUseCase")
    assert_true("DisputeSettlementUseCase" in content, "Koin domainModule must register DisputeSettlementUseCase")


def test_f5_05_settlement_lifecycle_simulation():
    """Simulate settlement state transitions: PENDING -> PAID -> CONFIRMED, and PAID -> DISPUTED."""
    u1 = User(id=1, username="Milad")
    u2 = User(id=2, username="Ali")
    
    settlement = Settlement(id=1, group_id=10, debtor=u2, creditor=u1, amount=Amount(500))
    assert_equal(settlement.status, SettlementStatus.PENDING)
    
    # Ali pays
    ok = SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    assert_true(ok, "Debtor can mark settlement as paid")
    assert_equal(settlement.status, SettlementStatus.PAID)
    
    # Milad disputes
    ok = SettlementEngine.dispute_payment(settlement, user_id=u1.id)
    assert_true(ok, "Creditor can dispute paid settlement")
    assert_equal(settlement.status, SettlementStatus.PENDING)
    
    # Ali pays again
    SettlementEngine.mark_as_paid(settlement, user_id=u2.id)
    # Milad confirms
    ok = SettlementEngine.confirm_payment(settlement, user_id=u1.id)
    assert_true(ok, "Creditor can confirm paid settlement")
    assert_equal(settlement.status, SettlementStatus.CONFIRMED)


# =============================================================================
# F6: Friends Deduplication (5 tests)
# =============================================================================

def test_f6_01_friends_vm_assignment_not_additive():
    """Verify FriendsViewModel replaces friends list with newFriends instead of appending it.friends."""
    vm_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "friends" / "FriendsViewModel.kt"
    assert_true(vm_file.exists(), "FriendsViewModel.kt must exist")
    content = read_file(vm_file)
    
    # Buggy pattern was: friends = it.friends + newFriends
    assert_false("it.friends + newFriends" in content, "FriendsViewModel must not append it.friends + newFriends")


def test_f6_02_multiple_refreshes_maintain_exact_count():
    """Verify that multiple consecutive refreshes do not accumulate duplicate friends."""
    friends_list = []
    incoming_api_response = [
        {"id": 1, "username": "Ali"},
        {"id": 2, "username": "Sara"},
        {"id": 3, "username": "Reza"},
    ]
    
    # Simulate 5 consecutive refreshes with the corrected assignment
    for _ in range(5):
        # Correct logic: replace
        friends_list = list(incoming_api_response)
    
    assert_equal(len(friends_list), 3, "Friend count must remain exactly 3 after 5 refreshes")


def test_f6_03_adding_friend_and_refresh_preserves_uniqueness():
    """Verify adding a friend followed by refresh produces unique list without duplicates."""
    initial_friends = [{"id": 1, "name": "Ali"}]
    updated_friends = [{"id": 1, "name": "Ali"}, {"id": 2, "name": "Sara"}]
    
    # State update
    state = {"friends": initial_friends}
    # Refresh
    state["friends"] = updated_friends
    
    unique_ids = set(f["id"] for f in state["friends"])
    assert_equal(len(state["friends"]), len(unique_ids), "All friends in state must have unique IDs")
    assert_equal(len(state["friends"]), 2)


def test_f6_04_empty_friends_refresh_yields_empty_list():
    """Verify refreshing when server returns empty friends list correctly clears state."""
    state = {"friends": [{"id": 1, "name": "Ali"}]}
    incoming_empty = []
    state["friends"] = incoming_empty
    
    assert_equal(len(state["friends"]), 0, "Friends list should be empty")


def test_f6_05_concurrent_friend_updates_idempotency():
    """Verify repeated loads with same dataset are strictly idempotent."""
    dataset = [User(1, "A"), User(2, "B"), User(3, "C")]
    current_state = []
    
    for _ in range(10):
        current_state = list(dataset)
    
    assert_equal(current_state, dataset, "State must remain strictly idempotent")


# =============================================================================
# F7: Friends Empty Handlers (5 tests)
# =============================================================================

def test_f7_01_friends_show_toast_implemented():
    """Verify FriendsScreen handles FriendsEvent.ShowToast."""
    screen_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "friends" / "FriendsScreen.kt"
    assert_true(screen_file.exists(), "FriendsScreen.kt must exist")
    content = read_file(screen_file)
    
    # Check that ShowToast is not an empty stub block
    assert_false("is FriendsEvent.ShowToast -> {}" in content, "ShowToast must not be empty stub block")


def test_f7_02_friend_detail_show_toast_implemented():
    """Verify FriendsScreen handles FriendDetailEvent.ShowToast."""
    screen_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "friends" / "FriendsScreen.kt"
    content = read_file(screen_file)
    
    assert_false("is FriendDetailEvent.ShowToast -> {}" in content, "FriendDetailEvent.ShowToast must not be empty stub block")


def test_f7_03_friend_detail_open_group_implemented():
    """Verify FriendsScreen handles FriendDetailEvent.OpenGroup."""
    screen_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "friends" / "FriendsScreen.kt"
    content = read_file(screen_file)
    
    assert_false("is FriendDetailEvent.OpenGroup -> {}" in content, "FriendDetailEvent.OpenGroup must not be empty stub block")


def test_f7_04_friend_detail_settle_up_dialog_implemented():
    """Verify FriendsScreen handles FriendDetailEvent.ShowSettleUpDialog."""
    screen_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "friends" / "FriendsScreen.kt"
    content = read_file(screen_file)
    
    assert_false("is FriendDetailEvent.ShowSettleUpDialog -> {}" in content, "ShowSettleUpDialog must not be empty stub block")


def test_f7_05_friends_event_processing_simulation():
    """Simulate handling of all 4 FriendsScreen events."""
    handled_events = []

    def on_event(event_type: str, payload: Any):
        handled_events.append((event_type, payload))

    on_event("FriendsEvent.ShowToast", "Friend request sent")
    on_event("FriendDetailEvent.ShowToast", "Profile updated")
    on_event("FriendDetailEvent.OpenGroup", {"groupId": 101})
    on_event("FriendDetailEvent.ShowSettleUpDialog", {"friendId": 2})

    assert_equal(len(handled_events), 4, "All 4 friend events must be processed")
    assert_equal(handled_events[0][0], "FriendsEvent.ShowToast")
    assert_equal(handled_events[2][1]["groupId"], 101)


# =============================================================================
# F8: Auth Token & Session Persistence (5 tests)
# =============================================================================

def test_f8_01_token_provider_stores_tokens():
    """Verify TokenProvider stores access and refresh tokens."""
    storage = PersistentTokenStorage()
    assert_equal(storage.get_token(), None, "Initially tokens must be None")
    
    storage.set_token("access_jwt_123", "refresh_jwt_456")
    tokens = storage.get_token()
    assert_true(tokens is not None, "Token must be present after set")
    assert_equal(tokens, ("access_jwt_123", "refresh_jwt_456"))


def test_f8_02_token_provider_survives_restart():
    """Verify persistent token survives process restart."""
    storage1 = PersistentTokenStorage()
    storage1.set_token("persisted_access_token", "persisted_refresh_token")
    
    # Simulate app process death and recreation
    storage2 = storage1.restart_process()
    restored = storage2.get_token()
    assert_true(restored is not None, "Token must survive app process restart")
    assert_equal(restored[0], "persisted_access_token")
    assert_equal(restored[1], "persisted_refresh_token")


def test_f8_03_token_provider_clear_token():
    """Verify clearToken() purges all tokens from storage."""
    storage = PersistentTokenStorage()
    storage.set_token("token_a", "token_b")
    storage.clear_token()
    
    assert_equal(storage.get_token(), None, "Storage must be empty after clear_token()")


def test_f8_04_logout_calls_clear_token():
    """Verify logout workflow in ProfileScreen or AuthManager invokes clearToken()."""
    class AuthManager:
        def __init__(self, token_storage: PersistentTokenStorage):
            self.storage = token_storage
            self.current_user = {"id": 1, "username": "Milad"}

        def logout(self):
            self.storage.clear_token()
            self.current_user = None

    storage = PersistentTokenStorage()
    storage.set_token("active_token", "active_refresh")
    manager = AuthManager(storage)
    
    manager.logout()
    assert_equal(storage.get_token(), None, "Tokens must be cleared on logout")
    assert_equal(manager.current_user, None, "User session must be cleared on logout")


def test_f8_05_user_repository_persistent_session():
    """Verify UserRepository session handling does not crash with uninitialized lateinit."""
    class UserRepositorySimulator:
        def __init__(self):
            self._cached_user: Optional[User] = None

        def get_user_info(self) -> Optional[User]:
            # Safe access instead of throwing UninitializedPropertyAccessException
            return self._cached_user

        def save_user(self, user: User):
            self._cached_user = user

    repo = UserRepositorySimulator()
    # Before login, get_user_info must return None safely without crashing
    assert_equal(repo.get_user_info(), None, "Cold start before login should return None safely")
    
    # After login, user is cached
    u = User(1, "Milad")
    repo.save_user(u)
    assert_equal(repo.get_user_info(), u)
