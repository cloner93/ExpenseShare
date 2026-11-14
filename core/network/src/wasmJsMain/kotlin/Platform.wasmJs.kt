import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.milad.expense_share.network.wasm.localStorage

actual fun getKtorEngine(): HttpClientEngine = Js.create()

actual fun getDefaultBaseUrl(): String {
    val configuredUrl = try {
        localStorage.getItem("EXPENSE_SHARE_BASE_URL")
    } catch (_: Throwable) {
        null
    }

    if (!configuredUrl.isNullOrBlank()) {
        return if (configuredUrl.endsWith("/")) configuredUrl else "$configuredUrl/"
    }

    return "https://expenseshare-server.liara.run/"
}
