package usecase.friends

import kotlinx.coroutines.flow.Flow
import model.FriendInfo
import repository.FriendsRepository

class GetFriendshipStatusUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(targetPhone: String): Flow<Result<FriendInfo>> {
        return friendsRepository.getFriendshipStatus(targetPhone)
    }
}