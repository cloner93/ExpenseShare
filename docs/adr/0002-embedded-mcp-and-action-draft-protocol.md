# Embedded Kotlin MCP Server and Action Draft Confirmation Protocol

## Context
Following ADR-0001, we need concrete protocols for how the Agent accesses domain tools, how clients interact with mutating financial proposals, and how conversations are maintained across client platforms.

## Decision
1. Implement the Model Context Protocol (MCP) server directly in Kotlin within the Ktor backend application, avoiding separate runtimes (Node.js/Python) and allowing direct dependency injection of domain repositories and database transactions.
2. Structure the mutation lifecycle with a deterministic Action Draft protocol:
   - The Agent creates an `ActionDraft` with an ID, action type, and payload parameters.
   - The server streams this draft to the client, which renders an inline `GenerativeCard`.
   - When the user confirms, the client dispatches a `ConfirmActionDraft` signal directly to the server.
   - The server executes the corresponding domain UseCase deterministically without re-prompting the LLM, returning the confirmed result to the session.
3. Persist conversation sessions and messages in PostgreSQL linked to the authenticated user ID.
4. Abstract the LLM client in Ktor behind a configurable interface with provider settings driven by environment variables.

## Consequences
- Zero external runtime overhead on Liara hosting; all tool execution runs inside the JVM.
- Deterministic execution of financial operations eliminates hallucination risks during confirmation.
- Users enjoy continuous multi-device conversation state across Android, Desktop, and iOS.
