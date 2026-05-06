# 🚀 ExpenseShare (Compose Multiplatform + Ktor Server)

**ExpenseShare** is a **Compose Multiplatform (CMP)** application designed for seamless group expense sharing.  
Users can create groups, add friends, record transactions, and the system automatically calculates how much each member owes or is owed.  
All data is synced through the backend — the app currently uses **no local database**.

AI-powered features will be added later to assist with categorization, analysis, and smart recommendations.

This repository includes both the **CMP client** and the **Ktor backend**.

## 📸 Screenshots

Here is a glimpse of **ExpenseShare** in action across different platforms:

|                               Group List                                |                               Two pane view (Canonical layouts)                              |                               List of transaction                                |                                 Add Transaction                                 |                                       Desktop                                        |
|:-----------------------------------------------------------------------:|:-----------------------------------------------------------------------:|:-----------------------------------------------------------------------:|:-----------------------------------------------------------------------:|:------------------------------------------------------------------------------------:|
| <img src="docs/screenshot/Screenshot_20251213_152144.png" width="200"/> | <img src="docs/screenshot/Screenshot_20251213_152240.png" width="200"/> | <img src="docs/screenshot/Screenshot_20251213_152252.png" width="200"/> | <img src="docs/screenshot/Screenshot_20251213_152329.png" width="200"/> | <img src="docs/screenshot/Screenshot%202025-12-13%20at%2015.24.58.png" width="200"/> |

---

## ⭐ Core Features

### 🔹 Group Management
- Create and manage groups  
- Add members  
- View group activity and balances  

### 🔹 Friends Management
- Add and manage friends  
- Create groups using your friend list  
- Friend relationships synced with the backend  

### 🔹 Transactions
- Add expenses  
- Select payer  
- Split cost among group members  
- All members can add new transactions  

### 🔹 Automatic Settlement Calculation
- Real-time debt/credit calculation  
- Clear overview of who owes whom  
- Group updates propagate to all members  

### 🔹 Future Enhancements
- AI-based transaction classification  
- Smart settlement suggestions  
- Expense pattern insights  


## 🧱 Architecture Overview

### Client – Compose Multiplatform

Supported platforms:

- Android  
- iOS  
- Desktop (JVM: Windows, macOS, Linux)  
- Web (WASM)  

### Tech Stack
- Clean Architecture  
- MVI  
- Compose Multiplatform  
- Koin  
- Coroutines + Flow  
- No local DB (server = source of truth)

### Navigation Graph

The app uses a hierarchical navigation structure:

```mermaid
graph TD
    Root[RootRoute] --> Auth[AuthRoute]
    Root --> Main[MainRoute]
    
    subgraph AuthFlow [Authentication Flow]
        Auth --> Login[Login Screen]
        Auth --> Register[Register Screen]
    end
    
    subgraph MainFlow [Main Application Flow]
        Main --> Dashboard[Dashboard Screen]
        Main --> Friends[Friends Screen]
        Main --> Profile[Profile Screen]
        
        Dashboard --> GroupDetail[Group Detail View]
        Dashboard --> AddTransaction[Add Transaction View]
        
        Friends --> FriendDetail[Friend Detail View]
    end
```

- **RootRoute**: Handles switching between `Auth` and `Main` flows.
  - **AuthRoute**: Contains `Login` and `Register` screens.
  - **MainRoute**: Contains `Dashboard`, `Friends`, and `Profile` screens.

### State Management & Persistence

#### ViewModel Architecture (MVI)
The project follows the MVI (Model-View-Intent) pattern using a `BaseViewModel`. 
- **State**: Managed via `StateFlow`. The UI observes `viewState` to react to changes.
- **Actions**: The UI sends `Actions` (e.g., `LoadData`, `SelectGroup`) to the ViewModel via the `handle()` function.
- **Events**: One-time side effects (e.g., showing a Toast, navigating) are emitted via `SharedFlow`.

#### In-Memory Data Sync
To ensure a smooth and responsive user experience, the application maintains the latest data in memory within the `ViewModel` state:
- **Reactive Updates**: When a transaction is added or a group is updated, the `ViewModel` updates its internal `groups` list by applying the changes to the existing state.
- **Single Source of Truth (Client-side)**: While the server is the ultimate source of truth, the `ViewModel` state acts as a local cache. When an API call succeeds, the `ViewModel` surgically updates the specific entity (e.g., adding a `Transaction` to a `Group`) in the `groups` list. 
- **Seamless UI**: This approach allows the UI to reflect changes immediately without requiring a full screen reload or a new fetch from the network, provided the local state is correctly synchronized with the server's response.

- **commonMain** → Shared UI & domain logic  
- **androidMain** → Android-specific  
- **iosMain** → iOS integration  
- **jvmMain** → Desktop  
- **wasmJsMain** → Web (WASM)

---

## 🖥️ Server – Ktor Backend

The backend manages all persistence & logic:

- Ktor Server  
- PostgreSQL  
- Exposed ORM  
- Authentication  
- Groups, Friends, Transactions APIs  
- Settlement calculation  

Folder:
```
/server
```

---

## ▶️ Running the Project

### Desktop (JVM)
```
./gradlew jvmRun -DmainClass=org.milad.expense_share.MainKt --quiet
```

### Server
```
./gradlew :server:run
```

### Tests (Kotest)
```
./gradlew kotest
```

### Web (WASM)
```
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

---

## 💡 Why ExpenseShare Matters

This project demonstrates:

- Production-grade Compose Multiplatform  
- Clean modular design  
- Full backend with Ktor + PostgreSQL  
- Multi-platform targeting (Android/iOS/Web/Desktop)  
- Extendable architecture for upcoming AI features  

A strong showcase of modern Kotlin ecosystem practices.
