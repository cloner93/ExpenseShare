package usecase.auth

import repository.AuthRepository

class RegisterUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phone: String, username: String, password: String) =
        authRepository.register(phone, username, password)
}