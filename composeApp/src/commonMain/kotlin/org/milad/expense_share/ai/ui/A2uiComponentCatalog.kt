package org.milad.expense_share.ai.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.*
import org.milad.expense_share.ai.ui.cards.DisambiguationChipsCard
import org.milad.expense_share.ai.ui.cards.ExpenseDraftCard
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
                    "PAY_SETTLEMENT" -> {
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
                    else -> {
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
                }
            }

            "DISAMBIGUATION" -> {
                val prompt = data["prompt"]?.jsonPrimitive?.content ?: ""
                val options = data["options"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

                DisambiguationChipsCard(
                    prompt = prompt,
                    options = options,
                    onSelectOption = onSelectOption,
                    modifier = modifier
                )
            }

            else -> {
                // Fallback for unmapped custom cards
            }
        }
    }
}
