package usecase.user

import repository.UserRepository

class GetUserInfoUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.getInfo()
}