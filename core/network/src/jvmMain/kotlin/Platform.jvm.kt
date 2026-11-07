
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun getKtorEngine(): HttpClientEngine = CIO.create()

actual fun getDefaultBaseUrl(): String {
    val raw = System.getenv("EXPENSE_SHARE_BASE_URL")
        ?: System.getProperty("expense_share.base_url")
        ?: "http://127.0.0.1:8082/"
    return if (raw.endsWith("/")) raw else "$raw/"
}