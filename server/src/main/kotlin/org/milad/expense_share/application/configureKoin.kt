package org.milad.expense_share.application

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.milad.expense_share.di.appModule

internal fun Application.configureKoin() {
    install(Koin) {
        modules(appModule)
    }
}