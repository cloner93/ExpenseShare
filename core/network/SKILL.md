# core/network — SKILL.md

## Purpose
Provides the HTTP client infrastructure for the Compose Multiplatform app.
Handles: request building, authentication headers, error mapping, and the `NetworkManager` abstraction.

---

## Layer Architecture

```
NetworkManager          ← used by repositories in core/data
    └── ApiClient       ← interface (KtorApiClient impl)
        └── HttpClient  ← Ktor with plugins (ContentNegotiation, Timeout, Logging, ErrorHandler)
```

---

## NetworkManager — Primary Interface for Repositories

All repository calls go through `NetworkManager`. It wraps responses in `Flow<Result<T>>`.

```kotlin
class NetworkManager(val client: ApiClient) {
    suspend inline fun <reified T> get(endpoint, params, headers): ApiResult<T>
    suspend inline fun <reified Req, reified Res> post(endpoint, body, headers): ApiResult<Res>
    suspend inline fun <reified Req, reified Res> put(endpoint, body, headers): ApiResult<Res>
    suspend inline fun <reified T> delete(endpoint, params, headers): ApiResult<T>
}

typealias ApiResult<T> = Flow<Result<T>>
```

### Response Wrapper
The server always wraps responses in:
```json
{ "success": true, "data": <actual payload> }
```
`NetworkManager` unwraps this automatically via `SuccessResponse<T>`.

### safeNetworkCall Pattern
```kotlin
inline fun <reified T> safeNetworkCall(
    crossinline block: suspend () -> SuccessResponse<T>
): Flow<Result<T>> = flow {
    val response = block()
    if (response.success) emit(Result.success(response.data))
    else emit(Result.failure(IllegalStateException("Request failed")))
}.catch { e ->
    emit(Result.failure(e))  // maps exceptions to Result.failure
}
```

---

## Base URL Configuration
```kotlin
// HttpConfig defaults:
baseUrl = "http://0.0.0.0:8082"   // Change for production
timeoutMillis = 15000
```

---

## Authentication
Token is stored in `InMemoryTokenProvider` (in-memory, not persisted to disk).

```kotlin
// On every request:
tokenProvider.loadTokens()?.let {
    header(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
}
```

**No refresh token logic** — token expires after `validityMs` (configured in server `application.conf`, default 86400000ms = 24h).

### Token Lifecycle
```
Register/Login → AuthRepositoryImpl → tokenProvider.setToken(token)
Logout         → tokenProvider.clearToken()
App restart    → token is LOST (no persistence) → must login again
```

---

## Error Handling
`installErrorHandler()` maps HTTP status codes to typed exceptions:

| HTTP Status | Exception |
|-------------|-----------|
| 401 | `UnauthorizedException` |
| 404 | `NotFoundException` |
| 500-599 | `ServerException` |
| Other 4xx | `GenericApiException(code)` |

These exceptions propagate through `safeNetworkCall` and end up as `Result.failure(e)`.

---

## Platform-specific Engines

| Platform | Engine |
|----------|--------|
| Android | OkHttp |
| iOS | Darwin (URLSession) |
| JVM Desktop | CIO |
| wasmJs | Js |

Selected via `expect fun getKtorEngine(): HttpClientEngine`.

---

## ApiClient Interface
```kotlin
interface ApiClient {
    suspend fun post(builder: HttpRequestBuilder.() -> Unit): HttpResponse
    suspend fun get(builder: HttpRequestBuilder.() -> Unit): HttpResponse
    suspend fun put(builder: HttpRequestBuilder.() -> Unit): HttpResponse
    suspend fun delete(builder: HttpRequestBuilder.() -> Unit): HttpResponse
}
```
`KtorApiClient` is the production implementation. Use `MockEngine` in tests.

---

## DI (Koin)
```kotlin
val networkModule = module {
    single { InMemoryTokenProvider() as TokenProvider }
    single { createHttpClient(get<TokenProvider>()) }
    single { KtorApiClient(get()) as ApiClient }
    single { NetworkManager(get()) }
}
```
All are **singletons** — there is one shared client instance.

---

## Rules
- Repositories must use `NetworkManager`, NOT `ApiClient` directly
- Never call `client.get/post` directly from outside this module
- `InMemoryTokenProvider` is `internal` — use `TokenProvider` interface
- All endpoints are relative paths (e.g. `"/groups"`, `"/auth/login"`)
- Do NOT add business logic here — network layer only

---

## Module Dependencies
```
core/network → ktor-client-*, koin-core, kotlinx-serialization-json, core/logger
```
