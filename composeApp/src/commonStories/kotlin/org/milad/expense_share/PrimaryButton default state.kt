package org.milad.expense_share

import org.jetbrains.compose.storytale.story
import org.milad.expense_share.dashboard.AppExtendedButton

val `AppExtendedButton default state` by story {
   val title by parameter("add member")
    AppExtendedButton(title = title , onClick = {})
}