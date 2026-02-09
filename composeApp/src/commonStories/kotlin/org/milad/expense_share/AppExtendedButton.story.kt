package org.milad.expense_share

import org.jetbrains.compose.storytale.story
import org.milad.expense_share.dashboard.AppExtendedButton

val AppExtendedButtonStory by story {
   val title by parameter("add member")
    AppExtendedButton(title = title , onClick = {})
}