
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getKtorEngine(): HttpClientEngine = OkHttp.create()

actual fun getDefaultBaseUrl(): String {
    val overrideUrl = System.getProperty("expense_share.base_url")
        ?.takeIf { it.isNotBlank() }
        ?: "https://expenseshare-server.liara.run/"

    return if (overrideUrl.endsWith("/")) overrideUrl else "$overrideUrl/"
}
