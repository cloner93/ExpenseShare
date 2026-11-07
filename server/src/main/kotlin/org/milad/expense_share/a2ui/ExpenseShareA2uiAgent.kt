package org.milad.expense_share.a2ui

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.milad.expense_share.a2ui.model.A2uiClientEventRequest
import org.milad.expense_share.a2ui.model.A2uiComponentPayload
import org.milad.expense_share.a2ui.model.A2uiMessageEnvelope
import org.milad.expense_share.a2ui.model.AgentInteractionRequest
import org.milad.expense_share.a2ui.model.AgentScenario
import org.milad.expense_share.a2ui.model.CreateSurfacePayload
import org.milad.expense_share.a2ui.model.UpdateComponentsPayload
import org.milad.expense_share.a2ui.model.UpdateDataModelPayload

class ExpenseShareA2uiAgent {

    fun getAvailableScenarios(): List<AgentScenario> = listOf(
        AgentScenario(
            id = "bill_split",
            title = "Smart Bill Splitter",
            prompt = "Split our dinner bill of $120 among Alice, Bob, and Charlie with a 15% tip",
            icon = "🍽️"
        ),
        AgentScenario(
            id = "debt_optimizer",
            title = "Optimize Group Balances",
            prompt = "Analyze group debts and recommend minimal settlements",
            icon = "⚖️"
        ),
        AgentScenario(
            id = "spending_insights",
            title = "Spending Insights",
            prompt = "Give me spending analysis and recommendations for this month",
            icon = "📊"
        )
    )

    fun handleInteraction(request: AgentInteractionRequest): List<A2uiMessageEnvelope> {
        val clientEvent = request.clientEvent
        if (clientEvent != null) {
            return handleClientEvent(clientEvent)
        }

        val prompt = request.prompt?.trim() ?: return emptyList()
        val lower = prompt.lowercase()

        return when {
            lower.contains("split") || lower.contains("bill") || lower.contains("dinner") -> {
                buildBillSplitterSurface(tipPercent = 15)
            }
            lower.contains("settle") || lower.contains("debt") || lower.contains("balance") || lower.contains("optimize") -> {
                buildDebtOptimizerSurface()
            }
            lower.contains("insight") || lower.contains("spend") || lower.contains("budget") || lower.contains("analy") -> {
                buildInsightsSurface()
            }
            else -> {
                buildBillSplitterSurface(tipPercent = 15)
            }
        }
    }

