package token

import io.ktor.client.plugins.auth.providers.BearerTokens
import org.milad.expense_share.network.wasm.localStorage

class LocalStorageTokenProvider : TokenProvider {
    private val accessTokenKey = "expense_share_access_token"
    private val refreshTokenKey = "expense_share_refresh_token"

    override fun getToken(): BearerTokens? {
        val access = try {
            localStorage.getItem(accessTokenKey)
        } catch (_: Throwable) {
            null
        } ?: return null

        val refresh = try {
            localStorage.getItem(refreshTokenKey)
        } catch (_: Throwable) {
            null
        }
        return BearerTokens(access, refresh)
    }

    override fun setToken(token: BearerTokens) {
        try {
            localStorage.setItem(accessTokenKey, token.accessToken)
            val refresh = token.refreshToken
            if (refresh != null) {
                localStorage.setItem(refreshTokenKey, refresh)
            } else {
                localStorage.removeItem(refreshTokenKey)
            }
        } catch (_: Throwable) {
            // Ignored
        }
    }

    override fun clearToken() {
        try {
            localStorage.removeItem(accessTokenKey)
            localStorage.removeItem(refreshTokenKey)
        } catch (_: Throwable) {
            // Ignored
        }
    }
}

actual fun createPlatformTokenProvider(): TokenProvider = LocalStorageTokenProvider()
