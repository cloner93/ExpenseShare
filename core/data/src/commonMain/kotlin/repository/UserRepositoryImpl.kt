package repository

import model.User

interface UserStorage {
    fun saveUser(user: User)
    fun getUser(): User?
    fun clearUser()
}

expect fun createPlatformUserStorage(): UserStorage

class InMemoryUserStorage : UserStorage {
    private var cachedUser: User? = null
    override fun saveUser(user: User) { cachedUser = user }
    override fun getUser(): User? = cachedUser
    override fun clearUser() { cachedUser = null }
}

class UserRepositoryImpl(
    private val storage: UserStorage = createPlatformUserStorage()
) : UserRepository {
    private var userInfo: User? = null

    override suspend fun setUserInfo(user: User) {
        userInfo = user
        storage.saveUser(user)
    }

    override suspend fun getInfo(): User {
        userInfo?.let { return it }
        val persisted = storage.getUser()
        if (persisted != null) {
            userInfo = persisted
            return persisted
        }
        // Safe fallback to prevent UninitializedPropertyAccessException on cold start
        return userInfo ?: User(id = 0, username = "", phone = "")
    }

    fun clear() {
        userInfo = null
        storage.clearUser()
    }
}