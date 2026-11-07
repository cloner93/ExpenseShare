
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getKtorEngine(): HttpClientEngine = OkHttp.create()
actual fun getDefaultBaseUrl(): String = "http://10.0.2.2:8082/"