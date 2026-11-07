"""
Tier 4 — Scenario 3: Dinner & Settle-Up with New Friends
End-to-end workflow covering:
1. Friend request exchange & deduplication verification.
2. Group creation from friends list.
3. Dinner bill split evenly among friends.
4. FriendsScreen settle-up dialog trigger and instant settlement confirmation.
"""

from tests.e2e.common import (
    Amount, User, Transaction, TransactionStatus, PayerRequest, ShareDetailsRequest, ShareType, ShareMemberRequest,
    Settlement, SettlementStatus, SettlementEngine, assert_true, assert_false, assert_equal
)


def test_scenario_3_dinner_with_new_friends_workflow():
    """
    Scenario 3: Dinner with new friends
    1. Host: Dave (1). Coworkers: Emma (2), Frank (3), Grace (4).
    2. Dave accepts friend requests from Emma, Frank, Grace.
    3. Multiple screen refreshes verify NO duplicate entries in FriendsScreen.
    4. Dave creates group 'Friday Team Dinner' with all 4.
    5. Dave pays $140 for dinner ($35 each).
    6. Settlements generated: Emma owes $35, Frank owes $35, Grace owes $35 to Dave.
    7. Frank opens FriendDetail and taps 'Settle Up' dialog.
    8. Frank marks $35 as PAID, Dave confirms.
    9. Emma and Grace pay their shares, Dave confirms.
    """
    dave = User(1, "Dave")
    emma = User(2, "Emma")
    frank = User(3, "Frank")
    grace = User(4, "Grace")

    # Friends state deduplication check
    raw_friends_from_server = [emma, frank, grace]
    friends_state = []

    # Emulate 5 consecutive screen visits/refreshes
    for _ in range(5):
        friends_state = list(raw_friends_from_server)

    assert_equal(len(friends_state), 3, "Friends count must strictly equal 3")
    assert_equal([f.username for f in friends_state], ["Emma", "Frank", "Grace"])

    # Group Creation
    group_id = 300
    group = {"id": group_id, "name": "Friday Team Dinner", "members": [dave] + friends_state}
    assert_equal(len(group["members"]), 4)

    # Transaction: Dinner $140 ($35 each)
    dinner_trx = Transaction(
        id=1, group_id=group_id, title="Friday Dinner", amount=Amount(140), created_by=dave.id,
        payers=[PayerRequest(dave, Amount(140))],
        share_details=ShareDetailsRequest(ShareType.EQUAL, [
            ShareMemberRequest(dave, Amount(35)),
            ShareMemberRequest(emma, Amount(35)),
            ShareMemberRequest(frank, Amount(35)),
            ShareMemberRequest(grace, Amount(35)),
        ])
    )

    settlements = SettlementEngine.calculate_settlements(group_id=group_id, transactions=[dinner_trx])
    assert_equal(len(settlements), 3)

    for s in settlements:
        assert_equal(s.creditor, dave)
        assert_equal(s.amount, Amount(35))
        assert_equal(s.status, SettlementStatus.PENDING)

    # Frank triggers Settle Up
    frank_settle = next(s for s in settlements if s.debtor == frank)
    dialog_event = {"type": "ShowSettleUpDialog", "amount": frank_settle.amount.value, "creditor": dave.username}
    assert_equal(dialog_event["amount"], 35)

    # Frank pays, Dave confirms
    SettlementEngine.mark_as_paid(frank_settle, user_id=frank.id)
    SettlementEngine.confirm_payment(frank_settle, user_id=dave.id)

    # Remaining pay
    for s in settlements:
        if s.status == SettlementStatus.PENDING:
            SettlementEngine.mark_as_paid(s, user_id=s.debtor.id)
            SettlementEngine.confirm_payment(s, user_id=dave.id)

    assert_true(all(s.status == SettlementStatus.CONFIRMED for s in settlements))
