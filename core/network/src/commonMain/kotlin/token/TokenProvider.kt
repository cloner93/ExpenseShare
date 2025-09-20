package token

import io.ktor.client.plugins.auth.providers.BearerTokens

interface TokenProvider {
    suspend fun loadTokens(): BearerTokens?
    suspend fun updateTokens(tokens: BearerTokens)
}

internal class InMemoryTokenProvider : TokenProvider {
    private var tokens: BearerTokens? = null

    override suspend fun loadTokens(): BearerTokens? = tokens

    override suspend fun updateTokens(tokens: BearerTokens) {
        this.tokens = tokens
    }
}