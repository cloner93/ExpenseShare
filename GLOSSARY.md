# ExpenseShare Domain Glossary

ExpenseShare is a multiplatform group expense sharing and debt settlement system.

## Language

**Agent**:
An intelligent entity that interprets user intent in natural language, queries domain state, and drafts domain actions.
_Avoid_: Chatbot, copilot, assistant

**Action Draft**:
A proposed state-modifying operation created by the Agent that requires explicit user approval before execution.
_Avoid_: Unconfirmed transaction, pending mutation

**Generative Card**:
An interactive visual UI component rendered directly inside the conversational stream using the A2UI protocol.
_Avoid_: Message attachment, custom widget, arbitrary webview

**Component Catalog**:
A predefined, secure set of native Compose UI components registered on the client that the Agent can instantiate via A2UI JSON messages.
_Avoid_: Widget registry, UI kit


**Settlement**:
A calculated financial resolution between group members to balance debts.
_Avoid_: Payment, checkout

**Transaction**:
A record of an expense paid by one or more members and split among participants.
_Avoid_: Purchase, transfer, bill

**Conversation Session**:
A persistent, stateful sequence of messages and action drafts exchanged between a User and the Agent.
_Avoid_: Chat room, thread, topic

**Disambiguation**:
The interactive process of resolving ambiguous entities (such as users, groups, or currencies) via interactive selection chips presented to the User.
_Avoid_: Clarification question, error fallback

**Receipt Extraction**:
The multimodal conversion of an itemized bill or invoice image into a structured Action Draft.
_Avoid_: OCR scan, photo upload


