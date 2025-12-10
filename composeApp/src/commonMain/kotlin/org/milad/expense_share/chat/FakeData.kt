package org.milad.expense_share.chat

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.painter.Painter
import org.milad.expense_share.chat.EMOJIS.EMOJI_CLOUDS
import org.milad.expense_share.chat.EMOJIS.EMOJI_FLAMINGO
import org.milad.expense_share.chat.EMOJIS.EMOJI_MELTING
import org.milad.expense_share.chat.EMOJIS.EMOJI_PINK_HEART
import org.milad.expense_share.chat.EMOJIS.EMOJI_POINTS

var initialMessages = mutableListOf<Message>(
        Message(
            "me",
            "Check it out!",
            "8:07 PM",
        ),
        Message(
            "me",
            "Thank you!$EMOJI_PINK_HEART",
            "8:06 PM",
        ),
        Message(
            "Taylor Brooks",
            "You can use all the same stuff",
            "8:05 PM",
        ),
        Message(
            "Taylor Brooks",
            "@aliconors Take a look at the `Flow.collectAsStateWithLifecycle()` APIs",
            "8:05 PM",
        ),
        Message(
            "John Glenn",
            "Compose newbie as well $EMOJI_FLAMINGO, have you looked at the JetNews sample? " +
                "Most blog posts end up out of date pretty fast but this sample is always up to " +
                "date and deals with async data loading (it's faked but the same idea " +
                "applies) $EMOJI_POINTS https://goo.gle/jetnews",
            "8:04 PM",
        ),
        Message(
            "me",
            "Compose newbie: I’ve scourged the internet for tutorials about async data " +
                "loading but haven’t found any good ones $EMOJI_MELTING $EMOJI_CLOUDS. " +
                "What’s the recommended way to load async data and emit composable widgets?",
            "8:03 PM",
        ),
        Message(
            "Shangeeth Sivan",
            "Does anyone know about Glance Widgets its the new way to build widgets in Android!",
            "8:08 PM",
        ),
        Message(
            "Taylor Brooks",
            "Wow! I never knew about Glance Widgets when was this added to the android ecosystem",
            "8:10 PM",
        ),
        Message(
            "John Glenn",
            "Yeah its seems to be pretty new!",
            "8:12 PM",
        ),
)

val unreadMessages = initialMessages.filter { it.author != "me" }

val exampleUiState = ConversationUiState(
    initialMessages = initialMessages,
    channelName = "#composers",
    channelMembers = 42,
)

/**
 * Example colleague profile
 */
val colleagueProfile = ProfileScreenState(
    userId = "12345",
//    photo = R.drawable.someone_else,
    name = "Taylor Brooks",
    status = "Away",
    displayName = "taylor",
    position = "Senior Android Dev at Openlane",
    twitter = "twitter.com/taylorbrookscodes",
    timeZone = "12:25 AM local time (Eastern Daylight Time)",
    commonChannels = "2",
)

/**
 * Example "me" profile.
 */
val meProfile = ProfileScreenState(
    userId = "me",
//    photo = R.drawable.ali,
    name = "Ali Conors",
    status = "Online",
    displayName = "aliconors",
    position = "Senior Android Dev at Yearin\nGoogle Developer Expert",
    twitter = "twitter.com/aliconors",
    timeZone = "In your timezone",
    commonChannels = null,
)

object EMOJIS {
    // EMOJI 15
    const val EMOJI_PINK_HEART = "\uD83E\uDE77"

    // EMOJI 14 🫠
    const val EMOJI_MELTING = "\uD83E\uDEE0"

    // ANDROID 13.1 😶‍🌫️
    const val EMOJI_CLOUDS = "\uD83D\uDE36\u200D\uD83C\uDF2B️"

    // ANDROID 12.0 🦩
    const val EMOJI_FLAMINGO = "\uD83E\uDDA9"

    // ANDROID 12.0  👉
    const val EMOJI_POINTS = " \uD83D\uDC49"
}


@Immutable
data class Message(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Painter? = null,
    val authorImage: Int? = null, /*= if (author == "me") R.drawable.ali else R.drawable.someone_else*/
)

@Immutable
data class ProfileScreenState(
    val userId: String,
//    @DrawableRes val photo: Int?,
    val name: String,
    val status: String,
    val displayName: String,
    val position: String,
    val twitter: String = "",
    val timeZone: String?, // Null if me
    val commonChannels: String?, // Null if me
) {
    fun isMe() = userId == meProfile.userId
}


class ConversationUiState(
    val channelName: String,
    val channelMembers: Int,
    initialMessages: List<Message>,
) {
    private val _messages: MutableList<Message> = initialMessages.toMutableStateList()
    val messages: List<Message> = _messages

    fun addMessage(msg: Message) {
        _messages.add(0, msg) // Add to the beginning of the list
    }
}