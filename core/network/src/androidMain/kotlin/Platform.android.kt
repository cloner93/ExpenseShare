import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpEngine

actual fun getKtorEngine(): HttpClientEngine = OkHttp.create()