# core/domain — SKILL.md

## Purpose
Pure business logic layer. Contains:
- Domain models (shared between client and server via KMP)
- Repository interfaces (contracts)
- Use cases (single-responsibility business operations)

**No platform code. No UI. No network calls. No DB access.**

---

## Domain Models

### User
```kotlin
data class User(val id: Int, val username: String, val phone: String)
```

### Group
```kotlin
data class Group(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val members: List<User>,        // populated by server GroupService
    val transactions: List<Transaction>  // populated by server GroupService
)
```

### Transaction
```kotlin
data class Transaction(
    val id: Int,
    val groupId: Int,
    val title: String,
    val amount: Amount,         // Amount from core/currency (Long, not Double)
    val description: String,
    val createdBy: Int,
    var status: TransactionStatus,
    val createdAt: Long,
    val transactionDate: Long,
    var approvedBy: Int? = null,
    val payers: List<PayerDto>,
    val shareDetails: ShareDetailsRequest,
)
```

### TransactionStatus
```kotlin
enum class TransactionStatus { PENDING, APPROVED, REJECTED }
```

### PayerDto & Share models
```kotlin
data class PayerDto(val user: User, val amountPaid: Amount)

data class ShareDetailsRequest(
    val type: String,       // "Equal", "Percent", "Weight", "Manual"
    val members: List<MemberShareDto>
)

data class MemberShareDto(val user: User, val share: Amount = Amount(0))
```

### FriendInfo
```kotlin
data class FriendInfo(
    val user: User,
    val status: FriendRelationStatus,
    val requestedBy: Int,
    val createdAt: Long,
    val updatedAt: Long
)

enum class FriendRelationStatus { ACCEPTED, PENDING, REJECTED, BLOCKED }
```

---

## Repository Interfaces (Contracts)

### Pattern
All repository methods are `suspend` and return `Flow<Result<T>>`.

```kotlin
@Mockable
interface AuthRepository {
    suspend fun register(phone: String, username: String, password: String): Flow<Result<User>>
    suspend fun login(phone: String, password: String): Flow<Result<User>>
}

@Mockable
interface GroupsRepository {
    suspend fun getGroups(): Flow<Result<List<Group>>>
    suspend fun createGroup(name: String, memberIds: List<Int>): Flow<Result<Group>>
    suspend fun updateGroupMembers(groupId: String, memberIds: List<Int>): Flow<Result<String>>
    suspend fun deleteGroup(groupId: String): Flow<Result<String>>
}

@Mockable
interface TransactionsRepository {
    suspend fun getTransactions(groupId: String): Flow<Result<List<Transaction>>>
    suspend fun createTransaction(groupId: Int, title: String, amount: Amount,
        description: String?, payers: List<PayerDto>?, shareDetails: ShareDetailsRequest?
    ): Flow<Result<Transaction>>
    suspend fun approveTransaction(groupId: String, transactionId: String): Flow<Result<String>>
    suspend fun rejectTransaction(groupId: String, transactionId: String): Flow<Result<String>>
    suspend fun deleteTransaction(groupId: String, transactionId: String): Flow<Result<String>>
}

@Mockable
interface FriendsRepository {
    suspend fun getAllFriends(): Flow<Result<List<FriendInfo>>>
    suspend fun sendFriendRequest(targetPhone: String): Flow<Result<String>>
    suspend fun acceptFriendRequest(targetPhone: String): Flow<Result<String>>
    suspend fun rejectFriendRequest(targetPhone: String): Flow<Result<String>>
    suspend fun removeFriend(targetPhone: String): Flow<Result<String>>
    suspend fun cancelFriendRequest(targetPhone: String): Flow<Result<String>>
    // ... and more
}

@Mockable
interface UserRepository {
    suspend fun setUserInfo(user: User)
    suspend fun getInfo(): User
}
```

---

## Use Cases

### Pattern
Each use case is a class with a single `invoke` operator. All are `suspend`.

```kotlin
class GetGroupsUseCase(private val groupRepository: GroupsRepository) {
    suspend operator fun invoke() = groupRepository.getGroups()
}
```

### Naming Convention
`<Action><Entity>UseCase` — e.g. `CreateGroupUseCase`, `ApproveTransactionUseCase`

### Complete List by Domain

**Auth**
- `RegisterUserUseCase(phone, username, password)`
- `LoginUserUseCase(phone, password)`

**User**
- `GetUserInfoUseCase()` — returns cached `User`
- `SetUserInfoUseCase(user)` — stores after login/register

**Groups**
- `GetGroupsUseCase()`
- `CreateGroupUseCase(name, memberIds: List<Int>)`
- `DeleteGroupUseCase(groupId: String)`
- `UpdateGroupMembersUseCase(groupId: String, memberIds: List<Int>)`

**Transactions**
- `GetTransactionsUseCase(groupId: String)`
- `CreateTransactionUseCase(groupId, title, amount, description, payers, shareDetails)`
- `ApproveTransactionUseCase(groupId, transactionId)`
- `RejectTransactionUseCase(groupId, transactionId)`
- `DeleteTransactionUseCase(groupId, transactionId)`

**Friends**
- `GetAllFriendsUseCase()`
- `GetAcceptedFriendsUseCase()`
- `GetIncomingRequestsUseCase()`
- `GetOutgoingRequestsUseCase()`
- `GetBlockedFriendsUseCase()`
- `GetFriendshipStatusUseCase(targetPhone)`
- `SendFriendRequestUseCase(targetPhone)`
- `AcceptFriendRequestUseCase(targetPhone)`
- `RejectFriendRequestUseCase(targetPhone)`
- `BlockFriendUseCase(targetPhone)`
- `UnblockFriendUseCase(targetPhone)`
- `RemoveFriendUseCase(targetPhone)`
- `CancelFriendRequestUseCase(targetPhone)`

---

## Testing Pattern (Kotest + Mockative)

```kotlin
class GetGroupsUseCaseTest : StringSpec({
    val repo = mock(of<GroupsRepository>())
    val usecase = GetGroupsUseCase(repo)

    "should return groups when repository succeeds" {
        coEvery { repo.getGroups() }
            .returns(flowOf(Result.success(groups)))

        val result = usecase().first()

        result.isSuccess shouldBe true
        coVerify { repo.getGroups() }.wasInvoked(exactly = 1)
    }
})
```

**Note:** `io.mockative.enabled=false` in `gradle.properties` — mockative may be disabled.

---

## Rules
- Use cases have ONE responsibility — no combining multiple repo calls
- Repository return types are always `Flow<Result<T>>` — no exceptions, no nullable
- Domain models are `@Serializable` — they're used in API responses
- `@Mockable` annotation is required on interfaces for mockative to generate mocks
- GroupId can be `String` or `Int` depending on context — check method signatures carefully

---

## Module Dependencies
```
core/domain → kotlinx-coroutines-core, kotlinx-serialization-json, core/currency, mockative
```
