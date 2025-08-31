package org.milad.expense_share

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ExpenseShare",
    ) {
        AppEntryPoint()
    }
}