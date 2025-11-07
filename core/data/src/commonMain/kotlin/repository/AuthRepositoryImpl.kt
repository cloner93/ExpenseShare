package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import model.LoginRequest
import model.RegisterRequest
import model.User
import token.TokenProvider

class AuthRepositoryImpl(
    private val networkManager: NetworkManager,
    private val tokenProvider: TokenProvider
) : AuthRepository {
    override suspend fun register(
        phone: String,
        username: String,
        password: String
    ): Flow<Result<User>> {
        return networkManager.post<RegisterRequest, User>(
            endpoint = "auth/register",
            body = RegisterRequest(phone, username, password)
        )
    }

    override suspend fun login(
        phone: String,
        password: String
    ): Flow<Result<User>> {
        val response = networkManager.post<LoginRequest, User>(
            endpoint = "auth/login",
            body = LoginRequest(phone, password)
        )

// TODO:          tokenProvider.setToken(response.onSudata.token)
        return response
    }
}