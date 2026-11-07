
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun getKtorEngine(): HttpClientEngine = Darwin.create()
actual fun getDefaultBaseUrl(): String = "http://127.0.0.1:8082/"