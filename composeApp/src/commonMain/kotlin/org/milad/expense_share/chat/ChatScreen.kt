package org.milad.expense_share.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun MessagesPreview() {
    ChatScreen(
        messages = initialMessages,
        navigateToProfile = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<Message> = initialMessages,
    navigateToProfile: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {

    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
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
            .background(MaterialTheme.colorScheme.surface)
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
                    cursorColor = MaterialTheme.colorScheme.primary,
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
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Text("➤", color = Color.White)
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
    val scope = rememberCoroutineScope()
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
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val messageColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier

    Row(
        modifier = spaceBetweenAuthors
    ) {
        // img
        if (isLastMessageByAuthor) Image(
            modifier = Modifier
                .clickable(onClick = { })
                .padding(horizontal = 16.dp)
                .size(42.dp)
                .border(1.5.dp, borderColor, CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .align(Alignment.Top),
            painter = painterResource(Res.drawable.paris),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        ) else
            Spacer(modifier = Modifier.width(74.dp))
        // column
        Column(
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)

        ) {
            if (isLastMessageByAuthor) Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                Text(
                    text = msg.author,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .alignBy(LastBaseline)
                        .paddingFrom(LastBaseline, after = 8.dp), // Space to 1st bubble
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = msg.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alignBy(LastBaseline),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = messageColor,
                shape = ChatBubbleShape,
                modifier = Modifier
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
                    shape = ChatBubbleShape,
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
    }
}

@Preview(showBackground = true)
@Composable
fun MessageP() {
    Column {
        Message(
            msg = Message(
                author = "me",
                content = "Check it out!",
                timestamp = "14:25",
            ),
            isUserMe = false,
            isFirstMessageByAuthor = true,
            isLastMessageByAuthor = true,
        )
        Message(
            msg = Message(
                author = "me",
                content = "Check it out!",
                timestamp = "14:25",
                image = painterResource(Res.drawable.paris),
            ),
            isUserMe = false,
            isFirstMessageByAuthor = false,
            isLastMessageByAuthor = false,
        )
        Message(
            msg = Message(
                author = "me",
                content = "Check it out ❤️!",
                timestamp = "14:25",
            ),
            isUserMe = true,
            isFirstMessageByAuthor = true,
            isLastMessageByAuthor = true,
        )
        Message(
            msg = Message(
                author = "me",
                content = "Check it out!",
                timestamp = "14:25",
                image = painterResource(Res.drawable.paris),
            ),
            isUserMe = true,
            isFirstMessageByAuthor = true,
            isLastMessageByAuthor = false,
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
