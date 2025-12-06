package repository

import io.mockative.Mockable
import model.User

@Mockable
interface UserRepository {
    suspend fun setUserInfo(user: User)
    suspend fun getInfo(): User
}