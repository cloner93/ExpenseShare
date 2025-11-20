package org.milad.expense_share.di

import di.dataAggregator
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.milad.expense_share.auth.login.LoginViewModel
import org.milad.expense_share.auth.register.RegisterViewModel
import org.milad.expense_share.dashboard.DashboardViewModel
import usecase.auth.LoginUserUseCase
import usecase.auth.RegisterUserUseCase
import usecase.friends.GetFriendsUseCase
import usecase.groups.CreateGroupUseCase
import usecase.groups.GetGroupsUseCase
import usecase.transactions.CreateTransactionUseCase
import usecase.transactions.GetTransactionsUseCase

val domainModule = module {
    factory { GetGroupsUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
    factory { RegisterUserUseCase(get()) }
    factory { LoginUserUseCase(get()) }
    factory { CreateGroupUseCase(get()) }
    factory { GetFriendsUseCase(get()) }
    factory { CreateTransactionUseCase(get()) }
}
val dashboardModule = module {
    viewModel {
        DashboardViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}
val registerModule = module {
    viewModel {
        RegisterViewModel(
            get()
        )
    }
}
val loginModule = module {
    viewModel {
        LoginViewModel(
            get()
        )
    }
}

val appModules = module {
    includes(domainModule)
    includes(dataAggregator)
    includes(dashboardModule, registerModule, loginModule)
}