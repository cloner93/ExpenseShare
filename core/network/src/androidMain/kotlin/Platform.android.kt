
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun getKtorEngine(): HttpClientEngine = OkHttp.create()

actual fun getDefaultBaseUrl(): String {
    val prop = try {
        val clazz = Class.forName("android.os.SystemProperties")
        val getMethod = clazz.getMethod("get", String::class.java, String::class.java)
        getMethod.invoke(null, "debug.expense_share.base_url", "") as? String
    } catch (_: Exception) {
        null
    }?.takeIf { it.isNotBlank() }

    val raw = prop
        ?: System.getProperty("expense_share.base_url")
        ?: "https://expenseshare-server.liara.run/"

    return if (raw.endsWith("/")) raw else "$raw/"
}