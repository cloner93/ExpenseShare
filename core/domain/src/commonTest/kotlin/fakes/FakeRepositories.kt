package fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import model.FriendInfo
import model.Group
import model.PayerDto
import model.Settlement
import model.ShareDetailsRequest
import model.Transaction
import model.User
import org.milad.expense_share.Amount
import repository.AuthRepository
import repository.FriendsRepository
import repository.GroupsRepository
import repository.SettlementRepository
import repository.TransactionsRepository
import repository.UserRepository

/**
 * Lightweight, zero-dependency test fakes replacing disabled Mockative mock generation
 * in core:domain module tests.
 */

class FakeTransactionsRepository : TransactionsRepository {
    var getTransactionsResult: Result<List<Transaction>> = Result.success(emptyList())
    var createTransactionResult: Result<Transaction>? = null
    var approveTransactionResult: Result<String> = Result.success("Success")
    var rejectTransactionResult: Result<String> = Result.success("Success")
    var deleteTransactionResult: Result<String> = Result.success("Success")

    var getTransactionsCallCount: Int = 0
    var lastGetTransactionsGroupId: String? = null

    var createTransactionCallCount: Int = 0
    var lastCreateTransactionArgs: CreateTransactionArgs? = null

    var approveTransactionCallCount: Int = 0
    var lastApproveTransactionArgs: Pair<String, String>? = null

    var rejectTransactionCallCount: Int = 0
    var lastRejectTransactionArgs: Pair<String, String>? = null

    var deleteTransactionCallCount: Int = 0
    var lastDeleteTransactionArgs: Pair<String, String>? = null

    data class CreateTransactionArgs(
        val groupId: Int,
        val title: String,
        val amount: Amount,
        val description: String?,
        val payers: List<PayerDto>?,
        val shareDetails: ShareDetailsRequest?
    )

    fun reset() {
        getTransactionsCallCount = 0
        lastGetTransactionsGroupId = null
        createTransactionCallCount = 0
        lastCreateTransactionArgs = null
        approveTransactionCallCount = 0
        lastApproveTransactionArgs = null
        rejectTransactionCallCount = 0
        lastRejectTransactionArgs = null
        deleteTransactionCallCount = 0
        lastDeleteTransactionArgs = null
    }

    override suspend fun getTransactions(groupId: String): Flow<Result<List<Transaction>>> {
        getTransactionsCallCount++
        lastGetTransactionsGroupId = groupId
        return flowOf(getTransactionsResult)
    }

    override suspend fun createTransaction(
        groupId: Int,
        title: String,
        amount: Amount,
        description: String?,
        payers: List<PayerDto>?,
        shareDetails: ShareDetailsRequest?
    ): Flow<Result<Transaction>> {
        createTransactionCallCount++
        lastCreateTransactionArgs = CreateTransactionArgs(
            groupId = groupId,
            title = title,
            amount = amount,
            description = description,
            payers = payers,
            shareDetails = shareDetails
        )
        return flowOf(createTransactionResult ?: Result.failure(IllegalStateException("createTransactionResult not configured in test")))
    }

    override suspend fun approveTransaction(groupId: String, transactionId: String): Flow<Result<String>> {
        approveTransactionCallCount++
        lastApproveTransactionArgs = Pair(groupId, transactionId)
        return flowOf(approveTransactionResult)
    }

    override suspend fun rejectTransaction(groupId: String, transactionId: String): Flow<Result<String>> {
        rejectTransactionCallCount++
        lastRejectTransactionArgs = Pair(groupId, transactionId)
        return flowOf(rejectTransactionResult)
    }

    override suspend fun deleteTransaction(groupId: String, transactionId: String): Flow<Result<String>> {
        deleteTransactionCallCount++
        lastDeleteTransactionArgs = Pair(groupId, transactionId)
        return flowOf(deleteTransactionResult)
    }
}

class FakeGroupsRepository : GroupsRepository {
    var getGroupsResult: Result<List<Group>> = Result.success(emptyList())
    var createGroupResult: Result<Group>? = null
    var updateGroupMembersResult: Result<String> = Result.success("Success")
    var deleteGroupResult: Result<String> = Result.success("Success")

    var getGroupsCallCount: Int = 0
    var createGroupCallCount: Int = 0
    var lastCreateGroupName: String? = null
    var lastCreateGroupMemberIds: List<Int>? = null

    var updateGroupMembersCallCount: Int = 0
    var lastUpdateGroupId: String? = null
    var lastUpdateGroupMemberIds: List<Int>? = null

    var deleteGroupCallCount: Int = 0
    var lastDeleteGroupId: String? = null

