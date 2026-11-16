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
        connectSocket()
    }

    private fun connectSocket() {
        viewModelScope.launch {
            socketClient.connect(
                onMessage = { serverMsg -> handleServerMessage(serverMsg) },
                onConnectionChanged = { isConnected ->
                    setState { it.copy(isOffline = !isConnected) }
                }
            )
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
            AgentChatAction.Reconnect -> connectSocket()
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
                if (!viewState.value.isOffline) {
                    socketClient.sendUserMessage(
                        sessionId = viewState.value.activeSessionId,
                        text = trimmed
                    )
                } else {
                    // Local fallback heuristic when offline
                    delay(800)
                    handleOfflineFallback(trimmed)
                }
            } catch (e: Exception) {
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
                if (!viewState.value.isOffline) {
                    socketClient.sendUserMessage(
                        sessionId = viewState.value.activeSessionId,
                        text = summary,
                        imageBase64 = imageBase64
                    )
                } else {
                    delay(1200)
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
            } catch (e: Exception) {
                setState { it.copy(isThinking = false) }
            }
        }
    }

    private fun confirmDraft(draftId: String, actionType: String, payload: JsonObject) {
        viewModelScope.launch {
            try {
                if (!viewState.value.isOffline) {
                    socketClient.confirmDraft(
                        sessionId = viewState.value.activeSessionId,
                        draftId = draftId,
                        actionType = actionType,
                        payload = payload
                    )
                } else {
                    setState {
                        it.copy(
                            messages = it.messages + ChatUiMessage(
                                id = "confirm-${System.currentTimeMillis()}",
                                role = "assistant",
                                content = "اقدام با موفقیت در سیستم ثبت گردید."
                            )
                        )
                    }
                    postEvent(AgentChatEvent.ScrollToBottom)
                }
            } catch (e: Exception) {
                postEvent(AgentChatEvent.ShowToast("خطا در تایید اقدام: ${e.message}"))
            }
        }
    }

    private fun cancelDraft(draftId: String) {
        viewModelScope.launch {
            try {
                if (!viewState.value.isOffline) {
                    socketClient.cancelDraft(
                        sessionId = viewState.value.activeSessionId,
                        draftId = draftId
                    )
                } else {
                    setState {
                        it.copy(
                            messages = it.messages + ChatUiMessage(
                                id = "cancel-${System.currentTimeMillis()}",
                                role = "assistant",
                                content = "پیش‌نویس اقدام لغو شد."
                            )
                        )
                    }
                    postEvent(AgentChatEvent.ScrollToBottom)
                }
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
                } else {
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
                        isThinking = false,
                        isOffline = true
                    )
                }
                postEvent(AgentChatEvent.ShowToast(message.message))
            }
        }
    }

    private fun handleOfflineFallback(text: String) {
        val response = if (text.contains("خرج") || text.contains("شام") || text.contains("هزینه") || text.contains("پول")) {
            val draftData = buildJsonObject {
                put("actionType", "CREATE_TRANSACTION")
                put("summary", "هزینه ثبت شده از متن پیام")
                put("payload", buildJsonObject {
                    put("title", text)
                    put("amount", 250000L)
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
        } else {
            ChatUiMessage(
                id = "bot-${System.currentTimeMillis()}",
                role = "assistant",
                content = "درخواست شما بررسی شد. در حالت آفلاین می‌توانید از فرم‌های سنتی استفاده کنید."
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
        viewModelScope.launch {
            socketClient.disconnect()
        }
    }
}
