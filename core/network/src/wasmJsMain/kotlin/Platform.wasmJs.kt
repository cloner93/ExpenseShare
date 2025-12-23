
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun getKtorEngine(): HttpClientEngine = Js.create()

actual object AppLogger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            println("ERROR: [$tag] $message. Throwable: ${throwable.message}")
        } else {
            println("ERROR: [$tag] $message")
        }
    }

    actual fun d(tag: String, message: String) {
        println("DEBUG: [$tag] $message")
    }

    actual fun i(tag: String, message: String) {
        println("INFO: [$tag] $message")
    }
}