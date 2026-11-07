package di

import NetworkManager
import client.ApiClient
import client.HttpConfig
import client.KtorApiClient
import client.createHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module
import token.TokenProvider
import token.createPlatformTokenProvider

val networkModule: Module = module {
    single<TokenProvider> { createPlatformTokenProvider() }
    single { createHttpClient(get<TokenProvider>(), getOrNull<HttpConfig>() ?: HttpConfig()) }
    single<ApiClient> { KtorApiClient(get()) }
    single { NetworkManager(get()) }
}