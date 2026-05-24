# composeApp — SKILL.md

## Purpose
The Compose Multiplatform UI layer. Contains all screens, ViewModels, and feature-specific logic.
Targets: Android, iOS, JVM Desktop, wasmJs (Web).

---

## Feature Structure

```
composeApp/src/commonMain/kotlin/org/milad/expense_share/
├── auth/
│   ├── login/        LoginScreen, LoginViewModel
│   ├── register/     RegisterScreen, RegisterViewModel
│   └── AuthNavHost
├── dashboard/
│   ├── group/        GroupDetailScreen, GroupDetailViewModel
│   │   ├── components/
│   │   ├── dialogs/
│   │   ├── screen/   (ExpenseList, MemberList, SettlementScreen)
│   │   └── tabs/
│   ├── Dashboard, DashboardScreen, DashboardViewModel
│   └── ExtraPaneContent
├── friends/
│   ├── detail/       FriendDetailScreen, FriendDetailViewModel
│   │   └── tab/      (FriendOverviewTab, FriendTransactionsTab, FriendSettlementsTab)
│   ├── dialogs/
│   ├── friendsList/  FriendsList
│   └── FriendsScreen, FriendsViewModel
├── expenses/         AddExpense, AnimatedLoadingButton, ConfirmButton
├── group/            AddGroupScreen, FriendSelectionRow
├── chat/             ChatScreen (AI assistant)
├── profile/          ProfileScreen
└── di/               koinModules.kt
```

---

## MVI Pattern — Per Feature

Each feature follows:
```kotlin
// State — persistent UI state
data class DashboardState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true,
    ...
) : BaseViewState

// Action — user intents
sealed interface DashboardAction : BaseViewAction {
    data object LoadData : DashboardAction
    data class SelectGroup(val group: Group) : DashboardAction
    ...
}

// Event — one-time events (navigation, toast)
sealed interface DashboardEvent : BaseViewEvent {
    data class ShowToast(val message: String) : DashboardEvent
    data object ExtraPaneSuccessful : DashboardEvent
}
```

---

## Navigation Architecture

### Top-level: AppEntryPoint
```
RootRoute.Auth → AuthNavHost
RootRoute.Main → ResponsiveApp
```

### Main App: ResponsiveApp
Uses `AppScaffold` + `NavHost` for tab navigation.
Tab navigation preserves state (`saveState = true`, `restoreState = true`).

### List-Detail Pattern (Dashboard & Friends)
Uses `ListDetailPaneScaffold` from Material 3 Adaptive:
```kotlin
ListDetailPaneScaffold(
    listPane = { Dashboard(...) },
    detailPane = { GroupDetailScreen(...) },  // or FriendDetailScreen
    extraPane = { ExtraPaneContent(...) },    // AddExpense or AddGroup
)
```

Navigator: `rememberListDetailPaneScaffoldNavigator<Nothing>()`

---

## DashboardViewModel — Key Responsibilities

1. **Loading**: groups + friends + user info (parallel with `launch`)
2. **Balance calculation**: runs on `Dispatchers.Default`
3. **ExtraPane**: manages AddGroup/AddExpense flow
4. **Transaction updates**: receives events from GroupDetailViewModel via events

### Balance Calculation
```kotlin
fun List<Group>.calculateBalance(userId: Int): Balance {
    // For each group, for each APPROVED transaction:
    // paidByUser = sum of payers where payer.id == userId
    // shareOfUser = sum of shareDetails.members where member.id == userId
    // net = paidByUser - shareOfUser
    // net > 0 → owed (others owe you)
    // net < 0 → owe (you owe others)
}
```

---

## GroupDetailViewModel — Key Responsibilities

Scoped to a specific group (created with `koinViewModel(key = "group_${group.id}")`):
- Approve/reject/delete transactions (calls use cases, updates local state optimistically)
- Delete group
- Update members
- Load friends (when Members tab is selected)

