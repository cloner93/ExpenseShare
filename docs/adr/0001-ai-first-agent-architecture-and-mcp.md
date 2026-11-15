# Server-side AI Agent Orchestration with MCP and Human-in-the-Loop

## Context
ExpenseShare is adopting an AI-first design where conversational interaction with an Agent is the primary interface for managing shared expenses, groups, and settlements across all supported platforms.

## Decision
We decided to:
1. Implement Agent orchestration on the backend server (Ktor), streaming tokens and UI event updates to clients.
2. Expose domain business capabilities as standard Model Context Protocol (MCP) tools on the server.
3. Enforce a strict Human-in-the-Loop policy where read queries run autonomously, but mutating financial operations generate an Action Draft requiring explicit user confirmation.
4. Redesign the client home screen to be conversational-first with inline Generative Cards, while keeping traditional screens as secondary views.

## Consequences
- Protects LLM API credentials on the server and provides centralized telemetry and caching.
- Standardizes tool integration via MCP, enabling decoupled testing and potential reuse by external agent tools.
- Prevents unintended financial transactions caused by model hallucinations.
- Introduces real-time streaming infrastructure and dynamic component rendering requirements in the client application.
