package org.milad.expense_share

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.milad.expense_share.di.appModules

fun main() = application {
    startKoin {
        printLogger(Level.INFO)
        modules(appModules)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "ExpenseShare",
    ) {
        AppEntryPoint()
    }
}