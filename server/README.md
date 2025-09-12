# Project Structure

This project follows a layered architecture for better separation of concerns and scalability.

```
src/
 └── main/
      ├── kotlin/
      │    └── org/milad/expense_share/
      │         ├── application/           
      │         │    ├── Application.kt        # Entry point (main/module)
      │         │    └── plugins/              # Ktor plugins (Auth, Routing, Serialization, etc.)
      │         │
      │         ├── data/                      
      │         │    ├── db/                   # Database configuration and migrations
      │         │    ├── repository/           # Repository implementations (e.g., InMemoryUserRepository)
      │         │    └── entity/               # Database entities
      │         │
      │         ├── domain/                    
      │         │    ├── model/                # Core business models (User, Transaction, etc.)
      │         │    ├── repository/           # Repository interfaces (e.g., UserRepository)
      │         │    └── service/              # Business logic (e.g., AuthService, UserService)
      │         │
      │         ├── presentation/              
      │         │    └── auth/                 # HTTP routes/controllers (e.g., authRoutes.kt)
      │         │
      │         ├── security/                  # Authentication & authorization (JwtConfig, AuthConfig)
      │         │
      │         └── utils/                     # Validation, error handling, helper utilities
      │
      └── resources/
           ├── application.conf                # Application configuration
           └── db/migrations/                  # Database migration scripts (Flyway/Liquibase)
```

---

## Layer Responsibilities

- **application/** → Application entry point and Ktor plugin setup.
- **data/** → Data persistence logic and database integration.
- **domain/** → Core business logic, models, repository contracts, and services.
- **presentation/** → API routes and controllers (only handle HTTP request/response).
- **security/** → Authentication and authorization logic.
- **utils/** → Shared utilities (validation, error response handling, etc.).
- **resources/** → Configuration and migration scripts.  
