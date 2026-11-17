import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.milad.expense_share.data.models.FriendInfo
import org.milad.expense_share.data.models.FriendRelationStatus
import org.milad.expense_share.data.models.User
import org.milad.expense_share.domain.repository.FriendRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.FriendsService

class FriendsServiceTest : DescribeSpec({

    val friendRepository = mockk<FriendRepository>()
    val userRepository = mockk<UserRepository>()

    val friendsService = FriendsService(
        friendRepository = friendRepository,
        userRepository = userRepository
    )

    val currentUser = User(id = 1, username = "Sender", phone = "09121111111")
    val friendUser = User(id = 2, username = "Receiver", phone = "09122222222")

    beforeEach {
        clearAllMocks()
    }

    describe("sendFriendRequest") {
        it("successfully sends a friend request to a valid registered user") {
            every { userRepository.findById(1) } returns currentUser
            every { userRepository.findByPhone("09122222222") } returns friendUser
            every { friendRepository.getFriendshipStatus(1, "09122222222") } returns null
            every { friendRepository.sendFriendRequest(1, "09122222222") } returns true

            val result = friendsService.sendFriendRequest(1, "09122222222")

            result.shouldBeSuccess("Friend request sent successfully")
            verify(exactly = 1) { friendRepository.sendFriendRequest(1, "09122222222") }
        }

        it("normalizes Persian digits when sending friend request") {
            every { userRepository.findById(1) } returns currentUser
            every { userRepository.findByPhone("09122222222") } returns friendUser
            every { friendRepository.getFriendshipStatus(1, "09122222222") } returns null
            every { friendRepository.sendFriendRequest(1, "09122222222") } returns true

            val result = friendsService.sendFriendRequest(1, "۰۹۱۲۲۲۲۲۲۲۲")

            result.shouldBeSuccess("Friend request sent successfully")
            verify(exactly = 1) { friendRepository.sendFriendRequest(1, "09122222222") }
        }

        it("fails when user tries to send friend request to themselves") {
            every { userRepository.findById(1) } returns currentUser

            val result = friendsService.sendFriendRequest(1, "09121111111")

            result.shouldBeFailure { error ->
                error.message shouldBe "Cannot send friend request to yourself: You are always your own friend."
            }
            verify(exactly = 0) { friendRepository.sendFriendRequest(any(), any()) }
        }

        it("fails with user not found when target phone does not exist") {
            every { userRepository.findById(1) } returns currentUser
            every { userRepository.findByPhone("09129999999") } returns null

            val result = friendsService.sendFriendRequest(1, "09129999999")

            result.shouldBeFailure { error ->
                error.message shouldBe "User not found with phone 09129999999"
            }
            verify(exactly = 0) { friendRepository.sendFriendRequest(any(), any()) }
        }

        it("fails when friendship already exists") {
            every { userRepository.findById(1) } returns currentUser
            every { userRepository.findByPhone("09122222222") } returns friendUser
            every { friendRepository.getFriendshipStatus(1, "09122222222") } returns FriendInfo(
                user = friendUser,
                status = FriendRelationStatus.ACCEPTED,
                requestedBy = 1,
                createdAt = 100L,
                updatedAt = 100L
            )

            val result = friendsService.sendFriendRequest(1, "09122222222")

            result.shouldBeFailure { error ->
                error.message shouldBe "You are already friends with this user"
            }
            verify(exactly = 0) { friendRepository.sendFriendRequest(any(), any()) }
        }
    }

    describe("unblockFriend") {
        it("calls unblockFriend on repository when relationship is blocked") {
            every { friendRepository.getFriendshipStatus(1, "09122222222") } returns FriendInfo(
                user = friendUser,
                status = FriendRelationStatus.BLOCKED,
                requestedBy = 1,
                createdAt = 100L,
                updatedAt = 100L
            )
            every { friendRepository.unblockFriend(1, "09122222222") } returns true

            val result = friendsService.unblockFriend(1, "09122222222")

            result.shouldBeSuccess("User unblocked successfully")
            verify(exactly = 1) { friendRepository.unblockFriend(1, "09122222222") }
        }
    }
})
