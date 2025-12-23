import io.ktor.client.engine.HttpClientEngine

expect fun getKtorEngine(): HttpClientEngine

expect object AppLogger {
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
}