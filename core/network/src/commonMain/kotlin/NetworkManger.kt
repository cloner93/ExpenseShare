import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.path
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable


@Serializable
data class SuccessResponse<T>(
    val success: Boolean = true,
    val data: T
)

typealias ApiResult<T> = Flow<Result<T>>

class NetworkManager(
    val client: HttpClient
) {
     fun <T> safeNetworkCall(block: suspend () -> SuccessResponse<T>): Flow<Result<T>> =
        flow {
            val response = block()
            if (response.success) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(IllegalStateException("Request failed")))
            }
        }.catch { e -> emit(Result.failure(e)) }

    inline fun <reified T> get(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T> = safeNetworkCall {
        client.get {
            url {
                path(endpoint)
                params.forEach { (k, v) -> parameters.append(k, v) }
            }
            headers.forEach { (k, v) -> header(k, v) }
        }.body()
    }

    inline fun <reified Req, reified Res> post(
        endpoint: String,
        body: Req? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<Res> = safeNetworkCall {
        client.post {
            url { path(endpoint) }
            headers.forEach { (k, v) -> header(k, v) }
            body?.let { setBody(it) }
        }.body()
    }

    inline fun <reified Req, reified Res> put(
        endpoint: String,
        body: Req? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<Res> = safeNetworkCall {
        client.put {
            url { path(endpoint) }
            headers.forEach { (k, v) -> header(k, v) }
            body?.let { setBody(it) }
        }.body()
    }

    inline fun <reified T> delete(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T> = safeNetworkCall {
        client.delete {
            url {
                path(endpoint)
                params.forEach { (k, v) -> parameters.append(k, v) }
            }
            headers.forEach { (k, v) -> header(k, v) }
        }.body()
    }
}