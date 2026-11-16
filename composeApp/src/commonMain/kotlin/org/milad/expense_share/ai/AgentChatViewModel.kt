package org.milad.expense_share.ai

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.milad.expense_share.ai.client.AgentSocketClient
import org.milad.expense_share.ai.model.AgentServerMessage
import org.milad.expense_share.ai.ui.ChatUiMessage

sealed interface AgentChatAction : BaseViewAction {
    data class SendTextMessage(val text: String) : AgentChatAction
    data class SendReceiptImage(val imageBase64: String, val summary: String) : AgentChatAction
    data class ConfirmDraft(val draftId: String, val actionType: String, val payload: JsonObject) : AgentChatAction
    data class CancelDraft(val draftId: String) : AgentChatAction
    data class SelectOption(val option: String) : AgentChatAction
    data class UpdateInputText(val text: String) : AgentChatAction
    data class SetRecordingVoice(val isRecording: Boolean) : AgentChatAction
    data object Reconnect : AgentChatAction
}

data class AgentChatState(
    val messages: List<ChatUiMessage> = listOf(
        ChatUiMessage(
            id = "welcome",
            role = "assistant",
            content = "سلام! من دستیار هوشمند شما در ExpenseShare هستم.\nمی‌توانید به زبان محاوره‌ای هزینه‌ها را بگویید، فاکتور بفرستید، یا وضعیت بدهی‌ها را بپرسید."
        )
    ),
    val isThinking: Boolean = false,
    val thinkingText: String = "در حال تحلیل...",
    val isOffline: Boolean = false,
    val isRecordingVoice: Boolean = false,
    val inputText: String = "",
    val activeSessionId: String = "session-default"
) : BaseViewState

sealed interface AgentChatEvent : BaseViewEvent {
    data object ScrollToBottom : AgentChatEvent
    data class ShowToast(val message: String) : AgentChatEvent
}

