
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun getKtorEngine(): HttpClientEngine = Js.create()
actual fun getDefaultBaseUrl(): String = "http://127.0.0.1:8082/"