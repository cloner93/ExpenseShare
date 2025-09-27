package di

import NetworkManager
import client.ApiClient
import client.KtorApiClient
import client.createHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import token.InMemoryTokenProvider
import token.TokenProvider

val networkModule: Module = module {
    single { InMemoryTokenProvider() as TokenProvider }
    single { createHttpClient(get()) }
    single { KtorApiClient(get()) as ApiClient }
    single { NetworkManager(get()) }
}