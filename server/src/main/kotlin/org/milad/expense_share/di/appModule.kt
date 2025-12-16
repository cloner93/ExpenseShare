package org.milad.expense_share.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module
import org.milad.expense_share.data.repository.ChatRepositoryImpl
import org.milad.expense_share.data.repository.FriendRepositoryImpl
import org.milad.expense_share.data.repository.GroupRepositoryImpl
import org.milad.expense_share.data.repository.TransactionRepositoryImpl
import org.milad.expense_share.data.repository.UserRepositoryImpl
import org.milad.expense_share.domain.repository.ChatRepository
import org.milad.expense_share.domain.repository.FriendRepository
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.BotService
import org.milad.expense_share.domain.service.ChatService
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService

val appModule = module {

    single { FriendRepositoryImpl() as FriendRepository }
    single { UserRepositoryImpl() as UserRepository }
    single { GroupRepositoryImpl() as GroupRepository }
    single { TransactionRepositoryImpl() as TransactionRepository }
    single { ChatRepositoryImpl() as ChatRepository }

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    single {
        BotService(
            httpClient = get(),
            groupRepository = get(),
            transactionRepository = get(),
            apiKey = System.getenv("OPENROUTER_API_KEY"),
            apiUrl = "https://openrouter.ai/api/v1/chat/completions",
            model = "amazon/nova-2-lite-v1:free"
        )
    }

    single { AuthService(get()) }
    single { FriendsService(get()) }
    single { GroupService(get(), get(), get()) }
    single { TransactionService(get()) }
    single { ChatService(get(), get(), get()) }
}