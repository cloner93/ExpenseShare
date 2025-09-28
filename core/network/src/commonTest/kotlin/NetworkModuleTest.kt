import client.ApiClient
import client.KtorApiClient
import di.networkModule
import io.kotest.core.spec.style.StringSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.HttpClient
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import token.TokenProvider

class NetworkModuleTest : KoinTest, StringSpec() {
    init {
        extension(KoinExtension(module = networkModule))

        "TokenProvider should be injected correctly" {
            val tokenProvider: TokenProvider by inject()
            tokenProvider shouldNotBe null
        }

        "HttpClient should be injected correctly" {
            val httpClient: HttpClient by inject()
            httpClient shouldNotBe null
        }

        "ApiClient should be injected as KtorApiClient" {
            val apiClient: ApiClient by inject()
            apiClient shouldNotBe null
            apiClient::class shouldBe KtorApiClient::class
        }

        "NetworkManager should be injected correctly" {
            val networkManager: NetworkManager by inject()
            networkManager shouldNotBe null
        }

        "All components should be single instances" {
            val networkManager1: NetworkManager by inject()
            val networkManager2: NetworkManager by inject()
            networkManager1 shouldBe networkManager2
        }
    }
}