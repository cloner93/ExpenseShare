package usecase.auth

import repository.AuthRepository

class LoginUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phone: String, password: String) =
        authRepository.login(phone, password)
}