package usecase.friends

import kotlinx.coroutines.flow.Flow
import model.FriendInfo
import repository.FriendsRepository

class GetIncomingRequestsUseCase(
    private val friendsRepository: FriendsRepository
) {
    suspend operator fun invoke(): Flow<Result<List<FriendInfo>>> {
        return friendsRepository.getIncomingRequests()
    }
}