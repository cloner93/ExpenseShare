package org.milad.expense_share.di

import di.dataAggregator
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.milad.expense_share.auth.login.LoginViewModel
import org.milad.expense_share.auth.register.RegisterViewModel
import org.milad.expense_share.dashboard.DashboardViewModel
import org.milad.expense_share.dashboard.group.GroupDetailViewModel
import org.milad.expense_share.friends.FriendsViewModel
import org.milad.expense_share.friends.detail.FriendDetailViewModel
import usecase.auth.LoginUserUseCase
import usecase.auth.RegisterUserUseCase
import usecase.friends.AcceptFriendRequestUseCase
import usecase.friends.BlockFriendUseCase
import usecase.friends.CancelFriendRequestUseCase
import usecase.friends.GetAcceptedFriendsUseCase
import usecase.friends.GetAllFriendsUseCase
import usecase.friends.GetBlockedFriendsUseCase
import usecase.friends.GetFriendshipStatusUseCase
import usecase.friends.GetIncomingRequestsUseCase
import usecase.friends.GetOutgoingRequestsUseCase
import usecase.friends.RejectFriendRequestUseCase
import usecase.friends.RemoveFriendUseCase
import usecase.friends.SendFriendRequestUseCase
import usecase.friends.UnblockFriendUseCase
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

val friendUseCasesModule = module {
    factory { GetAllFriendsUseCase(get()) }
    factory { GetAcceptedFriendsUseCase(get()) }
    factory { GetIncomingRequestsUseCase(get()) }
    factory { GetOutgoingRequestsUseCase(get()) }
    factory { GetBlockedFriendsUseCase(get()) }
    factory { GetFriendshipStatusUseCase(get()) }

    factory { SendFriendRequestUseCase(get()) }
    factory { AcceptFriendRequestUseCase(get()) }
    factory { RejectFriendRequestUseCase(get()) }
    factory { BlockFriendUseCase(get()) }
    factory { UnblockFriendUseCase(get()) }
    factory { RemoveFriendUseCase(get()) }
    factory { CancelFriendRequestUseCase(get()) }
}

val domainModule = module {
    factory { GetGroupsUseCase(get()) }
    factory { RegisterUserUseCase(get()) }
    factory { LoginUserUseCase(get()) }
    factory { CreateGroupUseCase(get()) }
    factory { GetUserInfoUseCase(get()) }
    factory { DeleteGroupUseCase(get()) }
    factory { UpdateGroupMembersUseCase(get()) }

    includes(friendUseCasesModule)

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
            get()
        )
    }

    viewModel {
        GroupDetailViewModel(
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
}
val friendsModule = module {
    viewModel {
        FriendsViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        FriendDetailViewModel(
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
    // feature modules
    includes(dashboardModule, registerModule, loginModule, friendsModule)
}