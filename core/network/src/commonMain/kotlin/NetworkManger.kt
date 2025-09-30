import client.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.path
import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable


@Serializable
data class SuccessResponse<T>(
    val success: Boolean = true,
    val data: T
)

typealias ApiResult<T> = Flow<Result<T>>

@Mockable
interface NetworkManager {
    fun <T> safeNetworkCall(block: suspend () -> SuccessResponse<T>): Flow<Result<T>> =
        flow {
            val response = block()
            if (response.success) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(IllegalStateException("Request failed")))
            }
        }.catch { e -> emit(Result.failure(e)) }

    suspend fun <T> get(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T>

    suspend fun <Req, Res> post(
        endpoint: String,
        body: Req? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<Res>

    suspend fun <Req, Res> put(
        endpoint: String,
        body: Req? = null,
        headers: Map<String, String> = emptyMap()
    ): ApiResult<Res>

    suspend fun <T> delete(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResult<T>
}

class NetworkManagerImpl(val client: ApiClient) : NetworkManager {
    override suspend fun <T> get(
        endpoint: String,
        params: Map<String, String>,
        headers: Map<String, String>
    ): ApiResult<T> = safeNetworkCall {
        client.get {
            url {
                path(endpoint)
                params.forEach { (k, v) -> parameters.append(k, v) }
            }
            headers.forEach { (k, v) -> header(k, v) }
        }.body()
    }

    override suspend fun <Req, Res> post(
        endpoint: String,
        body: Req?,
        headers: Map<String, String>
    ): ApiResult<Res> = safeNetworkCall {
        client.post {
            url { path(endpoint) }
            headers.forEach { (k, v) -> header(k, v) }
            body?.let { setBody(it) }
        }.body()
    }

    override suspend fun <Req, Res> put(
        endpoint: String,
        body: Req?,
        headers: Map<String, String>
    ): ApiResult<Res> = safeNetworkCall {
        client.put {
            url { path(endpoint) }
            headers.forEach { (k, v) -> header(k, v) }
            body?.let { setBody(it) }
        }.body()
    }

    override suspend fun <T> delete(
        endpoint: String,
        params: Map<String, String>,
        headers: Map<String, String>
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