package repository

import kotlinx.coroutines.flow.Flow
import model.User
import utils.Result

interface AuthRepository {
    suspend fun register(phone: String, username: String, password: String): Flow<Flow<Result<User>>>
    suspend fun login(phone: String, password: String): Flow<Flow<Result<User>>>
}