### Optimistic State Update Pattern
```kotlin
// After approve succeeds:
val updatedTrx = state.transactions.find { it.id == id }?.copy(status = APPROVED)!!
setState { it.copy(transactions = it.transactions.map { if (it.id == id) updatedTrx else it }) }
postEvent(GroupDetailEvent.UpdateTransactionsOfCurrentGroup(transactions))
// DashboardViewModel receives event and syncs its copy
```

---

## AddExpense Flow

```
User fills:
  1. Name + Price + Description
  2. Payers (with amounts) — must sum to total price
  3. ShareType: Equal / Percent / Weight / Manual
  4. Members per type

Validation:
  - Name not blank
  - Price > 0
  - At least one payer
  - Payer amounts sum == total price
  - At least one member
  - Share type selected

On submit:
  DashboardViewModel.handle(AddExpense(name, amount, desc, payers, shareDetails))
  → CreateTransactionUseCase → server
  → on success: ExtraPaneSuccessful event → close pane
```

### ShareType → API mapping
```kotlin
// ShareType enum title must match server ShareType enum:
ShareType.Equal   → "Equal"
ShareType.Percent → "Percent"
ShareType.Weight  → "Weight"
ShareType.Manual  → "Manual"
```

---

## Friends Feature

### FriendsViewModel manages:
- All friends list (all statuses mixed)
- Dialog state: NewRequest, CancelRequest, AcceptRequest, RejectRequest
- currentUser for determining which buttons to show

### FriendRow logic
```kotlin
if (status == PENDING) {
    if (currentUserRequested) → show Cancel button only
    else → show Accept + Reject buttons
}
if (status == ACCEPTED) → show arrow (tappable)
```

### FriendDetailViewModel
- Scoped to `friend_${friendId}`
- Loads shared groups from `GetGroupsUseCase`
- Calculates balance from shared group transactions
- Extracts shared transactions for tabs

---

## Koin DI (composeApp)

```kotlin
// ViewModels requiring parameters use parametersOf:
koinViewModel(
    key = "group_${selectedGroup.id}",
    parameters = { parametersOf(selectedGroup, currentUser, isListAndDetail, isDetail) }
)
```

### appModules composition
```
appModules
├── domainModule (use cases)
├── dataAggregator (= dataModule + networkModule)
├── dashboardModule (DashboardViewModel, GroupDetailViewModel)
├── friendsModule (FriendsViewModel, FriendDetailViewModel)
├── registerModule
└── loginModule
```

---

## Platform Entry Points

| Platform | Entry Point |
|----------|-------------|
| Android | `MainActivity` → `AppEntryPoint()` |
| iOS | `MainViewController()` → `AppEntryPoint()` |
| Desktop JVM | `main()` → `Window { AppEntryPoint() }` |
| Web (wasmJs) | `main()` → `ComposeViewport { AppEntryPoint() }` |

Koin is started in each platform entry point before `AppEntryPoint()`.

---

## Kotzilla Analytics
The app uses Kotzilla SDK for Koin analytics:
```kotlin
startKoin {
    modules(appModules)
    analytics()   // ← Kotzilla
}
```
Present on Android and iOS. Do NOT remove `analytics()` call.

---

## Rules
- Every screen uses `BaseViewModel` — no state in Composables (except ephemeral UI state)
- `LaunchedEffect(Unit)` for collecting `viewEvent` — not `LaunchedEffect(state)`
- Use `koinViewModel()` for ViewModels — not manual instantiation
- `AppTheme.colors.*` for all colors — never `Color(0xFF...)` hardcoded
- `AnimatedLoadingButton` for all buttons that trigger async operations
- `FullScreenLoading` overlays when `isLoading = true`
- Feature dialogs are in `dialogs/` subfolder and use `BottomSheet`, not `AlertDialog`
- No local database — all state comes from server; on app restart, data is reloaded

---

## Module Dependencies
```
composeApp → core/domain, core/data, core/common, core/navigation, core/currency, core/logger,
             koin-compose-viewmodel, navigation-compose, material3-adaptive-*
```
