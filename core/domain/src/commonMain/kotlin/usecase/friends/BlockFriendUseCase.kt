package usecase.friends

import kotlinx.coroutines.flow.Flow
import repository.FriendsRepository

class BlockFriendUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(targetPhone: String): Flow<Result<String>> {
        return friendsRepository.blockFriend(targetPhone)
    }
}