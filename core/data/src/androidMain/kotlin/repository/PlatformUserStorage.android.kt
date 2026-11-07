package repository

import android.content.Context
import model.User
import token.ContextHolder

class SharedPreferencesUserStorage(
    private val contextProvider: () -> Context? = { ContextHolder.context }
) : UserStorage {
    private var inMemoryUser: User? = null

    private val prefs
        get() = contextProvider()?.getSharedPreferences("expense_share_user_session", Context.MODE_PRIVATE)

    override fun saveUser(user: User) {
        inMemoryUser = user
        val sp = prefs ?: return
        sp.edit()
            .putInt("user_id", user.id)
            .putString("username", user.username)
            .putString("phone", user.phone)
            .apply()
    }

    override fun getUser(): User? {
        val sp = prefs ?: return inMemoryUser
        if (!sp.contains("user_id")) return inMemoryUser
        val id = sp.getInt("user_id", 0)
        val username = sp.getString("username", "") ?: ""
        val phone = sp.getString("phone", "") ?: ""
        return User(id = id, username = username, phone = phone)
    }

    override fun clearUser() {
        inMemoryUser = null
        prefs?.edit()?.clear()?.apply()
    }
}

actual fun createPlatformUserStorage(): UserStorage = SharedPreferencesUserStorage()