    private fun handleClientEvent(event: A2uiClientEventRequest): List<A2uiMessageEnvelope> {
        val action = (event.context["action"] as? JsonPrimitive)?.content ?: ""

        return when (action) {
            "SET_TIP" -> {
                val tip = (event.context["tip"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 15
                buildBillSplitterSurface(tipPercent = tip)
            }
            "CONFIRM_SPLIT" -> {
                buildSplitConfirmedSurface()
            }
            "RESET_BILL" -> {
                buildBillSplitterSurface(tipPercent = 15)
            }
            "SETTLE_ALICE" -> {
                buildDebtOptimizerSurface(aliceSettled = true)
            }
            "SETTLE_ALL" -> {
                buildDebtOptimizerSurface(aliceSettled = true, bobSettled = true)
            }
            else -> {
                buildBillSplitterSurface(tipPercent = 15)
            }
        }
    }

    private fun buildBillSplitterSurface(tipPercent: Int): List<A2uiMessageEnvelope> {
        val surfaceId = "smart-bill-split"
        val baseAmount = 120.00
        val tipAmount = baseAmount * (tipPercent / 100.0)
        val grandTotal = baseAmount + tipAmount
        val perPerson = grandTotal / 3.0

        val formattedGrandTotal = String.format("$%.2f", grandTotal)
        val formattedTipAmount = String.format("$%.2f", tipAmount)
        val formattedPerPerson = String.format("$%.2f", perPerson)

        val createSurface = CreateSurfacePayload(
            surfaceId = surfaceId,
            catalogId = "https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json",
            theme = mapOf("primary" to "#1976D2", "secondary" to "#388E3C")
        )

        val dataModel = UpdateDataModelPayload(
            surfaceId = surfaceId,
            path = "/",
            value = buildJsonObject {
                put("billTitle", "Dinner at Trattoria Milano")
                put("baseAmount", baseAmount)
                put("tipPercent", tipPercent)
                put("tipAmount", tipAmount)
                put("grandTotal", grandTotal)
                put("splitPerPerson", perPerson)
                put("currency", "$")
            }
        )

        val components = UpdateComponentsPayload(
            surfaceId = surfaceId,
            components = listOf(
                A2uiComponentPayload(
                    id = "root",
                    component = "Column",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("banner_card"))
                            add(JsonPrimitive("summary_card"))
                            add(JsonPrimitive("tip_selector_card"))
                            add(JsonPrimitive("members_card"))
                            add(JsonPrimitive("action_row"))
                        }
                        put("spacing", 12)
                    }
                ),
                A2uiComponentPayload(
                    id = "banner_card",
                    component = "Card",
                    properties = buildJsonObject {
                        put("child", "banner_row")
                        put("containerColor", "#E3F2FD")
                    }
                ),
                A2uiComponentPayload(
                    id = "banner_row",
                    component = "Row",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("banner_badge"))
                            add(JsonPrimitive("banner_title"))
                        }
                        put("spacing", 10)
                    }
                ),
                A2uiComponentPayload(
                    id = "banner_badge",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "✨ A2UI AGENT")
                        put("variant", "Caption")
                        put("color", "#0D47A1")
                    }
                ),
                A2uiComponentPayload(
                    id = "banner_title",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "Dinner at Trattoria Milano")
                        put("variant", "H3")
                        put("color", "#0D47A1")
                    }
                ),
                A2uiComponentPayload(
                    id = "summary_card",
                    component = "Card",
                    properties = buildJsonObject {
                        put("child", "summary_col")
                        put("containerColor", "#F1F8E9")
                    }
                ),
                A2uiComponentPayload(
                    id = "summary_col",
                    component = "Column",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("summary_label"))
                            add(JsonPrimitive("summary_total"))
                            add(JsonPrimitive("summary_subtext"))
                        }
                        put("spacing", 4)
                    }
                ),
                A2uiComponentPayload(
                    id = "summary_label",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "Total Payable (including $tipPercent% tip)")
                        put("variant", "Caption")
                        put("color", "#33691E")
                    }
                ),
                A2uiComponentPayload(
                    id = "summary_total",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", formattedGrandTotal)
                        put("variant", "H1")
                        put("color", "#1B5E20")
                    }
                ),
                A2uiComponentPayload(
                    id = "summary_subtext",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "Base: $120.00  •  Tip: $formattedTipAmount")
                        put("variant", "Body")
                        put("color", "#558B2F")
                    }
                ),
                A2uiComponentPayload(
                    id = "tip_selector_card",
                    component = "Card",
                    properties = buildJsonObject {
                        put("child", "tip_selector_col")
                    }
                ),
                A2uiComponentPayload(
                    id = "tip_selector_col",
                    component = "Column",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("tip_title"))
                            add(JsonPrimitive("tip_buttons_row"))
                        }
                        put("spacing", 8)
                    }
                ),
                A2uiComponentPayload(
                    id = "tip_title",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "Select Tip Percentage:")
                        put("variant", "H3")
                    }
                ),
                A2uiComponentPayload(
                    id = "tip_buttons_row",
                    component = "Row",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("btn_tip_10"))
                            add(JsonPrimitive("btn_tip_15"))
                            add(JsonPrimitive("btn_tip_20"))
                            add(JsonPrimitive("btn_tip_25"))
                        }
                        put("spacing", 8)
                    }
                ),
                A2uiComponentPayload(
                    id = "btn_tip_10",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "10%")
                        put("variant", if (tipPercent == 10) "Filled" else "Outlined")
                        put("action", buildJsonObject {
                            put("action", "SET_TIP")
                            put("tip", 10)
                        })
                    }
                ),
                A2uiComponentPayload(
                    id = "btn_tip_15",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "15%")
                        put("variant", if (tipPercent == 15) "Filled" else "Outlined")
                        put("action", buildJsonObject {
                            put("action", "SET_TIP")
                            put("tip", 15)
                        })
                    }
                ),
                A2uiComponentPayload(
                    id = "btn_tip_20",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "20%")
                        put("variant", if (tipPercent == 20) "Filled" else "Outlined")
                        put("action", buildJsonObject {
                            put("action", "SET_TIP")
                            put("tip", 20)
                        })
                    }
                ),
                A2uiComponentPayload(
                    id = "btn_tip_25",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "25%")
                        put("variant", if (tipPercent == 25) "Filled" else "Outlined")
                        put("action", buildJsonObject {
                            put("action", "SET_TIP")
                            put("tip", 25)
                        })
                    }
                ),
                A2uiComponentPayload(
                    id = "members_card",
                    component = "Card",
                    properties = buildJsonObject {
                        put("child", "members_col")
                    }
                ),
                A2uiComponentPayload(
                    id = "members_col",
                    component = "Column",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("members_header"))
                            add(JsonPrimitive("m1_row"))
                            add(JsonPrimitive("m2_row"))
                            add(JsonPrimitive("m3_row"))
                        }
                        put("spacing", 8)
                    }
                ),
                A2uiComponentPayload(
                    id = "members_header",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "Equally Split: $formattedPerPerson per person")
                        put("variant", "H3")
                    }
                ),
                A2uiComponentPayload(
                    id = "m1_row",
                    component = "Row",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("m1_name"))
                            add(JsonPrimitive("m1_amount"))
                        }
                        put("spacing", 8)
                    }
                ),
                A2uiComponentPayload(
                    id = "m1_name",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "👤 Alice (You)")
                        put("variant", "Body")
                    }
                ),
                A2uiComponentPayload(
                    id = "m1_amount",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", formattedPerPerson)
                        put("variant", "Body")
                        put("color", "#1976D2")
                    }
                ),
                A2uiComponentPayload(
                    id = "m2_row",
                    component = "Row",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("m2_name"))
                            add(JsonPrimitive("m2_amount"))
                        }
                        put("spacing", 8)
                    }
                ),
                A2uiComponentPayload(
                    id = "m2_name",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "👤 Bob")
                        put("variant", "Body")
                    }
                ),
                A2uiComponentPayload(
                    id = "m2_amount",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", formattedPerPerson)
                        put("variant", "Body")
                        put("color", "#1976D2")
                    }
                ),
                A2uiComponentPayload(
                    id = "m3_row",
                    component = "Row",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("m3_name"))
                            add(JsonPrimitive("m3_amount"))
                        }
                        put("spacing", 8)
                    }
                ),
                A2uiComponentPayload(
                    id = "m3_name",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "👤 Charlie")
                        put("variant", "Body")
                    }
                ),
                A2uiComponentPayload(
                    id = "m3_amount",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", formattedPerPerson)
                        put("variant", "Body")
                        put("color", "#1976D2")
                    }
                ),
                A2uiComponentPayload(
                    id = "action_row",
                    component = "Row",
                    properties = buildJsonObject {
                        putJsonArray("children") {
                            add(JsonPrimitive("btn_confirm"))
                            add(JsonPrimitive("btn_reset"))
                        }
                        put("spacing", 12)
                    }
                ),
                A2uiComponentPayload(
                    id = "btn_confirm",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "✓ Record Split & Notify Group")
                        put("variant", "Filled")
                        put("action", buildJsonObject {
                            put("action", "CONFIRM_SPLIT")
                        })
                    }
                ),
                A2uiComponentPayload(
                    id = "btn_reset",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "Reset")
                        put("variant", "Text")
                        put("action", buildJsonObject {
                            put("action", "RESET_BILL")
                        })
                    }
                )
            )
        )

        return listOf(
            A2uiMessageEnvelope(createSurface = createSurface),
            A2uiMessageEnvelope(updateDataModel = dataModel),
            A2uiMessageEnvelope(updateComponents = components)
        )
    }

    private fun buildSplitConfirmedSurface(): List<A2uiMessageEnvelope> {
        val surfaceId = "smart-bill-split"
        return listOf(
            A2uiMessageEnvelope(
                updateComponents = UpdateComponentsPayload(
                    surfaceId = surfaceId,
                    components = listOf(
                        A2uiComponentPayload(
                            id = "root",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("success_card"))
                                    add(JsonPrimitive("btn_done"))
                                }
                                put("spacing", 16)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "success_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "success_col")
                                put("containerColor", "#E8F5E9")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "success_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("success_icon"))
                                    add(JsonPrimitive("success_title"))
                                    add(JsonPrimitive("success_msg"))
                                }
                                put("spacing", 8)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "success_icon",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "🎉 Split Recorded Successfully!")
                                put("variant", "H2")
                                put("color", "#2E7D32")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "success_title",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "ExpenseShare has added 3 shares to the group ledger.")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "success_msg",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Payment requests of $46.00 sent to Bob and Charlie.")
                                put("variant", "Caption")
                                put("color", "#388E3C")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "btn_done",
                            component = "Button",
                            properties = buildJsonObject {
                                put("text", "Start New Bill Split")
                                put("variant", "Outlined")
                                put("action", buildJsonObject {
                                    put("action", "RESET_BILL")
                                })
                            }
                        )
                    )
                )
            )
        )
    }

    private fun buildDebtOptimizerSurface(
        aliceSettled: Boolean = false,
        bobSettled: Boolean = false
    ): List<A2uiMessageEnvelope> {
        val surfaceId = "debt-optimizer"
        val createSurface = CreateSurfacePayload(
            surfaceId = surfaceId,
            catalogId = "https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json"
        )

        val children = mutableListOf("header_card", "step1_card", "step2_card", "optimizer_action_row")

        val componentsList = mutableListOf(
            A2uiComponentPayload(
                id = "root",
                component = "Column",
                properties = buildJsonObject {
                    putJsonArray("children") {
                        children.forEach { add(JsonPrimitive(it)) }
                    }
                    put("spacing", 12)
                }
            ),
            A2uiComponentPayload(
                id = "header_card",
                component = "Card",
                properties = buildJsonObject {
                    put("child", "header_col")
                    put("containerColor", "#FFF8E1")
                }
            ),
            A2uiComponentPayload(
                id = "header_col",
                component = "Column",
                properties = buildJsonObject {
                    putJsonArray("children") {
                        add(JsonPrimitive("opt_badge"))
                        add(JsonPrimitive("opt_title"))
                        add(JsonPrimitive("opt_desc"))
                    }
                    put("spacing", 4)
                }
            ),
            A2uiComponentPayload(
                id = "opt_badge",
                component = "Text",
                properties = buildJsonObject {
                    put("text", "⚡ Graph Debt Simplification")
                    put("variant", "Caption")
                    put("color", "#F57F17")
                }
            ),
            A2uiComponentPayload(
                id = "opt_title",
                component = "Text",
                properties = buildJsonObject {
                    put("text", "6 Transitive Debts ➔ 2 Direct Payments")
                    put("variant", "H3")
                }
            ),
            A2uiComponentPayload(
                id = "opt_desc",
                component = "Text",
                properties = buildJsonObject {
                    put("text", "Min-Cash-Flow algorithm eliminated circular debts across 4 members.")
                    put("variant", "Caption")
                }
            ),
            A2uiComponentPayload(
                id = "step1_card",
                component = "Card",
                properties = buildJsonObject {
                    put("child", "step1_row")
                    put("containerColor", if (aliceSettled) "#E8F5E9" else "#FFFFFF")
                }
            ),
            A2uiComponentPayload(
                id = "step1_row",
                component = "Row",
                properties = buildJsonObject {
                    putJsonArray("children") {
                        add(JsonPrimitive("step1_text"))
                        add(JsonPrimitive("step1_btn"))
                    }
                    put("spacing", 12)
                }
            ),
            A2uiComponentPayload(
                id = "step1_text",
                component = "Text",
                properties = buildJsonObject {
                    put("text", if (aliceSettled) "✓ Alice paid Charlie: $35.00" else "Alice pays Charlie: $35.00")
                    put("variant", "Body")
                    put("color", if (aliceSettled) "#2E7D32" else "#212121")
                }
            ),
            A2uiComponentPayload(
                id = "step1_btn",
                component = "Button",
                properties = buildJsonObject {
                    put("text", if (aliceSettled) "Settled" else "Settle Now")
                    put("variant", if (aliceSettled) "Text" else "Filled")
                    put("action", buildJsonObject {
                        put("action", "SETTLE_ALICE")
                    })
                }
            ),
            A2uiComponentPayload(
                id = "step2_card",
                component = "Card",
                properties = buildJsonObject {
                    put("child", "step2_row")
                    put("containerColor", if (bobSettled) "#E8F5E9" else "#FFFFFF")
                }
            ),
            A2uiComponentPayload(
                id = "step2_row",
                component = "Row",
                properties = buildJsonObject {
                    putJsonArray("children") {
                        add(JsonPrimitive("step2_text"))
                        add(JsonPrimitive("step2_btn"))
                    }
                    put("spacing", 12)
                }
            ),
            A2uiComponentPayload(
                id = "step2_text",
                component = "Text",
                properties = buildJsonObject {
                    put("text", if (bobSettled) "✓ Bob pays Charlie: $20.00" else "Bob pays Charlie: $20.00")
                    put("variant", "Body")
                    put("color", if (bobSettled) "#2E7D32" else "#212121")
                }
            ),
            A2uiComponentPayload(
                id = "step2_btn",
                component = "Button",
                properties = buildJsonObject {
                    put("text", if (bobSettled) "Settled" else "Settle All")
                    put("variant", if (bobSettled) "Text" else "Outlined")
                    put("action", buildJsonObject {
                        put("action", "SETTLE_ALL")
                    })
                }
            ),
            A2uiComponentPayload(
                id = "optimizer_action_row",
                component = "Row",
                properties = buildJsonObject {
                    putJsonArray("children") {
                        add(JsonPrimitive("btn_settle_all"))
                    }
                    put("spacing", 8)
                }
            ),
            A2uiComponentPayload(
                id = "btn_settle_all",
                component = "Button",
                properties = buildJsonObject {
                    put("text", "⚡ Settle All Balances with One Tap")
                    put("variant", "Filled")
                    put("action", buildJsonObject {
                        put("action", "SETTLE_ALL")
                    })
                }
            )
        )

        return listOf(
            A2uiMessageEnvelope(createSurface = createSurface),
            A2uiMessageEnvelope(
                updateComponents = UpdateComponentsPayload(
                    surfaceId = surfaceId,
                    components = componentsList
                )
            )
        )
    }

    private fun buildInsightsSurface(): List<A2uiMessageEnvelope> {
        val surfaceId = "spending-insights"
        return listOf(
            A2uiMessageEnvelope(
                createSurface = CreateSurfacePayload(
                    surfaceId = surfaceId,
                    catalogId = "https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json"
                )
            ),
            A2uiMessageEnvelope(
                updateComponents = UpdateComponentsPayload(
                    surfaceId = surfaceId,
                    components = listOf(
                        A2uiComponentPayload(
                            id = "root",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("insight_header_card"))
                                    add(JsonPrimitive("category_card"))
                                    add(JsonPrimitive("tip_card"))
                                }
                                put("spacing", 12)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "insight_header_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "insight_header_col")
                                put("containerColor", "#EDE7F6")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "insight_header_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("insight_badge"))
                                    add(JsonPrimitive("insight_title"))
                                    add(JsonPrimitive("insight_total"))
                                }
                                put("spacing", 4)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "insight_badge",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "💡 Monthly Financial Advisor")
                                put("variant", "Caption")
                                put("color", "#512DA8")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "insight_title",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "October Spending: $485.50")
                                put("variant", "H2")
                                put("color", "#311B92")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "insight_total",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "You are spending 12% less compared to September! Great job.")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "category_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "cat_col")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "cat_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("cat_title"))
                                    add(JsonPrimitive("cat1"))
                                    add(JsonPrimitive("cat2"))
                                    add(JsonPrimitive("cat3"))
                                }
                                put("spacing", 8)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "cat_title",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Top Categories:")
                                put("variant", "H3")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "cat1",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "🍔 Restaurants & Dining: $245.00 (50.4%)")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "cat2",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "🛒 Groceries & Supermarkets: $155.50 (32.0%)")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "cat3",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "🚕 Transportation: $85.00 (17.6%)")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "tip_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "tip_text")
                                put("containerColor", "#E0F2F1")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "tip_text",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "💡 AI Recommendation: Splitting recurring dinner bills through ExpenseShare saved you ~$45 in untracked debts this month.")
                                put("variant", "Body")
                                put("color", "#004D40")
                            }
                        )
                    )
                )
            )
        )
    }
}