    fun reset() {
        getGroupsCallCount = 0
        createGroupCallCount = 0
        lastCreateGroupName = null
        lastCreateGroupMemberIds = null
        updateGroupMembersCallCount = 0
        lastUpdateGroupId = null
        lastUpdateGroupMemberIds = null
        deleteGroupCallCount = 0
        lastDeleteGroupId = null
    }

    override suspend fun getGroups(): Flow<Result<List<Group>>> {
        getGroupsCallCount++
        return flowOf(getGroupsResult)
    }

    override suspend fun createGroup(name: String, memberIds: List<Int>): Flow<Result<Group>> {
        createGroupCallCount++
        lastCreateGroupName = name
        lastCreateGroupMemberIds = memberIds
        return flowOf(createGroupResult ?: Result.failure(IllegalStateException("createGroupResult not configured in test")))
    }

    override suspend fun updateGroupMembers(groupId: String, memberIds: List<Int>): Flow<Result<String>> {
        updateGroupMembersCallCount++
        lastUpdateGroupId = groupId
        lastUpdateGroupMemberIds = memberIds
        return flowOf(updateGroupMembersResult)
    }

    override suspend fun deleteGroup(groupId: String): Flow<Result<String>> {
        deleteGroupCallCount++
        lastDeleteGroupId = groupId
        return flowOf(deleteGroupResult)
    }
}

class FakeAuthRepository : AuthRepository {
    var loginResult: Result<User> = Result.success(User(1, "milad", "09123456789"))
    var registerResult: Result<User> = Result.success(User(1, "milad", "09123456789"))

    var loginCallCount: Int = 0
    var lastLoginPhone: String? = null
    var lastLoginPassword: String? = null

    var registerCallCount: Int = 0
    var lastRegisterPhone: String? = null
    var lastRegisterUsername: String? = null
    var lastRegisterPassword: String? = null

    fun reset() {
        loginCallCount = 0
        lastLoginPhone = null
        lastLoginPassword = null
        registerCallCount = 0
        lastRegisterPhone = null
        lastRegisterUsername = null
        lastRegisterPassword = null
    }

    override suspend fun login(phone: String, password: String): Flow<Result<User>> {
        loginCallCount++
        lastLoginPhone = phone
        lastLoginPassword = password
        return flowOf(loginResult)
    }

    override suspend fun register(phone: String, username: String, password: String): Flow<Result<User>> {
        registerCallCount++
        lastRegisterPhone = phone
        lastRegisterUsername = username
        lastRegisterPassword = password
        return flowOf(registerResult)
    }
}

class FakeFriendsRepository : FriendsRepository {
    var getAllFriendsResult: Result<List<FriendInfo>> = Result.success(emptyList())
    var getAcceptedFriendsResult: Result<List<FriendInfo>> = Result.success(emptyList())
    var getIncomingRequestsResult: Result<List<FriendInfo>> = Result.success(emptyList())
    var getOutgoingRequestsResult: Result<List<FriendInfo>> = Result.success(emptyList())
    var getBlockedFriendsResult: Result<List<FriendInfo>> = Result.success(emptyList())
    var getFriendshipStatusResult: Result<FriendInfo>? = null

    var sendFriendRequestResult: Result<String> = Result.success("Success")
    var acceptFriendRequestResult: Result<String> = Result.success("Success")
    var rejectFriendRequestResult: Result<String> = Result.success("Success")
    var blockFriendResult: Result<String> = Result.success("Success")
    var unblockFriendResult: Result<String> = Result.success("Success")
    var removeFriendResult: Result<String> = Result.success("Success")
    var cancelFriendRequestResult: Result<String> = Result.success("Success")

    var getAllFriendsCallCount: Int = 0
    var getAcceptedFriendsCallCount: Int = 0
    var getIncomingRequestsCallCount: Int = 0
    var getOutgoingRequestsCallCount: Int = 0
    var getBlockedFriendsCallCount: Int = 0
    var getFriendshipStatusCallCount: Int = 0

    var sendFriendRequestCallCount: Int = 0
    var acceptFriendRequestCallCount: Int = 0
    var rejectFriendRequestCallCount: Int = 0
    var blockFriendCallCount: Int = 0
    var unblockFriendCallCount: Int = 0
    var removeFriendCallCount: Int = 0
    var cancelFriendRequestCallCount: Int = 0

    var lastSendPhone: String? = null
    var lastAcceptPhone: String? = null
    var lastRejectPhone: String? = null
    var lastRemovePhone: String? = null

