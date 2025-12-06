package usecase.user

import model.User
import repository.UserRepository

class SetUserInfoUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        user: User,
    ) = userRepository.setUserInfo(user)
}