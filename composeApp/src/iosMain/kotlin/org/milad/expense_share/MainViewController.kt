package org.milad.expense_share

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.milad.expense_share.di.appModules

fun MainViewController() = ComposeUIViewController {
    startKoin {
        printLogger(Level.INFO)
        modules(appModules)
    }
    AppEntryPoint()
}