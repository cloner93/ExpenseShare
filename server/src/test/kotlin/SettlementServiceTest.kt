
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.Settlement
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.model.SettlementStatus
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.SettlementRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.SettlementService
import org.milad.expense_share.presentation.groups.model.UserGroupResponse
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest
import org.milad.expense_share.presentation.transactions.model.ShareMemberRequest
import org.milad.expense_share.presentation.transactions.model.ShareType

class SettlementServiceTest : DescribeSpec({

    val groupRepository       = mockk<GroupRepository>()
    val transactionRepository = mockk<TransactionRepository>()
    val userRepository        = mockk<UserRepository>()
    val settlementRepository  = mockk<SettlementRepository>()

    val settlementService = SettlementService(
        groupRepository       = groupRepository,
        userRepository        = userRepository,
        transactionRepository = transactionRepository,
        settlementRepository  = settlementRepository,
    )

    val milad = User(id = 1, username = "Milad", phone = "09120000001")
    val ali   = User(id = 2, username = "Ali",   phone = "09120000002")
    val reza  = User(id = 3, username = "Reza",  phone = "09120000003")
    val sara  = User(id = 4, username = "Sara",  phone = "09120000004")

    fun makeTransaction(
        id: Int,
        groupId: Int,
        amount: Long,
        payers: List<Pair<User, Long>>,
        shares: List<Pair<User, Long>>,
        status: TransactionStatus = TransactionStatus.APPROVED,
    ) = Transaction(
        id              = id,
        groupId         = groupId,
        title           = "Transaction $id",
        amount          = Amount(amount),
        description     = "",
        createdBy       = payers.first().first.id,
        status          = status,
        createdAt       = 0L,
        transactionDate = 0L,
        payers          = payers.map { PayerRequest(it.first, Amount(it.second)) },
        shareDetails    = ShareDetailsRequest(
            type    = ShareType.Equal,
            members = shares.map { ShareMemberRequest(it.first, Amount(it.second)) }
        )
    )

    describe("recalculate") {

        context("simple case — single payer, three participants") {
            val groupId = 10
            val slot    = slot<List<Settlement>>()

            every { transactionRepository.getTransactions(1, groupId) } returns listOf(
                makeTransaction(
                    id      = 1,
                    groupId = groupId,
                    amount  = 3000,
                    payers  = listOf(milad to 3000L),
                    shares  = listOf(milad to 1000L, ali to 1000L, reza to 1000L),
                )
            )
            justRun { settlementRepository.replaceAll(groupId, capture(slot)) }

            settlementService.recalculate(groupId, requesterId = 1)

            it("should create two settlements") {
                slot.captured.size shouldBe 2
            }
            it("ali should owe 1000 to milad") {
                slot.captured.find { it.debtor == ali && it.creditor == milad }?.amount shouldBe Amount(1000)
            }
            it("reza should owe 1000 to milad") {
                slot.captured.find { it.debtor == reza && it.creditor == milad }?.amount shouldBe Amount(1000)
            }
            it("all settlements should be created with PENDING status") {
                slot.captured.all { it.status == SettlementStatus.PENDING } shouldBe true
            }
            it("replaceAll should be called exactly once") {
                verify(exactly = 1) { settlementRepository.replaceAll(groupId, any()) }
            }
        }

        context("multiple transactions with multiple payers") {

            val groupId = 20
            val slot    = slot<List<Settlement>>()

            every { transactionRepository.getTransactions(2, groupId) } returns listOf(
                makeTransaction(
                    id      = 1, groupId = groupId, amount = 5000,
                    payers  = listOf(milad to 5000L),
                    shares  = listOf(milad to 1500L, ali to 1500L, reza to 2000L),
                ),
                makeTransaction(
                    id      = 2, groupId = groupId, amount = 3000,
                    payers  = listOf(ali to 3000L),
                    shares  = listOf(milad to 1000L, ali to 2000L),
                )
            )
            justRun { settlementRepository.replaceAll(groupId, capture(slot)) }

            settlementService.recalculate(groupId, requesterId = 2)

            it("should create two settlements") {
                slot.captured.size shouldBe 2
            }
            it("reza should owe 2000 to milad") {
                slot.captured.find { it.debtor == reza && it.creditor == milad }?.amount shouldBe Amount(2000)
            }
            it("ali should owe 500 to milad") {
                slot.captured.find { it.debtor == ali && it.creditor == milad }?.amount shouldBe Amount(500)
            }
        }

        context("four participants — multiple creditors and debtors") {

            val groupId = 25
            val slot    = slot<List<Settlement>>()

            every { transactionRepository.getTransactions(1, groupId) } returns listOf(
                makeTransaction(
                    id      = 1, groupId = groupId, amount = 600,
                    payers  = listOf(milad to 600L),
                    shares  = listOf(milad to 300L, ali to 300L),
                ),
                makeTransaction(
                    id      = 2, groupId = groupId, amount = 800,
                    payers  = listOf(reza to 800L),
                    shares  = listOf(reza to 200L, sara to 600L),
                )
            )
            justRun { settlementRepository.replaceAll(groupId, capture(slot)) }

            settlementService.recalculate(groupId, requesterId = 1)

            it("should create two settlements") {
                slot.captured.size shouldBe 2
            }
            it("sara should owe 600 to reza") {
                slot.captured.find { it.debtor == sara && it.creditor == reza }?.amount shouldBe Amount(600)
            }
            it("ali should owe 300 to milad") {
                slot.captured.find { it.debtor == ali && it.creditor == milad }?.amount shouldBe Amount(300)
            }
        }

        context("when all balances are zero") {
            val groupId = 30
            val slot    = slot<List<Settlement>>()

            every { transactionRepository.getTransactions(1, groupId) } returns listOf(
                makeTransaction(
                    id      = 3, groupId = groupId, amount = 2000,
                    payers  = listOf(milad to 1000L, ali to 1000L),
                    shares  = listOf(milad to 1000L, ali to 1000L),
                )
            )
            justRun { settlementRepository.replaceAll(groupId, capture(slot)) }

            settlementService.recalculate(groupId, requesterId = 1)

            it("should store an empty list") {
                slot.captured.size shouldBe 0
            }
        }

        context("when there are no APPROVED transactions") {
            val groupId = 35
            val slot    = slot<List<Settlement>>()

            every { transactionRepository.getTransactions(1, groupId) } returns listOf(
                makeTransaction(
                    id      = 1, groupId = groupId, amount = 1000,
                    payers  = listOf(milad to 1000L),
                    shares  = listOf(milad to 500L, ali to 500L),
                    status  = TransactionStatus.PENDING,
                )
            )
            justRun { settlementRepository.replaceAll(groupId, capture(slot)) }

            settlementService.recalculate(groupId, requesterId = 1)

            it("should store an empty list since there are no APPROVED transactions") {
                slot.captured.size shouldBe 0
            }
            it("replaceAll should be called with an empty list") {
                verify { settlementRepository.replaceAll(groupId, emptyList()) }
            }
        }

        context("when paid and share are not equal — inconsistent data") {

            val groupId = 40

            every { transactionRepository.getTransactions(1, groupId) } returns listOf(
                makeTransaction(
                    id      = 4, groupId = groupId, amount = 3000,
                    payers  = listOf(milad to 3000L),
                    shares  = listOf(milad to 1000L, ali to 1500L),
                )
            )

            it("should throw IllegalStateException") {
                shouldThrow<IllegalStateException> {
                    settlementService.recalculate(groupId, requesterId = 1)
                }
            }
            it("replaceAll should not be called") {
                verify(exactly = 0) { settlementRepository.replaceAll(any(), any()) }
            }
        }
    }

    describe("getGroupSettlements") {

        context("when the user is a group member") {
            val groupId = 10
            val userId  = 1

            val fakeSettlements = listOf(
                Settlement(
                    id        = 1,
                    groupId   = groupId,
                    debtor    = ali,
                    creditor  = milad,
                    amount    = Amount(1000),
                    status    = SettlementStatus.PENDING,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            )

            every { groupRepository.getGroupsOfUser(userId) } returns listOf(
                UserGroupResponse(id = groupId, name = "Trip", ownerId = 1)
            )
            every { settlementRepository.getByGroup(groupId) } returns fakeSettlements

            val result = settlementService.getGroupSettlements(groupId, userId)

            it("should return success") {
                result.isSuccess shouldBe true
            }
            it("should return the settlements list") {
                result shouldBeSuccess { list ->
                    list.size shouldBe 1
                    list[0].debtor   shouldBe ali
                    list[0].creditor shouldBe milad
                    list[0].amount   shouldBe Amount(1000)
                }
            }
        }

        context("when the user is not a group member") {
            val groupId = 99
            val userId  = 5

            every { groupRepository.getGroupsOfUser(userId) } returns emptyList()

            val result = settlementService.getGroupSettlements(groupId, userId)

            it("should return failure") {
                result.isFailure shouldBe true
            }
            it("should be IllegalAccessException") {
                result shouldBeFailure { error ->
                    error shouldNotBe null
                    (error is IllegalAccessException) shouldBe true
                }
            }
            it("settlementRepository should not be called") {
                verify(exactly = 0) { settlementRepository.getByGroup(any()) }
            }
        }
    }

    describe("markAsPaid") {

        context("debtor requests payment — success") {
            val settlementId = 1
            val debtorId     = ali.id

            every { settlementRepository.markAsPaid(settlementId, debtorId) } returns true

            val result = settlementService.markAsPaid(settlementId, debtorId)

            it("should return success") {
                result.isSuccess shouldBe true
            }
            it("should return confirmation message") {
                result shouldBeSuccess { message ->
                    message.contains("Waiting") shouldBe true
                }
            }
        }

        context("non-debtor attempts — failure") {
            val settlementId = 1
            val wrongUserId  = milad.id

            every { settlementRepository.markAsPaid(settlementId, wrongUserId) } returns false

            val result = settlementService.markAsPaid(settlementId, wrongUserId)

            it("should return failure") {
                result.isFailure shouldBe true
            }
            it("should be IllegalAccessException") {
                result shouldBeFailure { (it is IllegalAccessException) shouldBe true }
            }
        }
    }

    describe("confirmPayment") {

        context("creditor confirms payment — success") {
            val settlementId = 1
            val creditorId   = milad.id

            every { settlementRepository.confirm(settlementId, creditorId) } returns true

            val result = settlementService.confirmPayment(settlementId, creditorId)

            it("should return success") {
                result.isSuccess shouldBe true
            }
            it("should return settlement completion message") {
                result shouldBeSuccess { message ->
                    message.contains("confirmed") shouldBe true
                }
            }
        }

        context("non-creditor attempts — failure") {
            val settlementId = 1
            val wrongUserId  = ali.id

            every { settlementRepository.confirm(settlementId, wrongUserId) } returns false

            val result = settlementService.confirmPayment(settlementId, wrongUserId)

            it("should return failure") {
                result.isFailure shouldBe true
            }
        }

        context("attempt to confirm in PENDING status — failure") {
            val settlementId = 2
            val creditorId   = milad.id

            every { settlementRepository.confirm(settlementId, creditorId) } returns false

            val result = settlementService.confirmPayment(settlementId, creditorId)

            it("should return failure") {
                result.isFailure shouldBe true
            }
        }
    }

    describe("disputePayment") {

        context("creditor disputes payment — success") {
            val settlementId = 1
            val creditorId   = milad.id

            every { settlementRepository.dispute(settlementId, creditorId) } returns true

            val result = settlementService.disputePayment(settlementId, creditorId)

            it("should return success") {
                result.isSuccess shouldBe true
            }
            it("should return message indicating status reverted to PENDING") {
                result shouldBeSuccess { message ->
                    message.contains("PENDING") shouldBe true
                }
            }
        }

        context("non-creditor attempts — failure") {
            val settlementId = 1
            val wrongUserId  = reza.id

            every { settlementRepository.dispute(settlementId, wrongUserId) } returns false

            val result = settlementService.disputePayment(settlementId, wrongUserId)

            it("should return failure") {
                result.isFailure shouldBe true
            }
        }
    }

    describe("end-to-end trip scenario") {

        val groupId = 100
        val slot    = slot<List<Settlement>>()

        every { transactionRepository.getTransactions(1, groupId) } returns listOf(
            makeTransaction(
                id = 1, groupId = groupId, amount = 900_000,
                payers = listOf(milad to 900_000L),
                shares = listOf(milad to 300_000L, sara to 300_000L, reza to 300_000L),
            ),
            makeTransaction(
                id = 2, groupId = groupId, amount = 450_000,
                payers = listOf(sara to 450_000L),
                shares = listOf(milad to 150_000L, sara to 150_000L, reza to 150_000L),
            ),
            makeTransaction(
                id = 3, groupId = groupId, amount = 180_000,
                payers = listOf(reza to 180_000L),
                shares = listOf(milad to 60_000L, sara to 60_000L, reza to 60_000L),
            )
        )
        justRun { settlementRepository.replaceAll(groupId, capture(slot)) }

        settlementService.recalculate(groupId, requesterId = 1)

        it("should create two settlements") {
            slot.captured.size shouldBe 2
        }
        it("reza should owe 330,000 to milad") {
            slot.captured
                .find { it.debtor == reza && it.creditor == milad }
                ?.amount shouldBe Amount(330_000)
        }
        it("sara should owe 60,000 to milad") {
            slot.captured
                .find { it.debtor == sara && it.creditor == milad }
                ?.amount shouldBe Amount(60_000)
        }
        it("sum of all settlements should equal total positive net (390,000)") {
            val total = slot.captured.sumOf { it.amount.value }
            total shouldBe 390_000L
        }
        it("all should have PENDING status") {
            slot.captured.all { it.status == SettlementStatus.PENDING } shouldBe true
        }
    }
})