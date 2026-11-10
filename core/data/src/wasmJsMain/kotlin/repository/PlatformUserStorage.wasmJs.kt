package repository

import kotlinx.serialization.json.Json
import model.User

external interface WasmJsStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun clear()
}

external val localStorage: WasmJsStorage

class LocalStorageUserStorage : UserStorage {
    private val userKey = "expense_share_user"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun saveUser(user: User) {
        try {
            val serialized = json.encodeToString(User.serializer(), user)
            localStorage.setItem(userKey, serialized)
        } catch (_: Throwable) {
            // Ignored if storage is restricted
        }
    }

    override fun getUser(): User? {
        val serialized = try {
            localStorage.getItem(userKey)
        } catch (_: Throwable) {
            null
        } ?: return null

        return try {
            json.decodeFromString(User.serializer(), serialized)
        } catch (_: Throwable) {
            null
        }
    }

    override fun clearUser() {
        try {
            localStorage.removeItem(userKey)
        } catch (_: Throwable) {
            // Ignored
        }
    }
}

actual fun createPlatformUserStorage(): UserStorage = LocalStorageUserStorage()
