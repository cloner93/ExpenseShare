package token

import android.content.Context
import android.content.SharedPreferences
import io.ktor.client.plugins.auth.providers.BearerTokens

object ContextHolder {
    var context: Context? = null
}

class SharedPreferencesTokenProvider(
    private val contextProvider: () -> Context? = { ContextHolder.context }
) : TokenProvider {

    private val prefs: SharedPreferences?
        get() = contextProvider()?.getSharedPreferences("expense_share_tokens", Context.MODE_PRIVATE)

    private var inMemoryTokens: BearerTokens? = null

    override fun getToken(): BearerTokens? {
        val sp = prefs ?: return inMemoryTokens
        val access = sp.getString("access_token", null) ?: return inMemoryTokens
        val refresh = sp.getString("refresh_token", null)
        return BearerTokens(accessToken = access, refreshToken = refresh)
    }

    override fun setToken(token: BearerTokens) {
        val sp = prefs
        if (sp != null) {
            sp.edit()
                .putString("access_token", token.accessToken)
                .putString("refresh_token", token.refreshToken)
                .apply()
        }
        inMemoryTokens = token
    }

    override fun clearToken() {
        prefs?.edit()?.clear()?.apply()
        inMemoryTokens = null
    }
}

actual fun createPlatformTokenProvider(): TokenProvider = SharedPreferencesTokenProvider()
