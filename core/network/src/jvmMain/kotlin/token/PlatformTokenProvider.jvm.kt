package token

import io.ktor.client.plugins.auth.providers.BearerTokens
import java.util.prefs.Preferences

class PreferencesTokenProvider(
    private val preferences: Preferences = Preferences.userRoot().node("org.milad.expense_share.tokens")
) : TokenProvider {

    private var inMemoryTokens: BearerTokens? = null

    override fun getToken(): BearerTokens? {
        return try {
            val access = preferences.get("access_token", null)
            if (access != null) {
                val refresh = preferences.get("refresh_token", null)
                BearerTokens(accessToken = access, refreshToken = refresh)
            } else {
                inMemoryTokens
            }
        } catch (e: Exception) {
            inMemoryTokens
        }
    }

    override fun setToken(token: BearerTokens) {
        try {
            preferences.put("access_token", token.accessToken)
            if (token.refreshToken != null) {
                preferences.put("refresh_token", token.refreshToken)
            } else {
                preferences.remove("refresh_token")
            }
            preferences.flush()
        } catch (e: Exception) {
            // Safe fallback to memory
        }
        inMemoryTokens = token
    }

    override fun clearToken() {
        try {
            preferences.remove("access_token")
            preferences.remove("refresh_token")
            preferences.flush()
        } catch (e: Exception) {
            // Ignore failure on clear
        }
        inMemoryTokens = null
    }
}

actual fun createPlatformTokenProvider(): TokenProvider = PreferencesTokenProvider()
