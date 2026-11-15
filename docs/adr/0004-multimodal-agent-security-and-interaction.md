# Multimodal Inputs, Agent Security Context, and Interactive Disambiguation

## Context
Following the adoption of embedded MCP tools (ADR-0002) and A2UI generative streaming (ADR-0003), we need to establish the security boundaries for tool execution and specify how multimodal inputs (voice and receipt images) and conversational ambiguities are handled.

## Decision
1. Enforce strict authorization boundaries: Inject an `AgentSecurityContext` carrying the verified user ID (from JWT authentication) into every MCP tool call. The tool execution layer validates that the user is an active member of any group being queried or modified.
2. Support voice-first interaction on clients through a voice recording interface with speech-to-text transcription.
3. Support multimodal receipt processing: When a user shares a receipt image, the Agent uses multimodal LLM vision to extract merchant, total, line items, and date, outputting an `ActionDraft` rendered as an A2UI Expense Card for verification.
4. Resolve ambiguous references (e.g. identical contact names, unspecified groups, or split methods) by emitting interactive `Disambiguation` chips via A2UI instead of guessing or failing.

## Consequences
- Prevents cross-tenant / cross-group data leakage; the LLM cannot bypass authorization rules even if prompted to do so.
- Eliminates manual receipt data entry, accelerating user adoption and satisfaction.
- Keeps conversational interactions smooth and zero-friction through one-tap choice chips.
