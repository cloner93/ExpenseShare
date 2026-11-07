package token

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.MutableStateFlow

interface TokenProvider {
    fun getToken(): BearerTokens?
    fun loadTokens(): BearerTokens? = getToken()
    fun setToken(token: BearerTokens)
    fun setToken(accessToken: String) = setToken(BearerTokens(accessToken, refreshToken = null))
    fun clearToken()
}

open class InMemoryTokenProvider : TokenProvider {
    protected val state = MutableStateFlow<BearerTokens?>(null)

    override fun getToken(): BearerTokens? = state.value

    override fun setToken(token: BearerTokens) {
        state.value = token
    }

    override fun clearToken() {
        state.value = null
    }
}

expect fun createPlatformTokenProvider(): TokenProvider