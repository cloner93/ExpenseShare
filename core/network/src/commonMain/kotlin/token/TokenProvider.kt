package token

import io.ktor.client.plugins.auth.providers.BearerTokens

interface TokenProvider {
    suspend fun loadTokens(): BearerTokens?
    suspend fun setToken(tokens: BearerTokens)
    suspend fun clearToken()
}

internal class InMemoryTokenProvider : TokenProvider {

    private var tokens: BearerTokens? = null


    override suspend fun loadTokens(): BearerTokens? = tokens

    override suspend fun setToken(tokens: BearerTokens) {
        this.tokens = tokens
    }

    override suspend fun clearToken() {
        this.tokens = null
    }
}