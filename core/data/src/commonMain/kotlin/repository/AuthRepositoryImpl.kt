package repository

import NetworkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.AuthResponse
import model.LoginRequest
import model.RegisterRequest
import model.User
import token.TokenProvider

class AuthRepositoryImpl(
    private val networkManager: NetworkManager,
    private val tokenProvider: TokenProvider,
) : AuthRepository {
    override suspend fun register(
        phone: String,
        username: String,
        password: String,
    ): Flow<Result<User>> {
        return networkManager.post<RegisterRequest, AuthResponse>(
            endpoint = "auth/register",
            body = RegisterRequest(phone, username, password)
        ).map { result ->
            result.map { (token, user) ->
                tokenProvider.setToken(token)
                user
            }.onFailure {
                tokenProvider.clearToken()
            }
        }
    }

    override suspend fun login(
        phone: String,
        password: String,
    ): Flow<Result<User>> {
        return networkManager.post<LoginRequest, AuthResponse>(
            endpoint = "auth/login",
            body = LoginRequest(phone, password)
        ).map { result ->
            result.map { (token, user) ->
                tokenProvider.setToken(token)
                user
            }.onFailure {
                tokenProvider.clearToken()
            }
        }
    }
}