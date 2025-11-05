package org.milad.expense_share.di

import di.dataAggregator
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.milad.expense_share.dashboard.DashboardViewModel
import usecase.groups.GetGroupsUseCase
import usecase.transactions.GetTransactionsUseCase

val domainModule = module {
    factory { GetGroupsUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
}
val presentationModule = module {
    viewModel {
        DashboardViewModel(
            get(),
            get()
        )
    }
}

val appModules = module{
    includes(domainModule)
    includes(dataAggregator)
    includes(presentationModule)
}