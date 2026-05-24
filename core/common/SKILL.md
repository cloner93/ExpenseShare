# core/common — SKILL.md

## Purpose
Shared UI infrastructure for all platforms. Provides:
- MVI ViewModel base class
- App theme (Material 3 + custom colors)
- Adaptive navigation scaffold
- Reusable UI components (loading, empty states)

---

## MVI Architecture — BaseViewModel

```kotlin
abstract class BaseViewModel<ACTION : BaseViewAction, STATE : BaseViewState, EVENT : BaseViewEvent>(
    initialState: STATE
) : ViewModel() {
    val viewState: StateFlow<STATE>
    val viewEvent: SharedFlow<EVENT>

    abstract fun handle(action: ACTION)
    protected fun setState(reducer: (currentState: STATE) -> STATE)
    protected fun postEvent(event: EVENT)
}
```

### Pattern for new ViewModels
```kotlin
// 1. Define interfaces
sealed interface MyAction : BaseViewAction { ... }
data class MyState(...) : BaseViewState
sealed interface MyEvent : BaseViewEvent { ... }

// 2. Implement ViewModel
class MyViewModel(...) : BaseViewModel<MyAction, MyState, MyEvent>(
    initialState = MyState()
) {
    override fun handle(action: MyAction) {
        when (action) { ... }
    }
}

// 3. In Composable
val state by viewModel.viewState.collectAsState()
LaunchedEffect(Unit) {
    viewModel.viewEvent.collect { event -> /* handle one-time events */ }
}
viewModel.handle(MyAction.SomeAction)
```

### Key Rules
- `setState` is atomic — always use the `currentState` parameter, never `viewState.value`
- `viewEvent` is for ONE-TIME events (navigation, toast) — not ongoing state
- `viewState` is for persistent UI state
- Use `viewModelScope.launch` for async operations inside `handle()`

---

## Theme System

### AppTheme
```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    contrastLevel: Int = 0,   // 0=normal, 1=medium, 2=high
    content: @Composable () -> Unit,
)
```

### Color Access — AppTheme.colors
**Always use `AppTheme.colors.*` — NEVER use `MaterialTheme.colorScheme.*` directly in UI.**

```kotlin
// Standard Material 3 colors (delegated internally):
AppTheme.colors.primary
AppTheme.colors.onSurface
AppTheme.colors.errorContainer
AppTheme.colors.surfaceVariant
// ... all standard M3 colors

// Custom extended colors (not in M3):
AppTheme.colors.success
AppTheme.colors.onSuccess
AppTheme.colors.successContainer
AppTheme.colors.onSuccessContainer
```

### Typography & Shapes
```kotlin
AppTheme.typography.titleMedium    // custom font (Adlam Display)
AppTheme.typography.bodyLarge
AppTheme.shapes.medium
```

Custom font: **Adlam Display Regular** — loaded from resources, applied to all typography styles.

---

## Adaptive Navigation Scaffold

### AppScaffold
Adapts navigation type based on screen size:

| Screen Size | Navigation Type |
|-------------|----------------|
| Compact (phone) | `NavigationBar` (bottom) |
| Medium (tablet portrait) | `NavigationRail` (side) |
| Expanded ≥1200dp | `NavigationDrawer` (permanent) |

```kotlin
AppScaffold(
    selectedItem = selectedItem,
    onItemSelected = { item -> /* navigate */ },
    onAddGroupClick = { /* show add group */ },
    showAddGroupButton = selectedItem == NavItem.Dashboard,
) { navLayoutType ->
    // navLayoutType: NavigationSuiteType
    // Content here
}
```

### NavItem Enum
```kotlin
enum class NavItem(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Friends("Friends", Icons.Default.People),
    Profile("Profile", Icons.Default.Settings)
}
```

---

## Reusable Components

### FullScreenLoading
```kotlin
@Composable
fun FullScreenLoading()
// Semi-transparent overlay with CircularProgressIndicator
// Blocks all touch input via pointerInput
```
Usage: show on top of content during async operations.

### EmptyListState
```kotlin
@Composable
fun EmptyListState(
    modifier: Modifier = Modifier.fillMaxSize(),
    title: String = "No Items Found",
    subtitle: String = "..."
)
// Animated (scaling) empty state with inbox icon
```

### EmptySelectionPlaceholder
```kotlin
@Composable
fun EmptySelectionPlaceholder(
    modifier: Modifier = Modifier.fillMaxSize(),
    title: String = "Nothing Selected",
    subtitle: String = "..."
)
// Floating animated placeholder for list-detail pane when no item is selected
```

---

## Rules
- Always wrap screens in `AppTheme` at the root — never apply it per-screen
- Use `AppTheme.colors` not `MaterialTheme.colorScheme` for color access
- `setState` reducer must be pure — no side effects, no network calls inside it
- `postEvent` uses `SharedFlow` — missed events if no collector is active; always collect in `LaunchedEffect(Unit)`
- The `showAddGroupButton` flag should be `false` on Profile screen

---

## Module Dependencies
```
core/common → compose-*, material3-adaptive-*, navigation-compose, core/navigation,
              kotlinx-coroutines-core, lifecycle-viewmodel-compose
```
