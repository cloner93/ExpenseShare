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

val dataModule = module {

    single { UserRepositoryImpl() as UserRepository }
    single { AuthRepositoryImpl(get(), get(), get()) as AuthRepository }
    single { FriendsRepositoryImpl(get()) as FriendsRepository }
    single { GroupsRepositoryImpl(get()) as GroupsRepository }
    single { SettlementRepositoryImpl(get()) as SettlementRepository }
    single { TransactionsRepositoryImpl(get()) as TransactionsRepository }

}

val dataAggregator = module {
    includes(networkModule)
    includes(dataModule)
}