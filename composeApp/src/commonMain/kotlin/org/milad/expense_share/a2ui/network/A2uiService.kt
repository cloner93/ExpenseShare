package org.milad.expense_share.a2ui.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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

class A2uiService {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
        }
    }

    private val candidateHosts = listOf(
        "https://expenseshare-server.liara.run",
        "http://10.0.2.2:8082",
        "http://127.0.0.1:8082",
        "http://localhost:8082"
    )

    suspend fun fetchScenarios(): List<AgentScenario> {
        for (host in candidateHosts) {
            try {
                return httpClient.get("$host/api/a2ui/agent/scenarios").body()
            } catch (_: Exception) { }
        }
        return defaultScenarios()
    }

    suspend fun sendInteraction(request: AgentInteractionRequest): List<A2uiMessageEnvelope> {
        for (host in candidateHosts) {
            try {
                return httpClient.post("$host/api/a2ui/agent/message") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
            } catch (_: Exception) { }
        }
        return fallbackInteraction(request)
    }

    private fun defaultScenarios(): List<AgentScenario> = listOf(
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

    private fun fallbackInteraction(request: AgentInteractionRequest): List<A2uiMessageEnvelope> {
        val clientEvent = request.clientEvent
        if (clientEvent != null) {
            val action = (clientEvent.context["action"] as? JsonPrimitive)?.content ?: ""
            if (action == "SET_TIP") {
                val tip = (clientEvent.context["tip"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 15
                return generateBillSplitEnvelopes(tip)
            }
            if (action == "CONFIRM_SPLIT") {
                return listOf(
                    A2uiMessageEnvelope(
                        updateComponents = UpdateComponentsPayload(
                            surfaceId = "smart-bill-split",
                            components = listOf(
                                A2uiComponentPayload(
                                    id = "root",
                                    component = "Column",
                                    properties = buildJsonObject {
                                        putJsonArray("children") {
                                            add(JsonPrimitive("success_card"))
                                            add(JsonPrimitive("btn_new"))
                                        }
                                        put("spacing", 14)
                                    }
                                ),
                                A2uiComponentPayload(
                                    id = "success_card",
                                    component = "Card",
                                    properties = buildJsonObject {
                                        put("child", "col_succ")
                                        put("containerColor", "#E8F5E9")
                                    }
                                ),
                                A2uiComponentPayload(
                                    id = "col_succ",
                                    component = "Column",
                                    properties = buildJsonObject {
                                        putJsonArray("children") {
                                            add(JsonPrimitive("t1"))
                                            add(JsonPrimitive("t2"))
                                        }
                                        put("spacing", 6)
                                    }
                                ),
                                A2uiComponentPayload(
                                    id = "t1",
                                    component = "Text",
                                    properties = buildJsonObject {
                                        put("text", "🎉 Split Recorded Successfully!")
                                        put("variant", "H2")
                                        put("color", "#2E7D32")
                                    }
                                ),
                                A2uiComponentPayload(
                                    id = "t2",
                                    component = "Text",
                                    properties = buildJsonObject {
                                        put("text", "Settlement requests recorded to your group balance.")
                                        put("variant", "Body")
                                    }
                                ),
                                A2uiComponentPayload(
                                    id = "btn_new",
                                    component = "Button",
                                    properties = buildJsonObject {
                                        put("text", "Start New Split")
                                        put("variant", "Outlined")
                                        put("action", buildJsonObject {
                                            put("action", "SET_TIP")
                                            put("tip", 15)
                                        })
                                    }
                                )
                            )
                        )
                    )
                )
            }
        }

        val prompt = request.prompt?.lowercase() ?: ""
        return if (prompt.contains("debt") || prompt.contains("settle") || prompt.contains("balance")) {
            generateDebtOptimizerEnvelopes()
        } else {
            generateBillSplitEnvelopes(15)
        }
    }

    private fun generateBillSplitEnvelopes(tipPercent: Int): List<A2uiMessageEnvelope> {
        val surfaceId = "smart-bill-split"
        val total = 120.0 * (1.0 + tipPercent / 100.0)
        val split = total / 3.0
        val totalStr = "$" + (if (tipPercent == 10) "132.00" else if (tipPercent == 15) "138.00" else if (tipPercent == 20) "144.00" else "150.00")
        val splitStr = "$" + (if (tipPercent == 10) "44.00" else if (tipPercent == 15) "46.00" else if (tipPercent == 20) "48.00" else "50.00")

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
                                    add(JsonPrimitive("header_card"))
                                    add(JsonPrimitive("summary_card"))
                                    add(JsonPrimitive("tip_card"))
                                    add(JsonPrimitive("members_card"))
                                    add(JsonPrimitive("action_row"))
                                }
                                put("spacing", 12)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "header_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "header_col")
                                put("containerColor", "#E3F2FD")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "header_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("badge"))
                                    add(JsonPrimitive("title"))
                                }
                                put("spacing", 4)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "badge",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "✨ A2UI Dynamic Interface • v0.9")
                                put("variant", "Caption")
                                put("color", "#1565C0")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "title",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Dinner at Italian Bistro")
                                put("variant", "H3")
                                put("color", "#0D47A1")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "summary_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "sum_col")
                                put("containerColor", "#F1F8E9")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "sum_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("lbl_tot"))
                                    add(JsonPrimitive("val_tot"))
                                    add(JsonPrimitive("sub_tot"))
                                }
                                put("spacing", 4)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "lbl_tot",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Grand Total (incl. $tipPercent% tip)")
                                put("variant", "Caption")
                                put("color", "#33691E")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "val_tot",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", totalStr)
                                put("variant", "H1")
                                put("color", "#1B5E20")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "sub_tot",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Base: $120.00 • Tip: ${tipPercent}%")
                                put("variant", "Body")
                                put("color", "#558B2F")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "tip_card",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "tip_col")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "tip_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("tip_title"))
                                    add(JsonPrimitive("tip_buttons"))
                                }
                                put("spacing", 8)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "tip_title",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Adjust Tip Percentage:")
                                put("variant", "H3")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "tip_buttons",
                            component = "Row",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("t10"))
                                    add(JsonPrimitive("t15"))
                                    add(JsonPrimitive("t20"))
                                    add(JsonPrimitive("t25"))
                                }
                                put("spacing", 8)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "t10",
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
                            id = "t15",
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
                            id = "t20",
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
                            id = "t25",
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
                                    add(JsonPrimitive("m_title"))
                                    add(JsonPrimitive("m1"))
                                    add(JsonPrimitive("m2"))
                                    add(JsonPrimitive("m3"))
                                }
                                put("spacing", 6)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "m_title",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "Equally Split ($splitStr per person):")
                                put("variant", "H3")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "m1",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "👤 Alice (You): $splitStr")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "m2",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "👤 Bob: $splitStr")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "m3",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "👤 Charlie: $splitStr")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "action_row",
                            component = "Row",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("btn_save"))
                                }
                                put("spacing", 8)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "btn_save",
                            component = "Button",
                            properties = buildJsonObject {
                                put("text", "✓ Record Split & Notify Group")
                                put("variant", "Filled")
                                put("action", buildJsonObject {
                                    put("action", "CONFIRM_SPLIT")
                                })
                            }
                        )
                    )
                )
            )
        )
    }

    private fun generateDebtOptimizerEnvelopes(): List<A2uiMessageEnvelope> {
        val surfaceId = "debt-optimizer"
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
                                    add(JsonPrimitive("opt_header"))
                                    add(JsonPrimitive("step1"))
                                    add(JsonPrimitive("step2"))
                                }
                                put("spacing", 12)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "opt_header",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "h_col")
                                put("containerColor", "#FFF8E1")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "h_col",
                            component = "Column",
                            properties = buildJsonObject {
                                putJsonArray("children") {
                                    add(JsonPrimitive("b1"))
                                    add(JsonPrimitive("b2"))
                                }
                                put("spacing", 4)
                            }
                        ),
                        A2uiComponentPayload(
                            id = "b1",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "⚡ Graph Debt Simplification")
                                put("variant", "Caption")
                                put("color", "#F57F17")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "b2",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "6 Transitive Debts ➔ 2 Direct Payments")
                                put("variant", "H3")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "step1",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "t_step1")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "t_step1",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "• Alice pays Charlie: $35.00")
                                put("variant", "Body")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "step2",
                            component = "Card",
                            properties = buildJsonObject {
                                put("child", "t_step2")
                            }
                        ),
                        A2uiComponentPayload(
                            id = "t_step2",
                            component = "Text",
                            properties = buildJsonObject {
                                put("text", "• Bob pays Charlie: $20.00")
                                put("variant", "Body")
                            }
                        )
                    )
                )
            )
        )
    }
}
