package org.milad.expense_share.ai.mcp

import kotlinx.serialization.json.*
import org.milad.expense_share.Amount
import org.milad.expense_share.ai.mcp.model.*
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.*
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest
import org.milad.expense_share.presentation.transactions.model.ShareMemberRequest
import org.milad.expense_share.presentation.transactions.model.ShareType
import java.util.UUID

interface McpServer {
    fun listTools(): List<McpTool>
    suspend fun executeTool(name: String, arguments: JsonObject, context: AgentSecurityContext): McpToolResult
}

class McpServerImpl(
    private val groupService: GroupService,
    private val transactionService: TransactionService,
    private val settlementService: SettlementService,
    private val friendsService: FriendsService,
    private val userRepository: UserRepository
) : McpServer {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override fun listTools(): List<McpTool> = listOf(
        McpTool(
            name = "get_user_overview",
            description = "Get an overview of the current authenticated user's groups, total friends, and recent activity.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {})
            }
        ),
        McpTool(
            name = "get_group_details",
            description = "Get detailed information about a group including its members, transactions, and outstanding settlements.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("groupId", buildJsonObject {
                        put("type", "integer")
                        put("description", "The unique ID of the group")
                    })
                })
                put("required", buildJsonArray { add("groupId") })
            }
        ),
        McpTool(
            name = "list_friends",
            description = "List the current authenticated user's confirmed friends for splitting expenses.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {})
            }
        ),
        McpTool(
            name = "propose_expense",
            description = "Propose a new expense in a group. Produces an Action Draft requiring explicit user confirmation before creation.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("groupId", buildJsonObject {
                        put("type", "integer")
                        put("description", "The group ID where the expense occurred")
                    })
                    put("title", buildJsonObject {
                        put("type", "string")
                        put("description", "A concise title or description of the expense (e.g. Dinner, Taxi, Groceries)")
                    })
                    put("amount", buildJsonObject {
                        put("type", "integer")
                        put("description", "The total monetary amount in smallest unit")
                    })
                    put("description", buildJsonObject {
                        put("type", "string")
                        put("description", "Optional details or notes")
                    })
                    put("payerId", buildJsonObject {
                        put("type", "integer")
                        put("description", "The user ID who paid the bill. Defaults to current user if omitted.")
                    })
                    put("participantIds", buildJsonObject {
                        put("type", "array")
                        put("items", buildJsonObject { put("type", "integer") })
                        put("description", "List of user IDs participating in this expense. If omitted or empty, splits equally among all group members.")
                    })
                })
                put("required", buildJsonArray {
                    add("groupId")
                    add("title")
                    add("amount")
                })
            }
        ),
        McpTool(
            name = "execute_expense_draft",
            description = "Execute a previously confirmed expense draft. Only called after explicit user approval.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("draftId", buildJsonObject {
                        put("type", "string")
                        put("description", "The unique ID of the confirmed draft")
                    })
                    put("groupId", buildJsonObject { put("type", "integer") })
                    put("title", buildJsonObject { put("type", "string") })
                    put("amount", buildJsonObject { put("type", "integer") })
                    put("description", buildJsonObject { put("type", "string") })
                    put("payerId", buildJsonObject { put("type", "integer") })
                    put("participantIds", buildJsonObject {
                        put("type", "array")
                        put("items", buildJsonObject { put("type", "integer") })
                    })
                })
                put("required", buildJsonArray {
                    add("draftId")
                    add("groupId")
                    add("title")
                    add("amount")
                })
            }
        ),
        McpTool(
            name = "propose_settlement_payment",
            description = "Propose settling an outstanding debt between group members. Produces an Action Draft requiring user confirmation.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("settlementId", buildJsonObject {
                        put("type", "integer")
                        put("description", "The settlement ID to pay")
                    })
                    put("groupId", buildJsonObject {
                        put("type", "integer")
                        put("description", "The group ID associated with the settlement")
                    })
                })
                put("required", buildJsonArray {
                    add("settlementId")
                    add("groupId")
                })
            }
        ),
        McpTool(
            name = "execute_settlement_draft",
            description = "Execute a confirmed settlement payment. Only called after explicit user approval.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("draftId", buildJsonObject { put("type", "string") })
                    put("settlementId", buildJsonObject { put("type", "integer") })
                })
                put("required", buildJsonArray {
                    add("draftId")
                    add("settlementId")
                })
            }
        ),
        McpTool(
            name = "propose_create_group",
            description = "Propose creating a new expense group with members. Produces an Action Draft requiring user confirmation.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("name", buildJsonObject {
                        put("type", "string")
                        put("description", "The name of the new group")
                    })
                    put("memberIds", buildJsonObject {
                        put("type", "array")
                        put("items", buildJsonObject { put("type", "integer") })
                        put("description", "User IDs to include in the group")
                    })
                })
                put("required", buildJsonArray { add("name") })
            }
        ),
        McpTool(
            name = "execute_create_group_draft",
            description = "Execute a confirmed group creation draft.",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("draftId", buildJsonObject { put("type", "string") })
                    put("name", buildJsonObject { put("type", "string") })
                    put("memberIds", buildJsonObject {
                        put("type", "array")
                        put("items", buildJsonObject { put("type", "integer") })
                    })
                })
                put("required", buildJsonArray {
                    add("draftId")
                    add("name")
                })
            }
        )
    )

    override suspend fun executeTool(
        name: String,
        arguments: JsonObject,
        context: AgentSecurityContext
    ): McpToolResult {
        return try {
            when (name) {
                "get_user_overview" -> handleGetUserOverview(context)
                "get_group_details" -> handleGetGroupDetails(arguments, context)
                "list_friends" -> handleListFriends(context)
                "propose_expense" -> handleProposeExpense(arguments, context)
                "execute_expense_draft" -> handleExecuteExpenseDraft(arguments, context)
                "propose_settlement_payment" -> handleProposeSettlement(arguments, context)
                "execute_settlement_draft" -> handleExecuteSettlementDraft(arguments, context)
                "propose_create_group" -> handleProposeCreateGroup(arguments, context)
                "execute_create_group_draft" -> handleExecuteCreateGroupDraft(arguments, context)
                else -> McpToolResult.error("Unknown tool: $name")
            }
        } catch (e: Exception) {
            McpToolResult.error("Error executing tool '$name': ${e.message ?: "Unknown error"}")
        }
    }

    private fun handleGetUserOverview(context: AgentSecurityContext): McpToolResult {
        val groupsResult = groupService.getUserGroups(context.userId)
        val friendsResult = friendsService.getAcceptedFriends(context.userId)

        val groups = groupsResult.getOrNull() ?: emptyList()
        val friends = friendsResult.getOrNull() ?: emptyList()

        val groupSummaries = groups.map { group ->
            buildJsonObject {
                put("id", group.id)
                put("name", group.name)
                put("memberCount", group.members.size)
                put("transactionCount", group.transactions.size)
            }
        }

        val responseJson = buildJsonObject {
            put("userId", context.userId)
            put("totalGroups", groups.size)
            put("totalFriends", friends.size)
            put("groups", JsonArray(groupSummaries))
        }

        return McpToolResult.success(responseJson.toString())
    }

    private fun handleGetGroupDetails(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val groupId = arguments["groupId"]?.jsonPrimitive?.intOrNull
            ?: return McpToolResult.error("Missing or invalid 'groupId'")

        val groups = groupService.getUserGroups(context.userId).getOrNull() ?: emptyList()
        val targetGroup = groups.firstOrNull { it.id == groupId }
            ?: return McpToolResult.error("Access denied: You are not a member of group #$groupId")

        val settlements = settlementService.getGroupSettlements(groupId, context.userId).getOrNull() ?: emptyList()

        val membersJson = targetGroup.members.map { member ->
            buildJsonObject {
                put("id", member.id)
                put("username", member.username)
                put("phone", member.phone)
            }
        }

        val settlementsJson = settlements.map { s ->
            buildJsonObject {
                put("id", s.id)
                put("debtorId", s.debtor.id)
                put("creditorId", s.creditor.id)
                put("amount", s.amount.value)
                put("status", s.status.name)
            }
        }

        val transactionsJson = targetGroup.transactions.takeLast(10).map { t ->
            buildJsonObject {
                put("id", t.id)
                put("title", t.title)
                put("amount", t.amount.value)
                put("status", t.status.name)
            }
        }

        val resultJson = buildJsonObject {
            put("groupId", targetGroup.id)
            put("name", targetGroup.name)
            put("ownerId", targetGroup.ownerId)
            put("members", JsonArray(membersJson))
            put("settlements", JsonArray(settlementsJson))
            put("recentTransactions", JsonArray(transactionsJson))
        }

        return McpToolResult.success(resultJson.toString())
    }

    private fun handleListFriends(context: AgentSecurityContext): McpToolResult {
        val friendsResult = friendsService.getAcceptedFriends(context.userId)
        val friends = friendsResult.getOrNull() ?: emptyList()

        val friendsJson = friends.map { f ->
            buildJsonObject {
                put("id", f.user.id)
                put("username", f.user.username)
                put("phone", f.user.phone)
            }
        }

        return McpToolResult.success(buildJsonObject {
            put("friends", JsonArray(friendsJson))
        }.toString())
    }

    private fun handleProposeExpense(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val userGroups = groupService.getUserGroups(context.userId).getOrNull() ?: emptyList()
        val requestedGroupId = arguments["groupId"]?.jsonPrimitive?.intOrNull
        val groupId = if (requestedGroupId != null && userGroups.any { it.id == requestedGroupId }) {
            requestedGroupId
        } else {
            userGroups.firstOrNull()?.id ?: return McpToolResult.error("شما عضو هیچ گروهی نیستید. لطفاً ابتدا یک گروه ایجاد کنید.")
        }
        val title = arguments["title"]?.jsonPrimitive?.contentOrNull
            ?: return McpToolResult.error("Missing 'title'")
        val amountValue = arguments["amount"]?.jsonPrimitive?.longOrNull
            ?: return McpToolResult.error("Missing 'amount'")
        val description = arguments["description"]?.jsonPrimitive?.contentOrNull ?: ""
        val payerId = arguments["payerId"]?.jsonPrimitive?.intOrNull ?: context.userId

        // Security check: User must belong to group
        val groupMembers = groupService.getUsersOfGroup(groupId)
        if (groupMembers.none { it.id == context.userId }) {
            return McpToolResult.error("Access denied: You are not a member of group #$groupId")
        }
        if (groupMembers.none { it.id == payerId }) {
            return McpToolResult.error("Invalid payer: User #$payerId is not a member of group #$groupId")
        }

        val participantIds = arguments["participantIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull }
            ?.takeIf { it.isNotEmpty() } ?: groupMembers.map { it.id }

        val draftId = UUID.randomUUID().toString()

        val draftPayload = buildJsonObject {
            put("groupId", groupId)
            put("title", title)
            put("amount", amountValue)
            put("description", description)
            put("payerId", payerId)
            put("participantIds", JsonArray(participantIds.map { JsonPrimitive(it) }))
        }

        val draft = ActionDraftPayload(
            draftId = draftId,
            actionType = "CREATE_TRANSACTION",
            summary = "Add expense '$title' of ${Amount(amountValue).value} in group #$groupId",
            requiresConfirmation = true,
            payload = draftPayload
        )

        return McpToolResult.success(
            text = "Expense draft created for '$title' ($amountValue). Awaiting user confirmation.",
            draftAction = draft
        )
    }

    private fun handleExecuteExpenseDraft(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val groupId = arguments["groupId"]?.jsonPrimitive?.intOrNull
            ?: return McpToolResult.error("Missing 'groupId'")
        val title = arguments["title"]?.jsonPrimitive?.contentOrNull
            ?: return McpToolResult.error("Missing 'title'")
        val amountValue = arguments["amount"]?.jsonPrimitive?.longOrNull
            ?: return McpToolResult.error("Missing 'amount'")
        val description = arguments["description"]?.jsonPrimitive?.contentOrNull ?: ""
        val payerId = arguments["payerId"]?.jsonPrimitive?.intOrNull ?: context.userId

        val groupMembers = groupService.getUsersOfGroup(groupId)
        val payerUser = groupMembers.firstOrNull { it.id == payerId }
            ?: return McpToolResult.error("Payer user #$payerId not found in group")

        val participantIds = arguments["participantIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull }
            ?.takeIf { it.isNotEmpty() } ?: groupMembers.map { it.id }

        val splitUsers = groupMembers.filter { participantIds.contains(it.id) }
        val sharePerUser = Amount(amountValue).split(splitUsers.size)

        val shareMembers = splitUsers.mapIndexed { index, user ->
            ShareMemberRequest(user = user, share = sharePerUser[index])
        }

        val result = transactionService.createTransaction(
            groupId = groupId,
            userId = context.userId,
            title = title,
            amount = Amount(amountValue),
            description = description,
            payers = listOf(PayerRequest(user = payerUser, amountPaid = Amount(amountValue))),
            shareDetails = ShareDetailsRequest(
                type = ShareType.Equal,
                members = shareMembers
            )
        )

        return result.fold(
            onSuccess = { transaction ->
                McpToolResult.success("Transaction #${transaction.id} created successfully for '$title'.")
            },
            onFailure = { error ->
                McpToolResult.error("Failed to create transaction: ${error.message}")
            }
        )
    }

    private fun handleProposeSettlement(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val settlementId = arguments["settlementId"]?.jsonPrimitive?.intOrNull
            ?: return McpToolResult.error("Missing 'settlementId'")
        val groupId = arguments["groupId"]?.jsonPrimitive?.intOrNull
            ?: return McpToolResult.error("Missing 'groupId'")

        val settlements = settlementService.getGroupSettlements(groupId, context.userId).getOrNull()
            ?: return McpToolResult.error("Access denied or group #$groupId not found")

        val target = settlements.firstOrNull { it.id == settlementId }
            ?: return McpToolResult.error("Settlement #$settlementId not found in group #$groupId")

        val draftId = UUID.randomUUID().toString()
        val draft = ActionDraftPayload(
            draftId = draftId,
            actionType = "PAY_SETTLEMENT",
            summary = "Pay settlement #${target.id} of ${target.amount.value} to user #${target.creditor.id}",
            requiresConfirmation = true,
            payload = buildJsonObject {
                put("settlementId", settlementId)
                put("groupId", groupId)
                put("amount", target.amount.value)
                put("creditorId", target.creditor.id)
            }
        )

        return McpToolResult.success(
            text = "Settlement payment draft prepared for #${target.id}. Awaiting user confirmation.",
            draftAction = draft
        )
    }

    private fun handleExecuteSettlementDraft(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val settlementId = arguments["settlementId"]?.jsonPrimitive?.intOrNull
            ?: return McpToolResult.error("Missing 'settlementId'")

        val result = settlementService.markAsPaid(settlementId, context.userId)
        return result.fold(
            onSuccess = { message -> McpToolResult.success(message) },
            onFailure = { error -> McpToolResult.error("Settlement payment failed: ${error.message}") }
        )
    }

    private fun handleProposeCreateGroup(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val name = arguments["name"]?.jsonPrimitive?.contentOrNull
            ?: return McpToolResult.error("Missing group 'name'")
        val memberIds = arguments["memberIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

        val allMembers = (memberIds + context.userId).distinct()
        val draftId = UUID.randomUUID().toString()

        val draft = ActionDraftPayload(
            draftId = draftId,
            actionType = "CREATE_GROUP",
            summary = "Create group '$name' with ${allMembers.size} members",
            requiresConfirmation = true,
            payload = buildJsonObject {
                put("name", name)
                put("memberIds", JsonArray(allMembers.map { JsonPrimitive(it) }))
            }
        )

        return McpToolResult.success(
            text = "Group draft for '$name' created with ${allMembers.size} members. Awaiting user confirmation.",
            draftAction = draft
        )
    }

    private fun handleExecuteCreateGroupDraft(arguments: JsonObject, context: AgentSecurityContext): McpToolResult {
        val name = arguments["name"]?.jsonPrimitive?.contentOrNull
            ?: return McpToolResult.error("Missing group 'name'")
        val memberIds = arguments["memberIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()

        val result = groupService.createGroup(
            ownerId = context.userId,
            name = name,
            members = memberIds
        )

        return result.fold(
            onSuccess = { group -> McpToolResult.success("Group '${group.name}' created with ID #${group.id}.") },
            onFailure = { error -> McpToolResult.error("Failed to create group: ${error.message}") }
        )
    }
}
