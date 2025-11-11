package org.milad.expense_share

import android.app.Application
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.milad.expense_share.di.appModules

class ExpenseShareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ExpenseShareApp)
            androidLogger(Level.DEBUG)
            modules(appModules)

            analytics()
        }
    }
}
