package org.milad.expense_share.di

import di.dataAggregator
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.milad.expense_share.auth.login.LoginViewModel
import org.milad.expense_share.auth.register.RegisterViewModel
import org.milad.expense_share.dashboard.DashboardViewModel
import org.milad.expense_share.dashboard.group.GroupDetailViewModel
import usecase.auth.LoginUserUseCase
import usecase.auth.RegisterUserUseCase
import usecase.friends.GetFriendsUseCase
import usecase.groups.CreateGroupUseCase
import usecase.groups.DeleteGroupUseCase
import usecase.groups.GetGroupsUseCase
import usecase.groups.UpdateGroupMembersUseCase
import usecase.transactions.ApproveTransactionUseCase
import usecase.transactions.CreateTransactionUseCase
import usecase.transactions.DeleteTransactionUseCase
import usecase.transactions.GetTransactionsUseCase
import usecase.transactions.RejectTransactionUseCase
import usecase.user.GetUserInfoUseCase

val domainModule = module {
    factory { GetGroupsUseCase(get()) }
    factory { RegisterUserUseCase(get()) }
    factory { LoginUserUseCase(get()) }
    factory { CreateGroupUseCase(get()) }
    factory { GetFriendsUseCase(get()) }
    factory { GetUserInfoUseCase(get()) }
    factory { DeleteGroupUseCase(get()) }
    factory { UpdateGroupMembersUseCase(get()) }

    factory { CreateTransactionUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
    factory { ApproveTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { RejectTransactionUseCase(get()) }

}
val dashboardModule = module {
    viewModel {
        DashboardViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        GroupDetailViewModel()
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