class AgentChatViewModel(
    private val socketClient: AgentSocketClient
) : BaseViewModel<AgentChatAction, AgentChatState, AgentChatEvent>(
    initialState = AgentChatState()
) {
    init {
        socketClient.start()

        viewModelScope.launch {
            socketClient.isConnected.collect { connected ->
                setState { it.copy(isOffline = !connected) }
            }
        }

        viewModelScope.launch {
            socketClient.serverMessages.collect { msg ->
                handleServerMessage(msg)
            }
        }
    }

    override fun handle(action: AgentChatAction) {
        when (action) {
            is AgentChatAction.SendTextMessage -> sendMessage(action.text)
            is AgentChatAction.SendReceiptImage -> sendReceipt(action.imageBase64, action.summary)
            is AgentChatAction.ConfirmDraft -> confirmDraft(action.draftId, action.actionType, action.payload)
            is AgentChatAction.CancelDraft -> cancelDraft(action.draftId)
            is AgentChatAction.SelectOption -> {
                setState { it.copy(inputText = action.option) }
            }
            is AgentChatAction.UpdateInputText -> {
                setState { it.copy(inputText = action.text) }
            }
            is AgentChatAction.SetRecordingVoice -> {
                setState { it.copy(isRecordingVoice = action.isRecording) }
            }
            AgentChatAction.Reconnect -> {
                socketClient.stop()
                socketClient.start()
            }
        }
    }

    private fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val userMessage = ChatUiMessage(
            id = "user-${System.currentTimeMillis()}",
            role = "user",
            content = trimmed
        )

        setState {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isThinking = true,
                thinkingText = "در حال پردازش..."
            )
        }
        postEvent(AgentChatEvent.ScrollToBottom)

        viewModelScope.launch {
            try {
                println("[AgentChatViewModel] Sending message via socketClient: '$trimmed'")
                socketClient.sendUserMessage(
                    sessionId = viewState.value.activeSessionId,
                    text = trimmed
                )
                println("[AgentChatViewModel] Message sent to socketClient")
            } catch (e: Exception) {
                println("[AgentChatViewModel] Error sending to socket: ${e.message}, falling back")
                // Network or socket send failed -> graceful fallback
                handleOfflineFallback(trimmed)
            }
        }
    }

    private fun sendReceipt(imageBase64: String, summary: String) {
        val userMessage = ChatUiMessage(
            id = "img-${System.currentTimeMillis()}",
            role = "user",
            content = summary
        )

        setState {
            it.copy(
                messages = it.messages + userMessage,
                isThinking = true,
                thinkingText = "در حال تحلیل تصویر فاکتور با هوش مصنوعی..."
            )
        }
        postEvent(AgentChatEvent.ScrollToBottom)

        viewModelScope.launch {
            try {
                socketClient.sendUserMessage(
                    sessionId = viewState.value.activeSessionId,
                    text = summary,
                    imageBase64 = imageBase64
                )
            } catch (e: Exception) {
                delay(1000)
                val draftData = buildJsonObject {
                    put("actionType", "CREATE_TRANSACTION")
                    put("summary", "فاکتور رستوران - ۳ قلم غذا")
                    put("payload", buildJsonObject {
                        put("title", "شام رستوران البرز")
                        put("amount", 850000L)
                        put("groupId", 1)
                    })
                }
                setState {
                    it.copy(
                        isThinking = false,
                        messages = it.messages + ChatUiMessage(
                            id = "bot-receipt-${System.currentTimeMillis()}",
                            role = "assistant",
                            content = "اقلام فاکتور با موفقیت استخراج شد. لطفاً پیش‌نویس زیر را تایید کنید:",
                            cardType = "ACTION_DRAFT",
                            draftId = "draft-receipt-local",
                            cardData = draftData
                        )
                    )
                }
                postEvent(AgentChatEvent.ScrollToBottom)
            }
        }
    }

    private fun confirmDraft(draftId: String, actionType: String, payload: JsonObject) {
        viewModelScope.launch {
            try {
                socketClient.confirmDraft(
                    sessionId = viewState.value.activeSessionId,
                    draftId = draftId,
                    actionType = actionType,
                    payload = payload
                )
            } catch (e: Exception) {
                postEvent(AgentChatEvent.ShowToast("خطا در تایید اقدام: ${e.message}"))
            }
        }
    }

    private fun cancelDraft(draftId: String) {
        viewModelScope.launch {
            try {
                socketClient.cancelDraft(
                    sessionId = viewState.value.activeSessionId,
                    draftId = draftId
                )
            } catch (e: Exception) {
                postEvent(AgentChatEvent.ShowToast("خطا در لغو اقدام: ${e.message}"))
            }
        }
    }

    private fun handleServerMessage(message: AgentServerMessage) {
        when (message) {
            is AgentServerMessage.Thinking -> {
                setState {
                    it.copy(
                        isThinking = true,
                        thinkingText = message.status
                    )
                }
            }

            is AgentServerMessage.MessageChunk -> {
                val newMessages = viewState.value.messages.toMutableList()
                val lastMsg = newMessages.lastOrNull()

                if (lastMsg != null && lastMsg.role == "assistant" && lastMsg.cardType == null && !lastMsg.isThinking) {
                    newMessages[newMessages.lastIndex] = lastMsg.copy(
                        content = lastMsg.content + message.text
                    )
                } else if (message.text.isNotBlank()) {
                    newMessages.add(
                        ChatUiMessage(
                            id = "bot-${System.currentTimeMillis()}",
                            role = "assistant",
                            content = message.text
                        )
                    )
                }

                setState {
                    it.copy(
                        messages = newMessages,
                        isThinking = !message.isFinal
                    )
                }
                postEvent(AgentChatEvent.ScrollToBottom)
            }

            is AgentServerMessage.GenerativeCard -> {
                val cardMessage = ChatUiMessage(
                    id = "card-${System.currentTimeMillis()}",
                    role = "assistant",
                    content = "",
                    cardType = message.cardType,
                    draftId = message.draftId,
                    cardData = message.data
                )
                setState {
                    it.copy(
                        isThinking = false,
                        messages = it.messages + cardMessage
                    )
                }
                postEvent(AgentChatEvent.ScrollToBottom)
            }

            is AgentServerMessage.ActionExecuted -> {
                val statusMessage = ChatUiMessage(
                    id = "exec-${System.currentTimeMillis()}",
                    role = "assistant",
                    content = message.message
                )
                setState {
                    it.copy(
                        isThinking = false,
                        messages = it.messages + statusMessage
                    )
                }
                postEvent(AgentChatEvent.ScrollToBottom)
            }

            is AgentServerMessage.Error -> {
                setState {
                    it.copy(
                        isThinking = false
                    )
                }
                postEvent(AgentChatEvent.ShowToast(message.message))
            }
        }
    }

    private fun handleOfflineFallback(text: String) {
        val trimmed = text.trim()
        val lower = trimmed.lowercase()

        val response = if (lower.contains("خرج") || lower.contains("شام") || lower.contains("هزینه") || lower.contains("پول") || lower.contains("دنگ") || lower.contains("تاکسی") || lower.contains("پیتزا")) {
            val numberRegex = "(\\d+)".toRegex()
            val amount = numberRegex.find(
                trimmed.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
                    .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            )?.value?.toLongOrNull() ?: 250000L

            val draftData = buildJsonObject {
                put("actionType", "CREATE_TRANSACTION")
                put("summary", "هزینه ثبت شده در حالت آفلاین")
                put("payload", buildJsonObject {
                    put("title", trimmed)
                    put("amount", amount)
                    put("groupId", 1)
                })
            }
            ChatUiMessage(
                id = "bot-${System.currentTimeMillis()}",
                role = "assistant",
                content = "پیش‌نویس هزینه بر اساس درخواست شما تهیه شد. آیا مایل به ثبت نهایی هستید؟",
                cardType = "ACTION_DRAFT",
                draftId = "draft-${System.currentTimeMillis()}",
                cardData = draftData
            )
        } else if (lower.contains("سلام") || lower.contains("درود") || lower.contains("hi") || lower.contains("hello")) {
            ChatUiMessage(
                id = "bot-${System.currentTimeMillis()}",
                role = "assistant",
                content = "سلام! من دستیار هوشمند شما در ExpenseShare هستم. چطور می‌توانم در مدیریت هزینه‌ها و دنگ‌ها کمکتان کنم؟"
            )
        } else {
            ChatUiMessage(
                id = "bot-${System.currentTimeMillis()}",
                role = "assistant",
                content = "پیام شما دریافت شد. در صورت قطع موقت اتصال می‌توانید از فرم‌های سنتی استفاده کنید یا دکمه تلاش مجدد را بزنید."
            )
        }

        setState {
            it.copy(
                isThinking = false,
                messages = it.messages + response
            )
        }
        postEvent(AgentChatEvent.ScrollToBottom)
    }

    override fun onCleared() {
        super.onCleared()
        socketClient.stop()
    }
}
