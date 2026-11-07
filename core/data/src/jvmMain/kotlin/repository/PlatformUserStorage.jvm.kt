package repository

import model.User
import java.util.prefs.Preferences

class PreferencesUserStorage(
    private val preferences: Preferences = Preferences.userRoot().node("org.milad.expense_share.user_session")
) : UserStorage {
    private var inMemoryUser: User? = null

    override fun saveUser(user: User) {
        inMemoryUser = user
        try {
            preferences.putInt("user_id", user.id)
            preferences.put("username", user.username)
            preferences.put("phone", user.phone)
            preferences.flush()
        } catch (_: Exception) {
            // Safe fallback to in-memory
        }
    }

    override fun getUser(): User? {
        return try {
            val id = preferences.getInt("user_id", -1)
            if (id != -1) {
                val username = preferences.get("username", "") ?: ""
                val phone = preferences.get("phone", "") ?: ""
                User(id = id, username = username, phone = phone)
            } else {
                inMemoryUser
            }
        } catch (_: Exception) {
            inMemoryUser
        }
    }

    override fun clearUser() {
        inMemoryUser = null
        try {
            preferences.remove("user_id")
            preferences.remove("username")
            preferences.remove("phone")
            preferences.flush()
        } catch (_: Exception) {
            // Ignore
        }
    }
}

actual fun createPlatformUserStorage(): UserStorage = PreferencesUserStorage()
