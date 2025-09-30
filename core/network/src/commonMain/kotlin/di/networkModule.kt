package di

import NetworkManager
import NetworkManagerImpl
import client.ApiClient
import client.KtorApiClient
import client.createHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import token.InMemoryTokenProvider
import token.TokenProvider

val networkModule: Module = module {
    single { InMemoryTokenProvider() as TokenProvider }
    single { createHttpClient(get<TokenProvider>()) }
    single { KtorApiClient(get()) as ApiClient }
    single { NetworkManagerImpl(get()) as NetworkManager }
}