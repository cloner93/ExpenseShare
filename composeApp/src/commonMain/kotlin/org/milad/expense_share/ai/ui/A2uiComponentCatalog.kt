package org.milad.expense_share.ai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.*
import org.milad.expense_share.ai.ui.cards.BalanceChartCard
import org.milad.expense_share.ai.ui.cards.DisambiguationChipsCard
import org.milad.expense_share.ai.ui.cards.ExpenseDraftCard
import org.milad.expense_share.ai.ui.cards.GroupDraftCard
import org.milad.expense_share.ai.ui.cards.SettlementProposalCard

/**
 * A2UI Component Catalog:
 * Renders verified native Compose components dynamically from A2UI protocol JSON messages.
 */
object A2uiComponentCatalog {

    @Composable
    fun Render(
        cardType: String,
        draftId: String?,
        data: JsonObject,
        onConfirmDraft: (draftId: String, actionType: String, payload: JsonObject) -> Unit,
        onCancelDraft: (draftId: String) -> Unit,
        onSelectOption: (option: String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        when (cardType) {
            "ACTION_DRAFT" -> {
                val actionType = data["actionType"]?.jsonPrimitive?.content ?: ""
                val summary = data["summary"]?.jsonPrimitive?.content ?: ""
                val payload = data["payload"]?.jsonObject ?: JsonObject(emptyMap())
                val id = draftId ?: ""

                when (actionType) {
                    "CREATE_TRANSACTION" -> {
                        ExpenseDraftCard(
                            draftId = id,
                            actionType = actionType,
                            summary = summary,
                            payload = payload,
                            onConfirm = onConfirmDraft,
                            onCancel = onCancelDraft,
                            modifier = modifier
                        )
                    }
                    "RECORD_SETTLEMENT", "PAY_SETTLEMENT" -> {
                        SettlementProposalCard(
                            draftId = id,
                            actionType = actionType,
                            summary = summary,
                            payload = payload,
                            onConfirm = onConfirmDraft,
                            onCancel = onCancelDraft,
                            modifier = modifier
                        )
                    }
                    "CREATE_GROUP" -> {
                        GroupDraftCard(
                            draftId = id,
                            actionType = actionType,
                            summary = summary,
                            payload = payload,
                            onConfirm = onConfirmDraft,
                            onCancel = onCancelDraft,
                            modifier = modifier
                        )
                    }
                    "DISAMBIGUATION" -> {
                        val prompt = payload["prompt"]?.jsonPrimitive?.contentOrNull
                            ?: payload["question"]?.jsonPrimitive?.contentOrNull
                            ?: summary
                        val options = payload["options"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                            ?: payload["chips"]?.jsonArray?.mapNotNull { it.jsonObject["label"]?.jsonPrimitive?.contentOrNull }
                            ?: emptyList()

                        DisambiguationChipsCard(
                            prompt = prompt,
                            options = options,
                            onSelectOption = onSelectOption,
                            modifier = modifier
                        )
                    }
                    else -> {
                        // Generic fallback card for any other action draft
                        Card(
                            modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = summary.ifBlank { "پیش‌نویس اقدام ($actionType)" },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    OutlinedButton(onClick = { onCancelDraft(id) }) { Text("لغو") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { onConfirmDraft(id, actionType, payload) }) { Text("تایید") }
                                }
                            }
                        }
                    }
                }
            }

            "DISAMBIGUATION" -> {
                val prompt = data["prompt"]?.jsonPrimitive?.contentOrNull
                    ?: data["question"]?.jsonPrimitive?.contentOrNull ?: ""
                val options = data["options"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    ?: data["chips"]?.jsonArray?.mapNotNull { it.jsonObject["label"]?.jsonPrimitive?.contentOrNull }
                    ?: emptyList()

                DisambiguationChipsCard(
                    prompt = prompt,
                    options = options,
                    onSelectOption = onSelectOption,
                    modifier = modifier
                )
            }

            "BALANCE_CHART" -> {
                val summary = data["summary"]?.jsonPrimitive?.contentOrNull ?: ""
                BalanceChartCard(
                    summary = summary,
                    data = data,
                    modifier = modifier
                )
            }

            else -> {
                // Fallback for unrecognized custom cards
            }
        }
    }
}
