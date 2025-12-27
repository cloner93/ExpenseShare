
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getKtorEngine(): HttpClientEngine = OkHttp.create()