package org.milad.expense_share.di

import org.koin.dsl.module
import org.milad.expense_share.data.repository.*
import org.milad.expense_share.domain.repository.*
import org.milad.expense_share.domain.service.*

val appModule = module {

    single { FriendRepositoryImpl() as FriendRepository }
    single { UserRepositoryImpl() as UserRepository }
    single { GroupRepositoryImpl() as GroupRepository }
    single { TransactionRepositoryImpl() as TransactionRepository }
    single { SettlementRepositoryImpl() as SettlementRepository }

    single { AuthService(get()) }
    single { FriendsService(get(), get()) }
    single { GroupService(get(), get(), get()) }
    single { SettlementService(get(), get(), get(), get()) }
    single { TransactionService(get(), get()) }

}