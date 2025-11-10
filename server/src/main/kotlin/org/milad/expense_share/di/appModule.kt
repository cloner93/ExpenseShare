package org.milad.expense_share.di

import org.koin.dsl.module
import org.milad.expense_share.data.repository.FriendRepositoryImpl
import org.milad.expense_share.data.repository.GroupRepositoryImpl
import org.milad.expense_share.data.repository.TransactionRepositoryImpl
import org.milad.expense_share.data.repository.UserRepositoryImpl
import org.milad.expense_share.domain.repository.FriendRepository
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService

val appModule = module {

    single { FriendRepositoryImpl() as FriendRepository }
    single { UserRepositoryImpl() as UserRepository }
    single { GroupRepositoryImpl() as GroupRepository }
    single { TransactionRepositoryImpl() as TransactionRepository }

    single { AuthService(get()) }
    single { FriendsService(get()) }
    single { GroupService(get(), get(), get()) }
    single { TransactionService(get()) }
}