package org.milad.expense_share.di

import org.koin.dsl.module
import org.milad.expense_share.data.repository.InMemoryFriendRepository
import org.milad.expense_share.data.repository.InMemoryGroupRepository
import org.milad.expense_share.data.repository.InMemoryTransactionRepository
import org.milad.expense_share.data.repository.InMemoryUserRepository
import org.milad.expense_share.domain.repository.FriendRepository
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService

val appModule = module {

    single { InMemoryFriendRepository() as FriendRepository }
    single { InMemoryUserRepository() as UserRepository }
    single { InMemoryGroupRepository() as GroupRepository }
    single { InMemoryTransactionRepository() as TransactionRepository }

    single { AuthService(get()) }
    single { FriendsService(get()) }
    single { GroupService(get(), get(), get()) }
    single { TransactionService(get()) }
}