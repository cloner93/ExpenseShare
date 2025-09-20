import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun getKtorEngine(): HttpClientEngineFactory<*> = CIO