import client.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import org.milad.expense_share.logger.AppLogger


@Serializable
data class SuccessResponse<T>(
    val success: Boolean = true,
    val data: T
)

typealias ApiResult<T> = Flow<Result<T>>

inline fun <reified T> safeNetworkCall(
    crossinline block: suspend () -> SuccessResponse<T>
): Flow<Result<T>> = flow {
//    delay(1500)
    val response = block()
    if (response.success) {
        AppLogger.i("NetworkManager", "Response: $response")
        emit(Result.success(response.data))
    } else {
        AppLogger.e("NetworkManager", "Response: $response")
        emit(Result.failure(IllegalStateException("Request failed")))
    }
}.catch { e ->
    AppLogger.e("NetworkManager", "Exception: ${e.stackTraceToString()}")
    emit(Result.failure(e))
}

class NetworkManager(val client: ApiClient) {

    suspend inline fun <reified T> get(
        endpoint: String,
        params: Map<String, String> = mapOf(),
        headers: Map<String, String> = mapOf()
    ): ApiResult<T> = safeNetworkCall {
        client.get {
            url {
                path(endpoint)
                params.forEach { (k, v) -> parameters.append(k, v) }
            }
            headers.forEach { (k, v) -> header(k, v) }
        }.body<SuccessResponse<T>>()
    }

    suspend inline fun <reified Req, reified Res> post(
        endpoint: String,
        body: Req? = null,
        headers: Map<String, String> = mapOf()
    ): ApiResult<Res> = safeNetworkCall {
        client.post {
            url { path(endpoint) }
            headers.forEach { (k, v) -> header(k, v) }
            body?.let { setBody(it) }
        }.body<SuccessResponse<Res>>()
    }

    suspend inline fun <reified Req, reified Res> put(
        endpoint: String,
        body: Req? = null,
        headers: Map<String, String> = mapOf()
    ): ApiResult<Res> = safeNetworkCall {
        client.put {
            url { path(endpoint) }
            headers.forEach { (k, v) -> header(k, v) }
            body?.let { setBody(it) }
        }.body<SuccessResponse<Res>>()
    }

    suspend inline fun <reified T> delete(
        endpoint: String,
        params: Map<String, String> = mapOf(),
        headers: Map<String, String> = mapOf()
    ): ApiResult<T> = safeNetworkCall {
        client.delete {
            url {
                path(endpoint)
                params.forEach { (k, v) -> parameters.append(k, v) }
            }
            headers.forEach { (k, v) -> header(k, v) }
        }.body<SuccessResponse<T>>()
    }
}