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
import org.milad.expense_share.normalizePersianDigits

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
            role = "agent",
            content = "سلام! من ایجنت هوشمند ExpenseShare هستم.\nمی‌توانید به زبان محاوره‌ای هزینه‌ها را بگویید، فاکتور بفرستید، یا وضعیت بدهی‌ها را بپرسید."
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
                sendMessage(action.option)
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

    private var messageIdCounter = 0L
    private fun nextId(prefix: String): String = "$prefix-${++messageIdCounter}-${kotlin.random.Random.nextInt(10000, 99999)}"

    private fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val userMessage = ChatUiMessage(
            id = nextId("user"),
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
            id = nextId("img"),
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
                setState {
                    it.copy(
                        isThinking = false,
                        messages = it.messages + ChatUiMessage(
                            id = nextId("agent-error"),
                            role = "agent",
                            content = "ارسال تصویر فاکتور با خطا مواجه شد. لطفاً اتصال اینترنت را بررسی کنید."
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

                if (lastMsg != null && lastMsg.role == "agent" && lastMsg.cardType == null && !lastMsg.isThinking) {
                    newMessages[newMessages.lastIndex] = lastMsg.copy(
                        content = lastMsg.content + message.text
                    )
                } else if (message.text.isNotBlank()) {
                    newMessages.add(
                        ChatUiMessage(
                            id = nextId("agent"),
                            role = "agent",
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
                    id = nextId("card"),
                    role = "agent",
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
                    id = nextId("exec"),
                    role = "agent",
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
        setState {
            it.copy(
                isThinking = false,
                messages = it.messages + ChatUiMessage(
                    id = nextId("agent"),
                    role = "agent",
                    content = "ارتباط با سرور برقرار نیست. در حالت آفلاین می‌توانید از فرم‌های سنتی در داشبورد استفاده کنید یا دکمه تلاش مجدد را بزنید."
                )
            )
        }
        postEvent(AgentChatEvent.ScrollToBottom)
    }

    override fun onCleared() {
        super.onCleared()
        socketClient.stop()
    }
}
