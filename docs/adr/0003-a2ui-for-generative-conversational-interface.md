# A2UI Protocol for Generative Conversational UI

## Context
Following the decision to adopt an AI-first conversational interface (ADR-0001) and embedded Kotlin MCP tools (ADR-0002), we need a standardized, secure mechanism for streaming rich interactive UI components (cards, forms, confirmation dialogs) from the server Agent to multiplatform Compose clients.

## Decision
1. Adopt the open-standard A2UI (Agent-to-User Interface) protocol for dynamic generative UI streaming.
2. Maintain a secure, pre-compiled `Component Catalog` in the Compose Multiplatform client (`composeApp`) containing native domain widgets (such as expense draft cards, balance charts, settlement resolution cards, and confirmation actions).
3. The server-side Agent emits A2UI JSON protocol payloads referencing components in this catalog over the bidirectional WebSocket connection, rather than sending arbitrary code or unstructured markdown.
4. User interactions with A2UI components dispatch structured events back to the server over the WebSocket to complete the Action Draft lifecycle.
5. In offline scenarios, the client displays a connection status banner and provides an immediate one-tap shortcut to fall back to traditional manual entry screens.

## Consequences
- High security: The agent cannot execute arbitrary code or scripts on the user's device, only instantiate pre-compiled native components from the catalog.
- Native performance: A2UI protocol instructions map directly to Jetpack Compose / Compose Multiplatform state snapshots, guaranteeing 60fps animations, dark/light theme fidelity, and cross-platform consistency.
- Standardized UI generation: Leverages standard A2UI component schemas for LLM tool calling and generative output.
