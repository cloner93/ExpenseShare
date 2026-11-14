package org.milad.expense_share

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.ic_app_logo
import org.jetbrains.compose.resources.painterResource
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
        icon = painterResource(Res.drawable.ic_app_logo),
    ) {
        AppEntryPoint()
    }
}