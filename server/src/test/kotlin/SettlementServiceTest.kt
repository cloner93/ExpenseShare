
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.model.SettlementTransaction
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.SettlementService
import org.milad.expense_share.presentation.groups.model.UserGroupResponse
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest
import org.milad.expense_share.presentation.transactions.model.ShareMemberRequest
import org.milad.expense_share.presentation.transactions.model.ShareType

class SettlementServiceTest : DescribeSpec({

    val groupRepository = mockk<GroupRepository>()
    val transactionRepository = mockk<TransactionRepository>()
    val userRepository = mockk<UserRepository>()

    val settlementService = SettlementService(
        groupRepository = groupRepository,
        transactionRepository = transactionRepository,
        userRepository = userRepository
    )

    val user1 = User(id = 1, username = "Milad", phone = "09123456888")
    val user2 = User(id = 2, username = "Ali", phone = "09123456888")
    val user3 = User(id = 3, username = "Reza", phone = "09123456888")
    val user4 = User(id = 4, username = "Sara", phone = "09123456888")

    // --- Successful Scenarios ---

    describe("groupSettlement") {
        context("when data is valid and simple settlement is needed") {
            val groupId = 10
            val userId = 1 // The user requesting the settlement

            // Arrange
            val transaction1 = Transaction(
                id = 1, groupId = groupId, title = "Groceries", amount = Amount(3000),
                description = "", createdBy = 1, transactionDate = System.currentTimeMillis(),
                status = TransactionStatus.APPROVED,
                payers = listOf(PayerRequest(user1, Amount(3000))),
                shareDetails = ShareDetailsRequest(
                    type = ShareType.Equal,
                    members = listOf(
                        ShareMemberRequest(user1, Amount(1000)),
                        ShareMemberRequest(user2, Amount(1000)),
                        ShareMemberRequest(user3, Amount(1000))
                    )
                )
            )
            val group = UserGroupResponse(
                id = groupId,
                name = "Trip",
                ownerId = 1,
                transactions = listOf(transaction1)
            )

            coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(group)

            // Act
            val settlements = settlementService.groupSettlement(groupId, userId)

            // Assert
            it("should return two transactions: Ali to Milad and Reza to Milad") {
                settlements shouldBeSuccess { list ->

                    list.size shouldBe 2
                    list.find { it.debtor == user2 && it.creditor == user1 }?.amount shouldBe 1000
                    list.find { it.debtor == user3 && it.creditor == user1 }?.amount shouldBe 1000
                }

                // MockK Verification
                coVerify(exactly = 1) { groupRepository.getGroupsOfUser(userId) }
            }
        }

        context("when complex settlement is needed with multiple payers and shares") {
            val groupId = 20
            val userId = 2

            // Arrange
            val transaction1 = Transaction(
                id = 1, groupId = groupId, title = "Food", amount = Amount(5000), createdBy = 1,
                payers = listOf(PayerRequest(user1, Amount(5000))),
                shareDetails = ShareDetailsRequest(
                    ShareType.Equal, listOf(
                        ShareMemberRequest(user1, Amount(1500)),
                        ShareMemberRequest(user2, Amount(1500)),
                        ShareMemberRequest(user3, Amount(2000))
                    )
                ),
                description = "",
                status = TransactionStatus.APPROVED,
                createdAt = 0L,
                transactionDate = 0L,
                approvedBy = 1
            )
            val transaction2 = Transaction(
                id = 2, groupId = groupId, title = "Gas", amount = Amount(3000), createdBy = 2,
                payers = listOf(PayerRequest(user2, Amount(3000))),
                shareDetails = ShareDetailsRequest(
                    ShareType.Equal, listOf(
                        ShareMemberRequest(user1, Amount(1000)),
                        ShareMemberRequest(user2, Amount(2000))
                    )
                ),
                description = "",
                status = TransactionStatus.APPROVED,
                createdAt = 0L,
                transactionDate = 0L,
                approvedBy = 1
            )
            val group = UserGroupResponse(
                id = groupId,
                name = "Weekend",
                ownerId = 1,
                transactions = listOf(transaction1, transaction2)
            )

            coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(group)

            // Act
            val settlements = settlementService.groupSettlement(groupId, userId)

            // Assert
            it("should generate correct settlements for complex scenario") {
                settlements.shouldBeSuccess { list ->
                    list.size shouldBe 2
                    list.find { it.debtor == user3 && it.creditor == user1 }?.amount shouldBe 2000
                    list.find { it.debtor == user2 && it.creditor == user1 }?.amount shouldBe 500
                }
            }
        }

        context("when all balances are zero") {
            val groupId = 30
            val userId = 3

            // Arrange
            val transaction = Transaction(
                id = 3, groupId = groupId, title = "Equal Split", amount = Amount(2000),
                payers = listOf(
                    PayerRequest(user1, Amount(1000)),
                    PayerRequest(user2, Amount(1000))
                ),
                shareDetails = ShareDetailsRequest(
                    ShareType.Equal, listOf(
                        ShareMemberRequest(user1, Amount(1000)),
                        ShareMemberRequest(user2, Amount(1000))
                    )
                ),
                description = "",
                status = TransactionStatus.APPROVED,
                createdAt = 0L,
                transactionDate = 0L,
                approvedBy = 1,
                createdBy = 1
            )
            val group = UserGroupResponse(
                id = groupId,
                name = "Balanced",
                transactions = listOf(transaction),
                ownerId = 1,
            )

            coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(group)

            // Act
            val settlements = settlementService.groupSettlement(groupId, userId)

            // Assert
            it("should return an empty list of settlements") {
                settlements.shouldBe(emptyList<SettlementTransaction>())
            }
        }
    }

    // --- Error Handling Scenarios ---

    describe("groupSettlement error handling") {
        context("when the group is not found for the user") {
            val userId = 1
            val nonExistentGroupId = 99

            // Arrange
            coEvery { groupRepository.getGroupsOfUser(userId) } returns emptyList()

            // Act & Assert
            it("should throw IllegalArgumentException") {
                val exception = shouldThrow<IllegalArgumentException> {
                    settlementService.groupSettlement(nonExistentGroupId, userId)
                }
                exception shouldHaveMessage "گروه مورد نظر یافت نشد یا شما به آن دسترسی ندارید"
            }
        }

        context("when total paid amount does not equal total shares amount") {
            val groupId = 40
            val userId = 1

            // Arrange
            val transaction = Transaction(
                id = 4, groupId = groupId, title = "Math Error", amount = Amount(3000),
                payers = listOf(PayerRequest(user1, Amount(3000))),
                status = TransactionStatus.APPROVED,
                shareDetails = ShareDetailsRequest(
                    ShareType.Equal, listOf(
                        ShareMemberRequest(user1, Amount(1000)),
                        ShareMemberRequest(user2, Amount(1500)) // Sum is 2500, not 3000
                    )
                ), description = "", createdBy = 1
            )
            val group = UserGroupResponse(
                id = groupId,
                name = "Math Error Group",
                transactions = listOf(transaction), ownerId = 0
            )

            coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(group)

            // Act & Assert
            it("should throw IllegalArgumentException due to balance sum not being zero") {
                val exception = shouldThrow<IllegalArgumentException> {
                    settlementService.groupSettlement(groupId, userId)
                }
                exception shouldHaveMessage "Balance sum is not zero: 500"
            }
        }

        xcontext("when any amount (paid or share) is negative") {
            val groupId = 50
            val userId = 1

            // Arrange
            val transaction = Transaction(
                id = 5, groupId = groupId, title = "Negative Amount", amount = Amount(-3000),
                payers = listOf(PayerRequest(user1, Amount(-3000))),
                status = TransactionStatus.APPROVED,
                shareDetails = ShareDetailsRequest(
                    ShareType.Equal, listOf(
                        ShareMemberRequest(user1, Amount(-1000)),
                        ShareMemberRequest(user2, Amount(-2000))
                    )
                ), description = "", createdBy = 1
            )
            val group = UserGroupResponse(
                id = groupId,
                name = "Negative Amounts",
                transactions = listOf(transaction), ownerId = 0
            )

            coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(group)

            // Act & Assert
            it("should throw IllegalArgumentException because amounts are negative") {
                shouldThrow<IllegalArgumentException> {
                    settlementService.groupSettlement(groupId, userId)
                }
            }
        }

        context("when transaction has empty payers or shareDetails") {
            val groupId = 60
            val userId = 1

            // Arrange
            val transactionNoPayers = Transaction(
                id = 61, groupId = groupId, title = "No Payers", amount = Amount(1000),
                payers = emptyList(),
                status = TransactionStatus.APPROVED,
                shareDetails = ShareDetailsRequest(
                    ShareType.Equal,
                    listOf(ShareMemberRequest(user1, Amount(1000)))
                ), description = "", createdBy = 1
            )
            val transactionNoShares = Transaction(
                id = 62,
                groupId = groupId,
                title = "No Shares",
                status = TransactionStatus.APPROVED,
                amount = Amount(1000),
                payers = listOf(PayerRequest(user1, Amount(1000))),
                shareDetails = ShareDetailsRequest(ShareType.Equal, emptyList()),
                description = "",
                createdBy = 1
            )
            val group = UserGroupResponse(
                id = groupId,
                name = "Empty Transaction Lists",
                transactions = listOf(transactionNoPayers, transactionNoShares), ownerId = 0
            )

            coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(group)

            // Act & Assert
            it("should throw IllegalArgumentException for transaction with no payers") {
                coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(
                    group.copy(
                        transactions = listOf(transactionNoPayers)
                    )
                )
                shouldThrow<IllegalArgumentException> {
                    settlementService.groupSettlement(groupId, userId)
                }
            }

            it("should throw IllegalArgumentException for transaction with no share members") {
                coEvery { groupRepository.getGroupsOfUser(userId) } returns listOf(
                    group.copy(
                        transactions = listOf(transactionNoShares)
                    )
                )
                shouldThrow<IllegalArgumentException> {
                    settlementService.groupSettlement(groupId, userId)
                }
            }
        }
    }
})
