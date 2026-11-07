"""
Tier 4 — Scenario 1: Multi-User Vacation Trip ("Trip to Istanbul")
End-to-end workflow mirroring real-world trip with 4 participants:
Milad (organizer), Ali, Sara, Reza.
Exercises group creation, multiple shared transactions, exact net balance calculation,
settlement generation, debtor payments, creditor confirmations, and final zero-balance audit.
"""

from tests.e2e.common import (
    Amount, User, Transaction, TransactionStatus, PayerRequest, ShareDetailsRequest, ShareType, ShareMemberRequest,
    Settlement, SettlementStatus, SettlementEngine, assert_true, assert_false, assert_equal
)


def test_scenario_1_vacation_trip_workflow():
    """
    Scenario 1: Complete end-to-end trip workflow
    1. Group initialization: 'Istanbul Vacation 2026' with Milad, Ali, Sara, Reza.
    2. Expense 1: Flight tickets paid by Milad (900,000) split equally between Milad, Sara, Reza.
    3. Expense 2: Hotel booking paid by Sara (450,000) split equally between Milad, Sara, Reza.
    4. Expense 3: Tour & dinner paid by Reza (180,000) split equally between Milad, Sara, Reza.
    5. Balance reconciliation: Milad (+390,000), Sara (-60,000), Reza (-330,000), Ali (0).
    6. Settlement generation:
       - Reza owes Milad 330,000 (PENDING)
       - Sara owes Milad 60,000 (PENDING)
    7. Settlement payment: Reza marks 330,000 as PAID.
    8. Confirmation: Milad confirms Reza's payment.
    9. Settlement payment: Sara marks 60,000 as PAID.
    10. Confirmation: Milad confirms Sara's payment.
    11. Final audit: All settlements CONFIRMED, zero outstanding group balance.
    """
    # 1. Users
    milad = User(id=1, username="Milad", phone="09120000001")
    ali   = User(id=2, username="Ali",   phone="09120000002")
    sara  = User(id=3, username="Sara",  phone="09120000003")
    reza  = User(id=4, username="Reza",  phone="09120000004")

    group_id = 100
    group_members = [milad, ali, sara, reza]
    assert_equal(len(group_members), 4)

    # 2. Transactions
    # Trx 1: Flights - 900,000 paid by Milad, 300,000 each for Milad, Sara, Reza
    trx1 = Transaction(
        id=1,
        group_id=group_id,
        title="Flight Tickets",
        amount=Amount(900_000),
        created_by=milad.id,
        status=TransactionStatus.APPROVED,
        payers=[PayerRequest(milad, Amount(900_000))],
        share_details=ShareDetailsRequest(
            type=ShareType.EQUAL,
            members=[
                ShareMemberRequest(milad, Amount(300_000)),
                ShareMemberRequest(sara, Amount(300_000)),
                ShareMemberRequest(reza, Amount(300_000)),
            ]
        )
    )

    # Trx 2: Hotel - 450,000 paid by Sara, 150,000 each for Milad, Sara, Reza
    trx2 = Transaction(
        id=2,
        group_id=group_id,
        title="Hotel Booking",
        amount=Amount(450_000),
        created_by=sara.id,
        status=TransactionStatus.APPROVED,
        payers=[PayerRequest(sara, Amount(450_000))],
        share_details=ShareDetailsRequest(
            type=ShareType.EQUAL,
            members=[
                ShareMemberRequest(milad, Amount(150_000)),
                ShareMemberRequest(sara, Amount(150_000)),
                ShareMemberRequest(reza, Amount(150_000)),
            ]
        )
    )

    # Trx 3: Dinner & Tour - 180,000 paid by Reza, 60,000 each for Milad, Sara, Reza
    trx3 = Transaction(
        id=3,
        group_id=group_id,
        title="Bosphorus Tour & Dinner",
        amount=Amount(180_000),
        created_by=reza.id,
        status=TransactionStatus.APPROVED,
        payers=[PayerRequest(reza, Amount(180_000))],
        share_details=ShareDetailsRequest(
            type=ShareType.EQUAL,
            members=[
                ShareMemberRequest(milad, Amount(60_000)),
                ShareMemberRequest(sara, Amount(60_000)),
                ShareMemberRequest(reza, Amount(60_000)),
            ]
        )
    )

    transactions = [trx1, trx2, trx3]

    # 3. Calculate Settlements
    settlements = SettlementEngine.calculate_settlements(group_id=group_id, transactions=transactions)
    assert_equal(len(settlements), 2, "Expected exactly 2 settlements")

    # Settlement A: Reza -> Milad (330,000)
    reza_settlement = next((s for s in settlements if s.debtor == reza and s.creditor == milad), None)
    assert_true(reza_settlement is not None, "Reza to Milad settlement must exist")
    assert_equal(reza_settlement.amount, Amount(330_000))
    assert_equal(reza_settlement.status, SettlementStatus.PENDING)

    # Settlement B: Sara -> Milad (60,000)
    sara_settlement = next((s for s in settlements if s.debtor == sara and s.creditor == milad), None)
    assert_true(sara_settlement is not None, "Sara to Milad settlement must exist")
    assert_equal(sara_settlement.amount, Amount(60_000))
    assert_equal(sara_settlement.status, SettlementStatus.PENDING)

    # Sum of debt equals 390,000
    assert_equal(sum(s.amount.value for s in settlements), 390_000)

    # 4. Debtor Payments
    # Reza marks payment as paid
    ok1 = SettlementEngine.mark_as_paid(reza_settlement, user_id=reza.id)
    assert_true(ok1)
    assert_equal(reza_settlement.status, SettlementStatus.PAID)

    # Sara marks payment as paid
    ok2 = SettlementEngine.mark_as_paid(sara_settlement, user_id=sara.id)
    assert_true(ok2)
    assert_equal(sara_settlement.status, SettlementStatus.PAID)

    # 5. Creditor Confirmations
    # Milad confirms Reza's payment
    conf1 = SettlementEngine.confirm_payment(reza_settlement, user_id=milad.id)
    assert_true(conf1)
    assert_equal(reza_settlement.status, SettlementStatus.CONFIRMED)

    # Milad confirms Sara's payment
    conf2 = SettlementEngine.confirm_payment(sara_settlement, user_id=milad.id)
    assert_true(conf2)
    assert_equal(sara_settlement.status, SettlementStatus.CONFIRMED)

    # 6. Final Audit: Group is completely settled
    assert_true(all(s.status == SettlementStatus.CONFIRMED for s in settlements))
