# ExpenseShare — Project SKILL.md

## What is this project?
A group expense sharing app (like Splitwise) built with Kotlin Multiplatform.
- **Client**: Compose Multiplatform (Android, iOS, Desktop JVM, Web/wasmJs)
- **Server**: Ktor + PostgreSQL

---

## Module Map

```
ExpenseShare/
├── core/
│   ├── currency/    → Amount type (money as Long) — see core/currency/SKILL.md
│   ├── logger/      → AppLogger cross-platform — see core/logger/SKILL.md
│   ├── navigation/  → Route definitions — see core/navigation/SKILL.md
│   ├── domain/      → Models, Repository interfaces, Use cases — see core/domain/SKILL.md
│   ├── data/        → Repository implementations (HTTP) — see core/data/SKILL.md
│   └── common/      → BaseViewModel (MVI), Theme, Scaffold — see core/common/SKILL.md
├── composeApp/      → All UI screens and ViewModels — see composeApp/SKILL.md
└── server/          → Ktor REST API + PostgreSQL — see server/SKILL.md
```

---

## Dependency Graph
```
composeApp
  └── core/common, core/domain, core/data, core/navigation, core/currency, core/logger

core/data
  └── core/domain, core/network, core/currency

core/domain
  └── core/currency

core/network
  └── core/logger

core/common
  └── core/navigation

server
  └── core/currency   (Amount type is shared!)
```

---

## Critical Cross-Cutting Rules

### 1. Money is always `Amount` (Long)
```kotlin
// NEVER use Double/Float for money
val amount: Amount = Amount(500L)    // 500 units
// See core/currency/SKILL.md
```

### 2. All async returns Flow<Result<T>>
```kotlin
// Repository contracts always:
suspend fun getGroups(): Flow<Result<List<Group>>>

// Collect in ViewModel:
useCase().collect { result ->
    result.onSuccess { ... }.onFailure { ... }
}
```

### 3. MVI — state changes only via setState
```kotlin
// NEVER mutate state directly
// WRONG: viewState.value.groups.add(group)
// RIGHT:
setState { it.copy(groups = it.groups + group) }
```

### 4. No local DB — server is source of truth
On app restart all data is re-fetched from server. Token is lost (in-memory only).

### 5. Server port: 8082
```
Client base URL: http://0.0.0.0:8082
Change in: core/network/src/commonMain/kotlin/client/NetworkClient.kt → HttpConfig.baseUrl
```

---

## Key Business Rules

### Transaction Approval
- Creator == group owner → auto-APPROVED
- Creator != group owner → PENDING (owner must approve)
- Owner sees all transactions; members see APPROVED + own PENDING

### Friend Requests
- Phone number is the unique identifier for users
- Friend relations are bidirectional (one DB row, queried both ways)
- Cannot send request to yourself, to blocked users, or to existing friends

### Group Membership
- Creator is always added to their own group
- Only group owner can update/delete group or approve transactions
- Members can add transactions (start as PENDING)

---

## Running the Project

```bash
# Server (required first)
./gradlew :server:run

# Android/iOS — via IDE

# Desktop
./gradlew jvmRun -DmainClass=org.milad.expense_share.MainKt --quiet

# Web
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# All tests
./gradlew kotest
```

---

## Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| UI | Compose Multiplatform 1.9.1 |
| Navigation | Jetbrains Navigation Compose |
| State Management | MVI (BaseViewModel + StateFlow/SharedFlow) |
| DI | Koin 4.x |
| HTTP Client | Ktor Client |
| HTTP Server | Ktor Server (Netty) |
| Database | PostgreSQL via Jetbrains Exposed ORM |
| Connection Pool | HikariCP |
| Auth | JWT (auth0) |
| Serialization | kotlinx.serialization |
| Testing | Kotest + Mockative |
| Analytics | Kotzilla SDK |
| Kotlin | 2.3.10 |
