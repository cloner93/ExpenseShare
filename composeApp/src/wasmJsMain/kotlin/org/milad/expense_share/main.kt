package org.milad.expense_share

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.milad.expense_share.di.appModules

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        printLogger(Level.INFO)
        modules(appModules)
    }

    ComposeViewport(document.body!!) {

        AppEntryPoint()
    }
}