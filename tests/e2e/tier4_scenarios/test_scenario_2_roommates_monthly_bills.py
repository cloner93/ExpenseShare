"""
Tier 4 — Scenario 2: Roommates Monthly Shared Bills & Dispute Handling
End-to-end workflow covering:
Alice, Bob, and Charlie sharing an apartment.
Expenses: Rent, Internet, Groceries, Electricity.
Includes settlement calculation, initial payment, dispute on utility discrepancy,
expense amendment, recalculation, and final settlement confirmation.
"""

from tests.e2e.common import (
    Amount, User, Transaction, TransactionStatus, PayerRequest, ShareDetailsRequest, ShareType, ShareMemberRequest,
    Settlement, SettlementStatus, SettlementEngine, assert_true, assert_false, assert_equal
)


def test_scenario_2_roommates_monthly_bills_workflow():
    """
    Scenario 2: Roommates Monthly Bills
    1. Roommates: Alice (1), Bob (2), Charlie (3).
    2. Rent: $1,800 paid by Alice ($600 each).
    3. Internet: $90 paid by Bob ($30 each).
    4. Groceries: $150 paid by Charlie ($50 each).
    5. Net balances:
       Alice: +$1,120
       Bob: -$590
       Charlie: -$530
    6. Bob pays $590. Alice disputes because electric bill was not yet split.
    7. Electricity: $60 paid by Alice ($20 each).
    8. Recalculate:
       Alice: +$1,160
       Bob: -$610
       Charlie: -$550
    9. Bob pays $610, Alice confirms.
    10. Charlie pays $550, Alice confirms.
    """
    alice = User(id=1, username="Alice")
    bob = User(id=2, username="Bob")
    charlie = User(id=3, username="Charlie")

    group_id = 200

    # Phase 1: Initial 3 bills
    trx_rent = Transaction(
        id=1, group_id=group_id, title="Rent", amount=Amount(1800), created_by=alice.id,
        payers=[PayerRequest(alice, Amount(1800))],
        share_details=ShareDetailsRequest(ShareType.EQUAL, [
            ShareMemberRequest(alice, Amount(600)),
            ShareMemberRequest(bob, Amount(600)),
            ShareMemberRequest(charlie, Amount(600))
        ])
    )

    trx_internet = Transaction(
        id=2, group_id=group_id, title="Internet", amount=Amount(90), created_by=bob.id,
        payers=[PayerRequest(bob, Amount(90))],
        share_details=ShareDetailsRequest(ShareType.EQUAL, [
            ShareMemberRequest(alice, Amount(30)),
            ShareMemberRequest(bob, Amount(30)),
            ShareMemberRequest(charlie, Amount(30))
        ])
    )

    trx_groceries = Transaction(
        id=3, group_id=group_id, title="Groceries", amount=Amount(150), created_by=charlie.id,
        payers=[PayerRequest(charlie, Amount(150))],
        share_details=ShareDetailsRequest(ShareType.EQUAL, [
            ShareMemberRequest(alice, Amount(50)),
            ShareMemberRequest(bob, Amount(50)),
            ShareMemberRequest(charlie, Amount(50))
        ])
    )

    phase1_settlements = SettlementEngine.calculate_settlements(
        group_id=group_id, transactions=[trx_rent, trx_internet, trx_groceries]
    )
    assert_equal(len(phase1_settlements), 2)

    bob_settle = next(s for s in phase1_settlements if s.debtor == bob)
    assert_equal(bob_settle.amount, Amount(590))

    # Bob marks as paid
    SettlementEngine.mark_as_paid(bob_settle, user_id=bob.id)
    assert_equal(bob_settle.status, SettlementStatus.PAID)

    # Alice disputes because Electricity bill was missing
    dispute_ok = SettlementEngine.dispute_payment(bob_settle, user_id=alice.id)
    assert_true(dispute_ok)
    assert_equal(bob_settle.status, SettlementStatus.PENDING)

    # Phase 2: Add Electricity bill ($60 paid by Alice, $20 each)
    trx_electric = Transaction(
        id=4, group_id=group_id, title="Electricity", amount=Amount(60), created_by=alice.id,
        payers=[PayerRequest(alice, Amount(60))],
        share_details=ShareDetailsRequest(ShareType.EQUAL, [
            ShareMemberRequest(alice, Amount(20)),
            ShareMemberRequest(bob, Amount(20)),
            ShareMemberRequest(charlie, Amount(20))
        ])
    )

    all_trxs = [trx_rent, trx_internet, trx_groceries, trx_electric]
    phase2_settlements = SettlementEngine.calculate_settlements(group_id=group_id, transactions=all_trxs)
    assert_equal(len(phase2_settlements), 2)

    # Updated amounts: Bob owes $610, Charlie owes $550
    updated_bob_settle = next(s for s in phase2_settlements if s.debtor == bob)
    updated_charlie_settle = next(s for s in phase2_settlements if s.debtor == charlie)

    assert_equal(updated_bob_settle.amount, Amount(610))
    assert_equal(updated_charlie_settle.amount, Amount(550))

    # Payments and confirmations
    SettlementEngine.mark_as_paid(updated_bob_settle, user_id=bob.id)
    SettlementEngine.confirm_payment(updated_bob_settle, user_id=alice.id)

    SettlementEngine.mark_as_paid(updated_charlie_settle, user_id=charlie.id)
    SettlementEngine.confirm_payment(updated_charlie_settle, user_id=alice.id)

    assert_true(all(s.status == SettlementStatus.CONFIRMED for s in phase2_settlements))
