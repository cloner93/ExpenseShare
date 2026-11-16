package org.milad.expense_share.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import org.koin.compose.viewmodel.koinViewModel
import org.milad.expense_share.ai.AgentChatAction
import org.milad.expense_share.ai.AgentChatEvent
import org.milad.expense_share.ai.AgentChatViewModel

data class ChatUiMessage(
    val id: String,
    val role: String,
    val content: String,
    val cardType: String? = null,
    val draftId: String? = null,
    val cardData: JsonObject? = null,
    val isThinking: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentChatScreen(
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgentChatViewModel = koinViewModel()
) {
    val state by viewModel.viewState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.isThinking) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvent.collect { event ->
            when (event) {
                is AgentChatEvent.ScrollToBottom -> {
                    if (state.messages.isNotEmpty()) {
                        listState.animateScrollToItem(state.messages.size - 1)
                    }
                }
                is AgentChatEvent.ShowToast -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "دستیار هوشمند مالی",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.isOffline) "حالت آفلاین (استفاده از پایگاه محلی)" else "برخط و متصل به سرور",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.isOffline) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToDashboard) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "فرم‌های سنتی"
                            )
                        }
                    }
                )

                // Offline fallback banner with one-tap navigation to traditional dashboard
                AnimatedVisibility(visible = state.isOffline) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "اتصال وب‌سوکت قطع است. دستیار به حالت آفلاین منتقل شد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Row {
                                TextButton(onClick = { viewModel.handle(AgentChatAction.Reconnect) }) {
                                    Text(text = "تلاش مجدد")
                                }
                                TextButton(onClick = onNavigateToDashboard) {
                                    Text(text = "داشبورد دستی")
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Voice recording status indicator
                    AnimatedVisibility(visible = state.isRecordingVoice) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "در حال شنیدن صدای شما... (صحبت کنید)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                TextButton(onClick = {
                                    viewModel.handle(AgentChatAction.SetRecordingVoice(false))
                                    viewModel.handle(AgentChatAction.UpdateInputText("۳۵۰ هزار تومان پول شام با علی و رضا"))
                                }) {
                                    Text(text = "اتمام ضبط")
                                }
                            }
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Multimodal receipt scan button
                        IconButton(
                            onClick = {
                                viewModel.handle(
                                    AgentChatAction.SendReceiptImage(
                                        imageBase64 = "base64-receipt-stub",
                                        summary = "[تصویر فاکتور رستوران پیوست شد]"
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = "اسکن رسید",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Voice recording button
                        IconButton(
                            onClick = {
                                viewModel.handle(AgentChatAction.SetRecordingVoice(!state.isRecordingVoice))
                            }
                        ) {
                            Icon(
                                imageVector = if (state.isRecordingVoice) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "ورودی صوتی",
                                tint = if (state.isRecordingVoice) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Text Field
                        TextField(
                            value = state.inputText,
                            onValueChange = { viewModel.handle(AgentChatAction.UpdateInputText(it)) },
                            placeholder = { Text(text = "پیام یا دستور مالی خود را بنویسید...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 3
                        )

                        // Send Button
                        IconButton(
                            onClick = {
                                viewModel.handle(AgentChatAction.SendTextMessage(state.inputText))
                            },
                            enabled = state.inputText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "ارسال",
                                tint = if (state.inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            items(state.messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    if (msg.content.isNotBlank()) {
                        // Chat bubble
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = msg.content,
                                modifier = Modifier.padding(12.dp),
                                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Inline A2UI Generative Card (if any)
                    if (msg.cardType != null && msg.cardData != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        A2uiComponentCatalog.Render(
                            cardType = msg.cardType,
                            draftId = msg.draftId,
                            data = msg.cardData,
                            onConfirmDraft = { draftId, actionType, payload ->
                                viewModel.handle(AgentChatAction.ConfirmDraft(draftId, actionType, payload))
                            },
                            onCancelDraft = { draftId ->
                                viewModel.handle(AgentChatAction.CancelDraft(draftId))
                            },
                            onSelectOption = { selectedOption ->
                                viewModel.handle(AgentChatAction.SelectOption(selectedOption))
                            },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        )
                    }
                }
            }

            // Thinking pulsating bubble
            if (state.isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.thinkingText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}


