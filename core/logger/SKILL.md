# core/logger — SKILL.md

## Purpose
Cross-platform logging abstraction. Provides a single `AppLogger` API with platform-specific implementations.
Zero dependencies on other project modules.

---

## API

```kotlin
expect object AppLogger {
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
}
```

### Usage Pattern
```kotlin
AppLogger.i("NetworkManager", "Response: $response")
AppLogger.e("NetworkManager", "Exception: ${e.stackTraceToString()}")
AppLogger.d("DashboardViewModel", "Groups loaded: ${groups.size}")
```

---

## Platform Implementations

| Platform | Underlying Logger |
|----------|------------------|
| Android | `android.util.Log` |
| iOS | `NSLog` |
| JVM (Desktop/Server) | `java.util.logging.Logger` |
| wasmJs (Web) | `println()` with prefix |

### Format per Platform
- Android: `Log.i(tag, message)` — standard Logcat
- iOS: `NSLog("INFO: [tag] message")`
- JVM: `logger.info("INFO: [tag] message")`
- Web: `println("INFO: [tag] message")`

---

## Rules
- **Do NOT use `println()` directly** in shared code — use `AppLogger`
- **Do NOT use `android.util.Log`** outside androidMain
- Tag convention: use the class or feature name, e.g. `"NetworkManager"`, `"DashboardViewModel"`
- The server module does NOT use AppLogger — it uses Logback (SLF4J) via `application.conf`

---

## Module Dependencies
```
core/logger → (none)
```

---

## Platforms Supported
Android, iOS, JVM, wasmJs