    fun reset() {
        getAllFriendsCallCount = 0
        getAcceptedFriendsCallCount = 0
        getIncomingRequestsCallCount = 0
        getOutgoingRequestsCallCount = 0
        getBlockedFriendsCallCount = 0
        getFriendshipStatusCallCount = 0
        sendFriendRequestCallCount = 0
        acceptFriendRequestCallCount = 0
        rejectFriendRequestCallCount = 0
        blockFriendCallCount = 0
        unblockFriendCallCount = 0
        removeFriendCallCount = 0
        cancelFriendRequestCallCount = 0
        lastSendPhone = null
        lastAcceptPhone = null
        lastRejectPhone = null
        lastRemovePhone = null
    }

    override suspend fun getAllFriends(): Flow<Result<List<FriendInfo>>> {
        getAllFriendsCallCount++
        return flowOf(getAllFriendsResult)
    }

    override suspend fun getAcceptedFriends(): Flow<Result<List<FriendInfo>>> {
        getAcceptedFriendsCallCount++
        return flowOf(getAcceptedFriendsResult)
    }

    override suspend fun getIncomingRequests(): Flow<Result<List<FriendInfo>>> {
        getIncomingRequestsCallCount++
        return flowOf(getIncomingRequestsResult)
    }

    override suspend fun getOutgoingRequests(): Flow<Result<List<FriendInfo>>> {
        getOutgoingRequestsCallCount++
        return flowOf(getOutgoingRequestsResult)
    }

    override suspend fun getBlockedFriends(): Flow<Result<List<FriendInfo>>> {
        getBlockedFriendsCallCount++
        return flowOf(getBlockedFriendsResult)
    }

    override suspend fun getFriendshipStatus(targetPhone: String): Flow<Result<FriendInfo>> {
        getFriendshipStatusCallCount++
        return flowOf(getFriendshipStatusResult ?: Result.failure(IllegalStateException("getFriendshipStatusResult not configured")))
    }

    override suspend fun sendFriendRequest(targetPhone: String): Flow<Result<String>> {
        sendFriendRequestCallCount++
        lastSendPhone = targetPhone
        return flowOf(sendFriendRequestResult)
    }

    override suspend fun acceptFriendRequest(targetPhone: String): Flow<Result<String>> {
        acceptFriendRequestCallCount++
        lastAcceptPhone = targetPhone
        return flowOf(acceptFriendRequestResult)
    }

    override suspend fun rejectFriendRequest(targetPhone: String): Flow<Result<String>> {
        rejectFriendRequestCallCount++
        lastRejectPhone = targetPhone
        return flowOf(rejectFriendRequestResult)
    }

    override suspend fun blockFriend(targetPhone: String): Flow<Result<String>> {
        blockFriendCallCount++
        return flowOf(blockFriendResult)
    }

    override suspend fun unblockFriend(targetPhone: String): Flow<Result<String>> {
        unblockFriendCallCount++
        return flowOf(unblockFriendResult)
    }

    override suspend fun removeFriend(targetPhone: String): Flow<Result<String>> {
        removeFriendCallCount++
        lastRemovePhone = targetPhone
        return flowOf(removeFriendResult)
    }

    override suspend fun cancelFriendRequest(targetPhone: String): Flow<Result<String>> {
        cancelFriendRequestCallCount++
        return flowOf(cancelFriendRequestResult)
    }
}

class FakeSettlementRepository : SettlementRepository {
    var getSettlementResult: Result<List<Settlement>> = Result.success(emptyList())
    var paySettlementResult: Result<String> = Result.success("Success")
    var confirmSettlementResult: Result<String> = Result.success("Success")
    var disputeSettlementResult: Result<String> = Result.success("Success")

    var getSettlementCallCount: Int = 0
    var paySettlementCallCount: Int = 0
    var confirmSettlementCallCount: Int = 0
    var disputeSettlementCallCount: Int = 0

    override suspend fun getSettlement(groupId: Int): Flow<Result<List<Settlement>>> {
        getSettlementCallCount++
        return flowOf(getSettlementResult)
    }

    override suspend fun paySettlement(groupId: Int, settlement: Int): Flow<Result<String>> {
        paySettlementCallCount++
        return flowOf(paySettlementResult)
    }

    override suspend fun confirmSettlement(groupId: Int, settlement: Int): Flow<Result<String>> {
        confirmSettlementCallCount++
        return flowOf(confirmSettlementResult)
    }

    override suspend fun disputeSettlement(groupId: Int, settlement: Int): Flow<Result<String>> {
        disputeSettlementCallCount++
        return flowOf(disputeSettlementResult)
    }
}

class FakeUserRepository : UserRepository {
    var currentUser: User = User(1, "milad", "09123456789")

    override suspend fun setUserInfo(user: User) {
        currentUser = user
    }

    override suspend fun getInfo(): User = currentUser
}
