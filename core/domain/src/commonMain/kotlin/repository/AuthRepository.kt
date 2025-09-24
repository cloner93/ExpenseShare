package repository

import io.mockative.Mockable
import kotlinx.coroutines.flow.Flow
import model.User

@Mockable
interface AuthRepository {
    suspend fun register(phone: String, username: String, password: String): Flow<Result<User>>
    suspend fun login(phone: String, password: String): Flow<Result<User>>
}