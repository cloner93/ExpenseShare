package di

import org.koin.dsl.module
import repository.AuthRepository
import repository.AuthRepositoryImpl
import repository.FriendsRepository
import repository.FriendsRepositoryImpl
import repository.GroupsRepository
import repository.GroupsRepositoryImpl
import repository.SettlementRepository
import repository.SettlementRepositoryImpl
import repository.TransactionsRepository
import repository.TransactionsRepositoryImpl
import repository.UserRepository
import repository.UserRepositoryImpl

import repository.UserStorage
import repository.createPlatformUserStorage

val dataModule = module {
    single<UserStorage> { createPlatformUserStorage() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<FriendsRepository> { FriendsRepositoryImpl(get()) }
    single<GroupsRepository> { GroupsRepositoryImpl(get()) }
    single<SettlementRepository> { SettlementRepositoryImpl(get()) }
    single<TransactionsRepository> { TransactionsRepositoryImpl(get()) }
}


val dataAggregator = module {
    includes(networkModule)
    includes(dataModule)
}