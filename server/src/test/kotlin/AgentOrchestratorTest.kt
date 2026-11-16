import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.milad.expense_share.ai.agent.*
import org.milad.expense_share.ai.mcp.McpServer
import org.milad.expense_share.ai.mcp.model.ActionDraftPayload
import org.milad.expense_share.ai.mcp.model.AgentSecurityContext
import org.milad.expense_share.ai.mcp.model.McpToolResult
import org.milad.expense_share.data.models.ConversationMessage
import org.milad.expense_share.domain.repository.ConversationRepository

class AgentOrchestratorTest : DescribeSpec({

    val mcpServer = mockk<McpServer>()
    val agentClient = mockk<AgentClient>()
    val conversationRepository = mockk<ConversationRepository>()

    val orchestrator = AgentOrchestrator(
        mcpServer = mcpServer,
        agentClient = agentClient,
        conversationRepository = conversationRepository
    )

    beforeEach {
        clearMocks(mcpServer, agentClient, conversationRepository)
        every { conversationRepository.ensureSession(any(), any(), any()) } returns mockk()
    }

    describe("Agent Conversational Flow") {
        it("handles natural language user turn and produces text response") {
            val sessionId = "session-123"
            val context = AgentSecurityContext(userId = 1)

            every { conversationRepository.saveMessage(sessionId, any(), any(), any()) } returns mockk()
            every { conversationRepository.getMessages(sessionId) } returns listOf(
                ConversationMessage(1, sessionId, "user", "سلام", null, 1000L)
            )
            every { mcpServer.listTools() } returns emptyList()
            coEvery { agentClient.generateResponse(any(), any()) } returns AgentResponse(
                text = "سلام! چطور می‌تونم کمکتون کنم؟"
            )

            val emittedEvents = mutableListOf<AgentServerEvent>()

            orchestrator.handleUserMessage(sessionId, "سلام", context) { event ->
                emittedEvents.add(event)
            }

            emittedEvents.any { it is AgentServerEvent.Thinking } shouldBe true
            val finalChunk = emittedEvents.filterIsInstance<AgentServerEvent.MessageChunk>().firstOrNull()
            finalChunk shouldNotBe null
            finalChunk?.text shouldBe "سلام! چطور می‌تونم کمکتون کنم؟"

            verify { conversationRepository.saveMessage(sessionId, "user", "سلام") }
            verify { conversationRepository.saveMessage(sessionId, "assistant", "سلام! چطور می‌تونم کمکتون کنم؟") }
        }

        it("emits A2UI GenerativeCard when a tool produces an ActionDraft") {
            val sessionId = "session-123"
            val context = AgentSecurityContext(userId = 1)

            every { conversationRepository.saveMessage(sessionId, any(), any(), any()) } returns mockk()
            every { conversationRepository.getMessages(sessionId) } returns emptyList()
            every { mcpServer.listTools() } returns emptyList()

            val toolCall = AgentToolCall(
                id = "call-1",
                name = "propose_expense",
                arguments = buildJsonObject { put("amount", 50000L) }
            )

            coEvery { agentClient.generateResponse(any(), any()) } returns AgentResponse(
                text = "پیش‌نویس هزینه ایجاد شد",
                toolCalls = listOf(toolCall)
            )

            val draftPayload = ActionDraftPayload(
                draftId = "draft-999",
                actionType = "CREATE_TRANSACTION",
                summary = "Dinner expense",
                requiresConfirmation = true,
                payload = buildJsonObject { put("title", "Dinner") }
            )

            coEvery { mcpServer.executeTool("propose_expense", any(), context) } returns McpToolResult(
                isError = false,
                draftAction = draftPayload
            )

            val emittedEvents = mutableListOf<AgentServerEvent>()

            orchestrator.handleUserMessage(sessionId, "ثبت شام", context) { event ->
                emittedEvents.add(event)
            }

            val cardEvent = emittedEvents.filterIsInstance<AgentServerEvent.GenerativeCard>().firstOrNull()
            cardEvent shouldNotBe null
            cardEvent?.cardType shouldBe "ACTION_DRAFT"
            cardEvent?.draftId shouldBe "draft-999"

            verify {
                conversationRepository.saveMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = "پیش‌نویس هزینه ایجاد شد",
                    a2uiPayload = any()
                )
            }
        }

        it("executes action draft on confirmation and emits ActionExecuted") {
            val sessionId = "session-123"
            val context = AgentSecurityContext(userId = 1)

            every { conversationRepository.saveMessage(sessionId, any(), any(), any()) } returns mockk()
            coEvery { mcpServer.executeTool("execute_expense_draft", any(), context) } returns McpToolResult.success(
                "تراکنش با موفقیت ثبت شد"
            )

            val emittedEvents = mutableListOf<AgentServerEvent>()

            orchestrator.handleDraftConfirmation(
                sessionId = sessionId,
                draftId = "draft-999",
                actionType = "CREATE_TRANSACTION",
                payload = buildJsonObject { put("amount", 50000L) },
                context = context
            ) { event ->
                emittedEvents.add(event)
            }

            val executedEvent = emittedEvents.filterIsInstance<AgentServerEvent.ActionExecuted>().firstOrNull()
            executedEvent shouldNotBe null
            executedEvent?.success shouldBe true
            executedEvent?.draftId shouldBe "draft-999"
            executedEvent?.message shouldBe "تراکنش با موفقیت ثبت شد"
        }
    }
})
