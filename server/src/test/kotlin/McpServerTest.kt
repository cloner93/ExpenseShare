import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.milad.expense_share.Amount
import org.milad.expense_share.ai.mcp.McpServerImpl
import org.milad.expense_share.ai.mcp.model.AgentSecurityContext
import org.milad.expense_share.data.models.FriendInfo
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.TransactionStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.SettlementService
import org.milad.expense_share.domain.service.TransactionService
import org.milad.expense_share.presentation.groups.model.UserGroupResponse

class McpServerTest : DescribeSpec({

    val groupService = mockk<GroupService>()
    val transactionService = mockk<TransactionService>()
    val settlementService = mockk<SettlementService>()
    val friendsService = mockk<FriendsService>()
    val userRepository = mockk<UserRepository>()

    val mcpServer = McpServerImpl(
        groupService = groupService,
        transactionService = transactionService,
        settlementService = settlementService,
        friendsService = friendsService,
        userRepository = userRepository
    )

    val user1 = User(id = 1, username = "Milad", phone = "09121111111")
    val user2 = User(id = 2, username = "Ali", phone = "09122222222")

    beforeEach {
        clearMocks(groupService, transactionService, settlementService, friendsService, userRepository)
    }

    describe("MCP Tool Discovery") {
        it("lists all available high-level domain tools") {
            val tools = mcpServer.listTools()
            val toolNames = tools.map { it.name }

            toolNames shouldBe listOf(
                "get_user_overview",
                "get_group_details",
                "list_friends",
                "propose_expense",
                "execute_expense_draft",
                "propose_settlement_payment",
                "execute_settlement_draft",
                "propose_create_group",
                "execute_create_group_draft"
            )
        }
    }

    describe("Security Context & Authorization") {
        it("denies get_group_details if user is not in the group") {
            every { groupService.getUserGroups(1) } returns Result.success(mutableListOf())

            val args = buildJsonObject { put("groupId", 99) }
            val result = mcpServer.executeTool("get_group_details", args, AgentSecurityContext(userId = 1))

            result.isError shouldBe true
            result.content.first().text shouldBe "Access denied: You are not a member of group #99"
        }

        it("denies propose_expense if user is not in the group") {
            every { groupService.getUsersOfGroup(99) } returns listOf(user2)

            val args = buildJsonObject {
                put("groupId", 99)
                put("title", "Coffee")
                put("amount", 50000L)
            }
            val result = mcpServer.executeTool("propose_expense", args, AgentSecurityContext(userId = 1))

            result.isError shouldBe true
            result.content.first().text shouldBe "Access denied: You are not a member of group #99"
        }
    }

    describe("Action Draft Lifecycle") {
        it("proposes expense as an ActionDraft without executing immediately") {
            every { groupService.getUsersOfGroup(10) } returns listOf(user1, user2)

            val args = buildJsonObject {
                put("groupId", 10)
                put("title", "Dinner")
                put("amount", 200000L)
                put("payerId", 1)
                put("participantIds", buildJsonArray { add(1); add(2) })
            }

            val result = mcpServer.executeTool("propose_expense", args, AgentSecurityContext(userId = 1))

            result.isError shouldBe false
            result.draftAction shouldNotBe null
            result.draftAction?.actionType shouldBe "CREATE_TRANSACTION"
            result.draftAction?.requiresConfirmation shouldBe true

            // Ensure transactionService.createTransaction was NOT called during proposal
            verify(exactly = 0) { transactionService.createTransaction(any(), any(), any(), any(), any(), any(), any()) }
        }

        it("executes expense draft when user confirms") {
            every { groupService.getUsersOfGroup(10) } returns listOf(user1, user2)

            val fakeTx = Transaction(
                id = 101,
                groupId = 10,
                title = "Dinner",
                amount = Amount(200000L),
                description = "",
                payers = emptyList(),
                shareDetails = mockk(),
                status = TransactionStatus.APPROVED,
                creatorId = 1
            )

            every {
                transactionService.createTransaction(
                    groupId = 10,
                    userId = 1,
                    title = "Dinner",
                    amount = Amount(200000L),
                    description = "",
                    payers = any(),
                    shareDetails = any()
                )
            } returns Result.success(fakeTx)

            val args = buildJsonObject {
                put("draftId", "test-draft-123")
                put("groupId", 10)
                put("title", "Dinner")
                put("amount", 200000L)
                put("payerId", 1)
                put("participantIds", buildJsonArray { add(1); add(2) })
            }

            val result = mcpServer.executeTool("execute_expense_draft", args, AgentSecurityContext(userId = 1))

            result.isError shouldBe false
            result.content.first().text shouldBe "Transaction #101 created successfully for 'Dinner'."
            verify(exactly = 1) { transactionService.createTransaction(10, 1, "Dinner", Amount(200000L), any(), any(), any()) }
        }
    }
})
