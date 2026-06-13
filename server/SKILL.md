# server — SKILL.md

## Purpose
Ktor backend server for ExpenseShare. Handles all persistence, business logic, and REST API.
Runs on JVM only. PostgreSQL database via JetBrains Exposed ORM + HikariCP.

---

## Architecture Layers

```
presentation/    ← Ktor routes (HTTP in/out only)
domain/
  ├── service/   ← Business logic
  └── repository/← Interfaces (contracts)
data/
  ├── repository/← Exposed ORM implementations
  ├── db/table/  ← Exposed Table objects (schema)
  └── models/    ← Server-side data models
security/        ← JWT generation and validation
```

---

## Startup Sequence (Application.kt)

```kotlin
fun Application.main() {
    install(ContentNegotiation) { json() }
    install(CallLogging) { level = Level.INFO }
    DatabaseFactory.init()    // connects to PostgreSQL, runs SchemaUtils.create()
    configureCORS()
    configureStatusPages()
    configureSecurity()       // JWT auth setup
    configureKoin()           // DI
    configureRouting()        // registers all routes
}
```

---

## Database (PostgreSQL via Exposed)

### Connection Config
```kotlin
jdbcUrl = "jdbc:postgresql://localhost:5432/expenseshare"
username = "postgres"
password = "miladmilad"
maximumPoolSize = 10
isAutoCommit = false
transactionIsolation = "TRANSACTION_REPEATABLE_READ"
```
⚠️ Credentials are hardcoded — change for production.

### Schema (auto-created on startup)
All tables defined in `data/db/table/`. `SchemaUtils.create()` creates them if they don't exist.

### Amount Column Type
```kotlin
// Custom column type for monetary values:
fun Table.amount(name: String): Column<Amount>
// → BIGINT in PostgreSQL
// Usage: val amount = amount("amount")
```
**Never use `double()` or `decimal()` for money.**

### All Transactions in `transaction { }` block
```kotlin
override fun createGroup(...): UserGroupResponse = transaction {
    // all DB operations here
}
```

---

## JWT Authentication

### Config (application.conf)
```hocon
jwt {
    secret = "LKJHkjh$KHdGDU@EGjBDj%BDjhgreG"
    issuer = "http://0.0.0.0:8082/"
    realm = "Access to 'test'"
    validityMs = 86400000   // 24 hours
}
```

### Token Claims
```kotlin
JWT.create()
    .withClaim("id", user.id)
    .withClaim("username", user.username)
    .withClaim("phone", user.phone)
```

### Extracting User from Token
```kotlin
// In routes:
val userId = call.principal<JWTPrincipal>().getUserId()
    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(...))

// Extension function:
fun JWTPrincipal?.getUserId(): Int? = this?.payload?.getClaim("id")?.asInt()
```

### Protected Routes
```kotlin
authenticate("auth-jwt") {
    route("/groups") {
        // all routes here require valid JWT
    }
}
```
Public routes (`/auth/register`, `/auth/login`) are outside `authenticate` block.

---

## API Response Format

### Success
```kotlin
@Serializable
data class SuccessResponse<T>(val success: Boolean = true, val data: T)

// Usage:
call.respond(HttpStatusCode.Created, SuccessResponse(data = it))
```

### Error
```kotlin
@Serializable
data class ErrorResponse(
    val message: String,
    val code: String,
    val details: Map<String, String>? = null
)

// Usage:
call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid group ID", "INVALID_GROUP_ID"))
```

---

## Transaction Approval Flow

```
User creates transaction
    ↓
if (creator == group.ownerId) → status = APPROVED, approvedBy = userId
else                          → status = PENDING

Group owner can:
    PENDING → APPROVED (approve)
    PENDING → REJECTED (reject)

Any user can:
    DELETE their own transaction
Group owner can:
    DELETE any transaction
```

### Visibility Rules for getTransactions
```kotlin
if (ownerId == userId) {
    // owner sees ALL transactions (PENDING, APPROVED, REJECTED)
} else {
    // member sees: APPROVED + their own PENDING
}
```

---

## Route Patterns

### Standard Route Handler
```kotlin
post("/create") {
    val userId = call.principal<JWTPrincipal>().getUserId()
        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse(...))

    val groupId = call.getIntParameter("groupId")
        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(...))

    val request = call.receive<CreateGroupRequest>()

    groupService.createGroup(userId, request.name, request.memberIds)
        .onSuccess { call.respond(HttpStatusCode.Created, SuccessResponse(data = it)) }
        .onFailure { call.respond(HttpStatusCode.BadRequest, ErrorResponse(...)) }
}
```

### Service Return Type
All service methods return `Result<T>`:
```kotlin
fun createGroup(ownerId: Int, name: String, members: List<Int>): Result<UserGroupResponse>
```

---

## Friend Relation Logic

Friendship is stored in ONE row with `(userId, friendId)` — queries must check BOTH directions:
```kotlin
// Check if relationship exists (either direction):
(FriendRelations.userId eq fromId AND FriendRelations.friendId eq toId) OR
(FriendRelations.userId eq toId AND FriendRelations.friendId eq fromId)
```

`requestedBy` field tracks who initiated — used to prevent accepting your own request.

### Status Transitions
```
(nothing) → sendFriendRequest → PENDING
PENDING   → accept            → ACCEPTED
PENDING   → reject            → REJECTED
PENDING   → cancel (requester)→ deleted
ACCEPTED  → block             → BLOCKED
BLOCKED   → unblock           → ACCEPTED
ACCEPTED  → remove            → deleted
```

---

## DI (Koin — server)

```kotlin
val appModule = module {
    single { FriendRepositoryImpl() as FriendRepository }
    single { UserRepositoryImpl() as UserRepository }
    single { GroupRepositoryImpl() as GroupRepository }
    single { TransactionRepositoryImpl() as TransactionRepository }

    single { AuthService(get()) }
    single { FriendsService(get(), get()) }
    single { GroupService(get(), get(), get()) }
    single { TransactionService(get()) }
}
```

---

## Running the Server
```bash
./gradlew :server:run
# Starts on port 8082 (configured in application.conf)
```

---

## Common Mistakes to Avoid

| Wrong | Correct |
|-------|---------|
| Using `FakeDatabase` | It's unused — use Exposed ORM |
| `double()` column for money | `amount()` custom column (BIGINT) |
| Route logic with business rules | Move to Service layer |
| DB queries outside `transaction {}` | Always wrap in `transaction {}` |
| Checking only one direction in friend queries | Check both `(userId, friendId)` and `(friendId, userId)` |

---

## Module Dependencies (Server)
```
server → ktor-server-*, exposed-*, postgresql, hikaricp, jbcrypt, koin-ktor, core/currency
```
