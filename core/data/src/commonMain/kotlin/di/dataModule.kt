package di

import org.koin.dsl.module
import repository.AuthRepository
import repository.AuthRepositoryImpl
import repository.FriendsRepository
import repository.FriendsRepositoryImpl
import repository.GroupsRepository
import repository.GroupsRepositoryImpl
import repository.TransactionsRepository
import repository.TransactionsRepositoryImpl

val dataModule = module {

    single { AuthRepositoryImpl(get(),get()) as AuthRepository }
    single { FriendsRepositoryImpl(get()) as FriendsRepository }
    single { GroupsRepositoryImpl(get()) as GroupsRepository }
    single { TransactionsRepositoryImpl(get()) as TransactionsRepository }

}

val dataAggregator = module {
    includes(networkModule)
    includes(dataModule)
}