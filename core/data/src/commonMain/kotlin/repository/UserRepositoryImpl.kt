package repository

import model.User

class UserRepositoryImpl : UserRepository {
    private lateinit var userInfo: User

    override suspend fun setUserInfo(user: User) {
        userInfo = user
    }

    override suspend fun getInfo(): User {
        return userInfo
    }
}