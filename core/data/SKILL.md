# core/data — SKILL.md

## Purpose
Data layer for the Compose Multiplatform client. Implements repository interfaces defined in `core/domain`.
All implementations use `NetworkManager` from `core/network` to make HTTP requests.

---

## Repository Implementations

### Pattern
```kotlin
class GroupsRepositoryImpl(private val networkManager: NetworkManager) : GroupsRepository {
    override suspend fun getGroups(): Flow<Result<List<Group>>> {
        return networkManager.get<List<Group>>("/groups")
    }
}
```

Each impl:
1. Takes `NetworkManager` via constructor injection (Koin)
2. Calls `networkManager.get/post/put/delete` with the endpoint
3. Returns `Flow<Result<T>>` — no try/catch needed (handled by `safeNetworkCall`)

---

## API Endpoint Map

### Auth
| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| POST | `/auth/register` | `RegisterRequest` | `AuthResponse` |
| POST | `/auth/login` | `LoginRequest` | `AuthResponse` |

### Groups
| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| GET | `/groups` | — | `List<Group>` |
| POST | `/groups/create` | `CreateGroupRequest` | `Group` |
| POST | `/groups/{id}/updateMembers` | `List<Int>` | `String` |
| DELETE | `/groups/{id}` | — | `String` |

### Transactions
| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| GET | `/groups/{groupId}/transactions` | — | `List<Transaction>` |
| POST | `/groups/{groupId}/transactions` | `CreateTransactionRequest` | `Transaction` |
| POST | `/groups/{groupId}/transactions/{id}/approve` | `Unit` | `String` |
| POST | `/groups/{groupId}/transactions/{id}/reject` | `Unit` | `String` |
| DELETE | `/groups/{groupId}/transactions/{id}` | — | `String` |

### Friends
| Method | Endpoint | Body | Returns |
|--------|----------|------|---------|
| GET | `/friends` | — | `List<FriendInfo>` |
| GET | `/friends/accepted` | — | `List<FriendInfo>` |
| GET | `/friends/requests/incoming` | — | `List<FriendInfo>` |
| GET | `/friends/requests/outgoing` | — | `List<FriendInfo>` |
| GET | `/friends/status/{phone}` | — | `FriendInfo` |
| POST | `/friends/request` | `FriendRequest(phone)` | `String` |
| PUT | `/friends/accept` | `FriendRequest(phone)` | `String` |
| PUT | `/friends/reject` | `FriendRequest(phone)` | `String` |
| PUT | `/friends/block` | `FriendRequest(phone)` | `String` |
| PUT | `/friends/unblock` | `FriendRequest(phone)` | `String` |
| PUT | `/friends/cancel` | `FriendRequest(phone)` | `String` |
| DELETE | `/friends/remove` | `FriendRequest(phone)` | `String` (uses PUT body actually) |

---

## Request Models (client-side only)

```kotlin
// core/data/src/commonMain/kotlin/model/
data class LoginRequest(val phone: String, val password: String)
data class RegisterRequest(val phone: String, val username: String, val password: String)
data class CreateGroupRequest(val name: String, val memberIds: List<Int>)
data class FriendRequest(val phone: String)

data class CreateTransactionRequest(
    val title: String,
    val amount: Amount,
    val description: String?,
    val payers: List<PayerDto>?,
    val shareDetails: ShareDetailsRequest?,
)
```

---

## AuthResponse & Token Flow

```kotlin
// Server returns:
data class AuthResponse(val token: String, val user: User)

// AuthRepositoryImpl handles token storage:
override suspend fun register(...): Flow<Result<User>> {
    return networkManager.post<RegisterRequest, AuthResponse>("auth/register", body)
        .map { result ->
            result.map { (token, user) ->
                tokenProvider.setToken(token)   // ← stores JWT
                userRepository.setUserInfo(user)  // ← caches user locally
                user
            }.onFailure {
                tokenProvider.clearToken()
            }
        }
}
```

---

## UserRepository (Local Cache)

`UserRepositoryImpl` stores user info **in-memory only**:
```kotlin
class UserRepositoryImpl : UserRepository {
    private lateinit var userInfo: User
    override suspend fun setUserInfo(user: User) { userInfo = user }
    override suspend fun getInfo(): User { return userInfo }
}
```

⚠️ If `getInfo()` is called before `setUserInfo()`, it will throw `UninitializedPropertyAccessException`.
This is intentional — user must be logged in before accessing protected screens.

---

## DI (Koin)

```kotlin
val dataModule = module {
    single { UserRepositoryImpl() as UserRepository }
    single { AuthRepositoryImpl(get(), get(), get()) as AuthRepository }
    single { FriendsRepositoryImpl(get()) as FriendsRepository }
    single { GroupsRepositoryImpl(get()) as GroupsRepository }
    single { TransactionsRepositoryImpl(get()) as TransactionsRepository }
}

val dataAggregator = module {
    includes(networkModule)
    includes(dataModule)
}
```

Use `dataAggregator` in app — it includes the network module automatically.

---

## Testing Pattern

```kotlin
class GroupsRepositoryTest : DescribeSpec({
    val network = mock(of<NetworkManager>())
    val repo = GroupsRepositoryImpl(network)

    describe("getGroups") {
        it("should return success") {
            coEvery { network.get<List<Group>>("/groups") }
                .returns(flowOf(Result.success(expectedGroups)))

            val result = repo.getGroups().first()

            result shouldBeSuccess { groups ->
                groups.size shouldBe 1
            }
        }
    }
})
```

---

## Rules
- Do NOT add business logic to repository implementations — delegate to use cases
- All endpoints must match exactly what the server defines (see server `SKILL.md`)
- `FriendRequest(phone)` wraps the phone in an object — the API doesn't accept raw strings
- `CreateGroupRequest.memberIds` is `List<Int>` — user IDs, not phones
- The `removeFriend` endpoint uses `PUT /friends/remove` with body (not `DELETE`) — this is a known API design choice
- Never store auth tokens in SharedPreferences or files — in-memory only per current design

---

## Module Dependencies
```
core/data → core/domain, core/network, core/currency, kotlinx-coroutines-core, kotlinx-serialization-json
```
