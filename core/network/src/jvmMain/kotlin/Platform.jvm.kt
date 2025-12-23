
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import java.util.logging.Level
import java.util.logging.Logger

actual fun getKtorEngine(): HttpClientEngine = CIO.create()

actual object AppLogger {
    private val logger: Logger = Logger.getLogger(AppLogger::class.java.name)

    init {
        logger.level = Level.FINE
    }

    actual fun e(
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (throwable != null) {
            logger.log(Level.SEVERE, "ERROR: [$tag] $message", throwable)
        } else {
            logger.severe("ERROR: [$tag] $message")
        }
    }

    actual fun d(tag: String, message: String) {
        logger.info("DEBUG: [$tag] $message")
    }

    actual fun i(tag: String, message: String) {
        logger.info("INFO: [$tag] $message")
    }
}