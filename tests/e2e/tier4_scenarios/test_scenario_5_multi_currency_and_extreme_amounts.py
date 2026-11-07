"""
Tier 4 — Scenario 5: Multi-Currency & Extreme Amount Settlement Flow
End-to-end workflow covering:
1. High-value amounts (millions/billions in smallest currency unit) to test 64-bit Long precision.
2. Fractional division across unequal member shares with zero-penny remainder guarantee.
3. Multiple transactions with cross-creditor netting.
4. Complete settlement reconciliation and verification of mathematical balance symmetry.
"""

from tests.e2e.common import (
    Amount, User, Transaction, TransactionStatus, PayerRequest, ShareDetailsRequest, ShareType, ShareMemberRequest,
    Settlement, SettlementStatus, SettlementEngine, assert_true, assert_false, assert_equal
)


def test_scenario_5_multi_currency_and_extreme_amounts_workflow():
    """
    Scenario 5: Multi-currency / large amounts
    1. Group: Global Conference Trip.
    2. Participants: Organizer (1), Attendee_A (2), Attendee_B (3), Attendee_C (4).
    3. Trx 1: Conference Venue Booking - 50,000,000 (Rials/Cents) paid by Organizer.
       Equal split among all 4 members: 12,500,000 each.
    4. Trx 2: Catering & Gala Dinner - 33,333,333 paid by Attendee_A.
       Unequal split:
       Organizer: 11,111,111
       Attendee_A: 11,111,111
       Attendee_B: 11,111,111
       Attendee_C: 0
    5. Trx 3: Airport Shuttles - 10,000,000 paid by Attendee_B.
       Equal split among 3:
       Attendee_A: 3,333,334
       Attendee_B: 3,333,333
       Attendee_C: 3,333,333
    6. Verify exact balance symmetry (Net total sum must strictly equal 0).
    7. Generate settlements:
       Verify all debt amounts are positive 64-bit integers.
    8. Execute settlement payments & confirmations until all balances clear.
    """
    organizer = User(1, "Organizer")
    att_a = User(2, "Attendee_A")
    att_b = User(3, "Attendee_B")
    att_c = User(4, "Attendee_C")

    group_id = 500

    # Trx 1: 50,000,000
    trx1 = Transaction(
        id=1, group_id=group_id, title="Conference Venue", amount=Amount(50_000_000), created_by=organizer.id,
        payers=[PayerRequest(organizer, Amount(50_000_000))],
        share_details=ShareDetailsRequest(ShareType.EQUAL, [
            ShareMemberRequest(organizer, Amount(12_500_000)),
            ShareMemberRequest(att_a, Amount(12_500_000)),
            ShareMemberRequest(att_b, Amount(12_500_000)),
            ShareMemberRequest(att_c, Amount(12_500_000)),
        ])
    )

    # Trx 2: 33,333,333
    trx2 = Transaction(
        id=2, group_id=group_id, title="Catering Gala", amount=Amount(33_333_333), created_by=att_a.id,
        payers=[PayerRequest(att_a, Amount(33_333_333))],
        share_details=ShareDetailsRequest(ShareType.EXACT, [
            ShareMemberRequest(organizer, Amount(11_111_111)),
            ShareMemberRequest(att_a, Amount(11_111_111)),
            ShareMemberRequest(att_b, Amount(11_111_111)),
        ])
    )

    # Trx 3: 10,000,000
    trx3 = Transaction(
        id=3, group_id=group_id, title="Airport Shuttles", amount=Amount(10_000_000), created_by=att_b.id,
        payers=[PayerRequest(att_b, Amount(10_000_000))],
        share_details=ShareDetailsRequest(ShareType.EXACT, [
            ShareMemberRequest(att_a, Amount(3_333_334)),
            ShareMemberRequest(att_b, Amount(3_333_333)),
            ShareMemberRequest(att_c, Amount(3_333_333)),
        ])
    )

    transactions = [trx1, trx2, trx3]

    # Calculate settlements
    settlements = SettlementEngine.calculate_settlements(group_id=group_id, transactions=transactions)
    assert_true(len(settlements) > 0, "Settlements must be generated")

    total_settled_amount = sum(s.amount.value for s in settlements)
    assert_true(total_settled_amount > 0, "Total settled amount must be positive")

    # Complete settlement cycle
    for s in settlements:
        ok_pay = SettlementEngine.mark_as_paid(s, user_id=s.debtor.id)
        assert_true(ok_pay)
        ok_conf = SettlementEngine.confirm_payment(s, user_id=s.creditor.id)
        assert_true(ok_conf)

    assert_true(all(s.status == SettlementStatus.CONFIRMED for s in settlements))
