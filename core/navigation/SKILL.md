# core/navigation — SKILL.md

## Purpose
Defines all navigation route contracts for the Compose Multiplatform app using `kotlinx.serialization`.
Routes are type-safe sealed interfaces used with Jetpack Navigation Compose.

---

## Route Hierarchy

```
RootRoute          → top-level: Auth or Main
├── AuthRoute      → Login, Register
└── MainRoute      → Dashboard, Friends, Profile
```

---

## Route Definitions

```kotlin
// Root navigation
sealed interface RootRoute {
    @Serializable data object Auth : RootRoute
    @Serializable data object Main : RootRoute
}

// Auth flow
sealed interface AuthRoute {
    @Serializable data object Login : AuthRoute
    @Serializable data object Register : AuthRoute
}

// Main app tabs
sealed interface MainRoute {
    @Serializable data object Dashboard : MainRoute
    @Serializable data object Friends : MainRoute
    @Serializable data object Profile : MainRoute
}
```

---

## Navigation Flow

```
App Start
└── RootRoute.Auth
    ├── AuthRoute.Login  (startDestination)
    └── AuthRoute.Register
        → on success → RootRoute.Main (popUpTo Auth inclusive)

RootRoute.Main
├── MainRoute.Dashboard  (startDestination)
├── MainRoute.Friends
└── MainRoute.Profile
    → on logout → RootRoute.Auth (popUpTo Main inclusive)
```

---

## Usage Pattern

### Navigating
```kotlin
// Navigate and clear back stack
navController.navigate(RootRoute.Main) {
    popUpTo(RootRoute.Auth) { inclusive = true }
}

// Navigate preserving state
navController.navigate(MainRoute.Friends) {
    popUpTo(navController.graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

### Declaring composable destinations
```kotlin
NavHost(startDestination = RootRoute.Auth) {
    composable<RootRoute.Auth> { ... }
    composable<RootRoute.Main> { ... }
}
```

---

## All Transitions are None
```kotlin
// Applied globally in AppEntryPoint and AuthNavHost:
enterTransition = { EnterTransition.None }
exitTransition = { ExitTransition.None }
popEnterTransition = { EnterTransition.None }
popExitTransition = { ExitTransition.None }
```
Do NOT add custom transitions unless explicitly requested.

---

## Rules
- All routes MUST be `@Serializable` — required by Navigation Compose type-safe API
- All routes are `data object` (no parameters currently) — if adding route params, use `data class`
- Do NOT define UI or ViewModels in this module — routes only
- The `NavItem` enum (Dashboard/Friends/Profile) lives in `core/common`, not here

---

## Module Dependencies
```
core/navigation → kotlinx-serialization-json
```

---

## Platforms Supported
Android, iOS, JVM, wasmJs
