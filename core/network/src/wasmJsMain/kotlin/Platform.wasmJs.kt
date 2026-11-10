import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.milad.expense_share.network.wasm.localStorage
import org.milad.expense_share.network.wasm.window

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

    val hostname = try {
        window.location.hostname
    } catch (_: Throwable) {
        ""
    }

    if (hostname.isNotEmpty() && hostname != "localhost" && hostname != "127.0.0.1") {
        val origin = try {
            window.location.origin
        } catch (_: Throwable) {
            ""
        }
        if (origin.isNotEmpty()) {
            return if (origin.endsWith("/")) origin else "$origin/"
        }
    }

    return "https://expenseshare-server.liara.run/"
}
