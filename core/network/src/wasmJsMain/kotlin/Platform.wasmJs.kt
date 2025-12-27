
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun getKtorEngine(): HttpClientEngine = Js.create()