package org.milad.expense_share.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<Message> = initialMessages,
    navigateToProfile: (String) -> Unit = {},
    onAddMessage: (Message) -> Unit = {},
    modifier: Modifier = Modifier,
) {

    val authorMe = "me"
    val timeNow = "8:30 PM"

    val scrollState = rememberLazyListState()
    var text by remember { mutableStateOf("") }


    var background by remember {
        mutableStateOf(Color.Transparent)
    }

    var borderStroke by remember {
        mutableStateOf(Color.Transparent)
    }

    LaunchedEffect(Unit) {
        scrollState.scrollToItem(0)
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(color = background)
            .border(width = 2.dp, color = borderStroke),
    ) {
        Messages(
            messages = messages,
            navigateToProfile = navigateToProfile,
            modifier = Modifier.weight(1f),
            scrollState = scrollState,
        )
        ChatInputBar(
            value = text,
            onValueChange = { text = it },
            onSend = {
                if (text.isNotBlank()) {
                    onAddMessage(Message(authorMe, text, timeNow))
                    println(text)
                    text = ""
                }
            }
        )
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
//            .background(AppTheme.colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Message…") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AppTheme.colors.primary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.primaryContainer)
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Send,
                tint = AppTheme.colors.onPrimaryContainer,
                contentDescription = null
            )
        }
    }
}


@Composable
fun Messages(
    messages: List<Message>,
    navigateToProfile: (String) -> Unit,
    modifier: Modifier,
    scrollState: LazyListState,
) {
    Box(modifier = modifier) {
        val authorMe = "me"
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier
//                .testTag(ConversationTestTag)
                .fillMaxSize(),
        ) {

            for (index in messages.indices) {
                val prevAuthor = messages.getOrNull(index - 1)?.author
                val nextAuthor = messages.getOrNull(index + 1)?.author
                val content = messages[index]
                val isFirstMessageByAuthor = prevAuthor != content.author
                val isLastMessageByAuthor = nextAuthor != content.author

                // Hardcode day dividers for simplicity
                if (index == messages.size - 1) {
                    item {
                        Text("20 Aug")
                    }
                } else if (index == 2) {
                    item {
                        Text("Today")
                    }
                }

                item {
                    Message(
                        msg = content,
                        isUserMe = content.author == authorMe,
                        isFirstMessageByAuthor = isFirstMessageByAuthor,
                        isLastMessageByAuthor = isLastMessageByAuthor,
                    )
                }
            }
        }
    }
}

@Composable
fun Message(
    msg: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
) {

    val borderColor = if (isUserMe) {
        AppTheme.colors.primary
    } else {
        AppTheme.colors.tertiary
    }
    val messageColor = if (isUserMe) {
        AppTheme.colors.primary
    } else {
        AppTheme.colors.surfaceVariant
    }
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier

    Row(
        modifier = spaceBetweenAuthors
            .fillMaxWidth(),
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start
    ) {

        if (!isUserMe) {
            Avatar(isLastMessageByAuthor, borderColor)
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f, fill = false)
        ) {
            AuthorHeader(isUserMe, msg, isLastMessageByAuthor)
            MessageBubble(isUserMe, msg, messageColor)
        }

        if (isUserMe) {
            Avatar(isLastMessageByAuthor, borderColor)
        }
    }

}

@Composable
private fun Avatar(show: Boolean, borderColor: Color) {
    if (show) {
        Image(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(42.dp)
                .border(1.5.dp, borderColor, CircleShape)
                .clip(CircleShape),
            painter = painterResource(Res.drawable.paris),
            contentDescription = null
        )
    } else {
        Spacer(modifier = Modifier.width(74.dp))
    }
}

@Composable
private fun AuthorHeader(isUserMe: Boolean, msg: Message, isLastMessageByAuthor: Boolean) {
    if (!isLastMessageByAuthor) {
        Spacer(modifier = Modifier.height(8.dp))
        return
    }

    Row {
        if (!isUserMe) {
            Text(msg.author)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(msg.timestamp)
    }
}

@Composable
private fun MessageBubble(isUserMe: Boolean, msg: Message, messageColor: Color) {
    val chatShape = if (isUserMe) ChatBubbleShapeMe else ChatBubbleShape
    Surface(
        color = messageColor,
        shape = chatShape
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = msg.content,
        )
    }

    msg.image?.let {
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = messageColor,
            shape = chatShape
        ) {
            Image(
                painter = it,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(160.dp),
                contentDescription = null,
            )
        }
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
private val ChatBubbleShapeMe = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)

@Preview
@Composable
fun MessagesPreview() {
    AppTheme(content = {
        Column(modifier = Modifier.background(color = AppTheme.colors.background)) {
            ChatScreen(
                messages = initialMessages,
                navigateToProfile = {},
            )
        }
    })
}

@Preview
@Composable
fun MessagesPreview2() {
    AppTheme(true, content = {
        Column(modifier = Modifier.background(color = AppTheme.colors.background)) {
            ChatScreen(
                messages = initialMessages,
                navigateToProfile = {},
            )
        }
    })
}