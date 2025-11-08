package token

import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.MutableStateFlow

interface TokenProvider {
    fun loadTokens(): BearerTokens?
    fun setToken(accessToken: String)
    fun clearToken()
}

internal class InMemoryTokenProvider : TokenProvider {
    private val state = MutableStateFlow<BearerTokens?>(null)

    override fun loadTokens(): BearerTokens? = state.value

    override fun setToken(accessToken: String) {
        state.value = BearerTokens(accessToken , refreshToken = null)
    }

    override fun clearToken() {
        state.value = null
    }
}