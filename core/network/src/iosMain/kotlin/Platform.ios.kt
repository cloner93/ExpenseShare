import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun getKtorEngine(): HttpClientEngine = Darwin.create()