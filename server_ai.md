# 🤖 Chat & AI Bot System - Server

Real-time chat system with AI-powered bot for ExpenseShare server.

---

## 🏗️ Architecture

```mermaid
graph TB
    subgraph Client["Client (Kotlin Multiplatform)"]
        UI[Chat UI]
        WS_CLIENT[WebSocket Client]
    end
    
    subgraph Server["Server (Ktor)"]
        WSR[WebSocket Routes]
        CS[ChatService]
        BS[BotService]
        CR[ChatRepository]
    end
    
    subgraph External["External AI"]
        OR[OpenRouter API]
        AI[LLaMA 3.2 3B]
    end
    
    subgraph Database["PostgreSQL"]
        SESS[chat_sessions]
        MSG[chat_messages]
        CTX[bot_contexts]
    end
    
    UI --> WS_CLIENT
    WS_CLIENT --> WSR
    WSR --> WS_CLIENT
    
    WSR --> CS
    CS --> BS
    CS --> CR
    
    BS --> OR
    OR --> AI
    AI --> OR
    OR --> BS
    
    CR --> SESS
    CR --> MSG
    CR --> CTX
    
    style Client fill:#e1f5ff
    style Server fill:#fff3e0
    style Database fill:#f1f8e9
    style External fill:#fce4ec
```

---

## 🔄 Communication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant BotService
    participant OpenRouter
    participant Database
    
    Client->>Server: WebSocket Connect
    Server->>Database: Load History
    Database-->>Server: Chat Messages
    Server-->>Client: Send History
    
    Client->>Server: User Message
    Server->>Database: Save Message
    
    Server->>BotService: Process with AI
    BotService->>Database: Get Context
    BotService->>OpenRouter: Call AI
    OpenRouter-->>BotService: AI Response
    
    BotService-->>Server: Bot Message
    Server->>Database: Save Bot Message
    Server-->>Client: Broadcast Messages
```

---

## 💾 Database Schema

```mermaid
erDiagram
    chat_sessions ||--o{ chat_messages : contains
    chat_sessions }o--|| groups : belongs_to
    
    chat_sessions {
        int id PK
        int group_id FK
        bigint created_at
        bigint updated_at
    }
    
    chat_messages {
        int id PK
        int session_id FK
        int sender_id FK "NULL for bot"
        text content
        bigint timestamp
        varchar type "USER/BOT/SYSTEM"
        text metadata
    }
    
    bot_contexts {
        int id PK
        int session_id FK
        text context_data
        bigint last_updated
    }
```
---

## 📁 Project Structure

```
server/src/main/kotlin/org/milad/expense_share/
├── data/
│   ├── db/table/
│   │   ├── ChatSessions.kt          # Session table
│   │   ├── ChatMessages.kt          # Messages table
│   │   └── BotContexts.kt           # AI context
│   ├── models/
│   │   ├── ChatMessage.kt           # Message model
│   │   └── ChatSession.kt           # Session model
│   └── repository/
│       └── ChatRepositoryImpl.kt    # Data access
│
├── domain/
│   ├── repository/
│   │   └── ChatRepository.kt        # Repository interface
│   └── service/
│       ├── ChatService.kt           # Business logic
│       └── BotService.kt            # AI integration
│
├── presentation/
│   └── chat/
│       ├── chatRoutes.kt            # WebSocket routes
│       └── model/
│           └── SendMessageRequest.kt
│
└── application/
    ├── configureWebSockets.kt       # WebSocket config
    └── Application.kt               # App entry point
```

---

## 🔒 Security

```mermaid
flowchart LR
    A[Client] -->|JWT Token| B[WebSocket]
    B -->|Validate| C{Valid?}
    C -->|Yes| D{In Group?}
    C -->|No| E[Reject]
    D -->|Yes| F[Allow]
    D -->|No| E
    
    style F fill:#e8f5e9
    style E fill:#ffebee
```

- ✅ JWT authentication required
- ✅ Group membership validated
- ✅ Message sender verified
- ✅ SQL injection prevented (ORM)

---

## 🛠️ Troubleshooting

### **Bot Not Responding**

```bash
# Check API key
echo $OPENROUTER_API_KEY

# Check logs
tail -f logs/application.log

# Test API directly
curl https://openrouter.ai/api/v1/chat/completions \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"meta-llama/llama-3.2-3b-instruct:free","messages":[{"role":"user","content":"Hello"}]}'
```

### **Connection Issues**

```bash
# Verify server running
curl http://localhost:8082

# Check port
lsof -i :8082

# Check database
psql -U postgres -d expenseshare -c "\dt"
```

---

## 📊 Performance

| Metric | Target | Status |
|--------|--------|--------|
| WebSocket Connect | < 200ms | ✅ ~150ms |
| Bot Response | < 3s | ✅ ~2s |
| Message Delivery | < 100ms | ✅ ~50ms |

---

## 🔮 Future

- [ ] Multi-language support
- [ ] Voice messages
- [ ] Message reactions
- [ ] File attachments

---

## 📚 Resources

- **OpenRouter:** https://openrouter.ai/docs
- **Ktor WebSockets:** https://ktor.io/docs/websocket.html
- **Main Project:** ../README.md

---

**Built with Kotlin, Ktor & AI** 🚀
