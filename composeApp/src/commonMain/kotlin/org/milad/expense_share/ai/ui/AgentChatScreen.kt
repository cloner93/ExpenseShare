package org.milad.expense_share.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isOffline by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var thinkingText by remember { mutableStateOf("در حال تحلیل...") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            ChatUiMessage(
                id = "welcome",
                role = "assistant",
                content = "سلام! من ایجنت هوشمند شما در ExpenseShare هستم.\nمی‌توانید به زبان محاوره‌ای هزینه‌ها را بگویید، فاکتور بفرستید، یا وضعیت بدهی‌ها را بپرسید."
            )
        )
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
                                    text = if (isOffline) "حالت آفلاین" else "برخط و آماده پاسخگویی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOffline) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick switch to manual dashboard button
                        IconButton(onClick = onNavigateToDashboard) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "فرم‌های سنتی"
                            )
                        }
                    }
                )

                // Offline fallback banner with one-tap navigation to traditional dashboard
                AnimatedVisibility(visible = isOffline) {
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
                                    text = "اتصال به سرور قطع است. می‌توانید از فرم‌های سنتی استفاده کنید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            TextButton(onClick = onNavigateToDashboard) {
                                Text(text = "داشبورد دستی")
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
                    AnimatedVisibility(visible = isRecordingVoice) {
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
                                    isRecordingVoice = false
                                    inputText = "۳۵۰ هزار تومان پول شام با علی و رضا"
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
                                coroutineScope.launch {
                                    messages.add(
                                        ChatUiMessage(
                                            id = "img-${messages.size}",
                                            role = "user",
                                            content = "[تصویر رسید رستوران پیوست شد]"
                                        )
                                    )
                                    isThinking = true
                                    thinkingText = "در حال تحلیل تصویر فاکتور با هوش مصنوعی..."
                                    delay(1200)
                                    isThinking = false

                                    val draftData = buildJsonObject {
                                        put("actionType", "CREATE_TRANSACTION")
                                        put("summary", "فاکتور رستوران - ۳ قلم غذا")
                                        put("payload", buildJsonObject {
                                            put("title", "شام رستوران البرز")
                                            put("amount", 850000L)
                                            put("groupId", 1)
                                        })
                                    }

                                    messages.add(
                                        ChatUiMessage(
                                            id = "bot-${messages.size}",
                                            role = "assistant",
                                            content = "اقلام فاکتور با موفقیت استخراج شد. لطفاً پیش‌نویس زیر را تایید کنید:",
                                            cardType = "ACTION_DRAFT",
                                            draftId = "draft-receipt-1",
                                            cardData = draftData
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "اسکن رسید",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Voice recording button
                        IconButton(
                            onClick = { isRecordingVoice = !isRecordingVoice }
                        ) {
                            Icon(
                                imageVector = if (isRecordingVoice) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "ورودی صوتی",
                                tint = if (isRecordingVoice) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Text Field
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
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
                                val text = inputText.trim()
                                if (text.isNotBlank()) {
                                    messages.add(
                                        ChatUiMessage(
                                            id = "msg-${messages.size}",
                                            role = "user",
                                            content = text
                                        )
                                    )
                                    inputText = ""

                                    coroutineScope.launch {
                                        isThinking = true
                                        thinkingText = "در حال برنامه‌ریزی و اجرای ابزار..."
                                        delay(1000)
                                        isThinking = false

                                        // Simulate intelligent agent response & draft creation
                                        if (text.contains("خرج") || text.contains("شام") || text.contains("هزینه") || text.contains("پول")) {
                                            val draftData = buildJsonObject {
                                                put("actionType", "CREATE_TRANSACTION")
                                                put("summary", "هزینه ثبت شده از متن پیام")
                                                put("payload", buildJsonObject {
                                                    put("title", text)
                                                    put("amount", 250000L)
                                                    put("groupId", 1)
                                                })
                                            }
                                            messages.add(
                                                ChatUiMessage(
                                                    id = "bot-${messages.size}",
                                                    role = "assistant",
                                                    content = "پیش‌نویس هزینه بر اساس درخواست شما تهیه شد. آیا مایل به ثبت نهایی هستید؟",
                                                    cardType = "ACTION_DRAFT",
                                                    draftId = "draft-${messages.size}",
                                                    cardData = draftData
                                                )
                                            )
                                        } else if (text.contains("علی")) {
                                            // Trigger Disambiguation card
                                            val disambigData = buildJsonObject {
                                                put("prompt", "چند مخاطب با نام علی یافت شد. کدام مد نظرتان است؟")
                                                put("options", kotlinx.serialization.json.buildJsonArray {
                                                    add(kotlinx.serialization.json.JsonPrimitive("علی حسینی (گروه سفر)"))
                                                    add(kotlinx.serialization.json.JsonPrimitive("علی رضوانی (هم‌خانه)"))
                                                })
                                            }
                                            messages.add(
                                                ChatUiMessage(
                                                    id = "bot-${messages.size}",
                                                    role = "assistant",
                                                    content = "لطفاً مخاطب دقیق را مشخص فرمایید:",
                                                    cardType = "DISAMBIGUATION",
                                                    cardData = disambigData
                                                )
                                            )
                                        } else {
                                            messages.add(
                                                ChatUiMessage(
                                                    id = "bot-${messages.size}",
                                                    role = "assistant",
                                                    content = "درخواست شما بررسی شد. در صورت نیاز به ثبت هزینه، تسویه یا ایجاد گروه، کافیست جزئیات را بفرمایید."
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "ارسال",
                                tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
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

            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
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

                    // Inline A2UI Generative Card (if any)
                    if (msg.cardType != null && msg.cardData != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        A2uiComponentCatalog.Render(
                            cardType = msg.cardType,
                            draftId = msg.draftId,
                            data = msg.cardData,
                            onConfirmDraft = { draftId, actionType, payload ->
                                messages.add(
                                    ChatUiMessage(
                                        id = "confirm-${messages.size}",
                                        role = "assistant",
                                        content = "اقدام با موفقیت در سیستم ثبت گردید."
                                    )
                                )
                            },
                            onCancelDraft = { draftId ->
                                messages.add(
                                    ChatUiMessage(
                                        id = "cancel-${messages.size}",
                                        role = "assistant",
                                        content = "پیش‌نویس اقدام لغو شد."
                                    )
                                )
                            },
                            onSelectOption = { selectedOption ->
                                inputText = selectedOption
                            },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        )
                    }
                }
            }

            // Thinking pulsating bubble
            if (isThinking) {
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
                            text = thinkingText,
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
