# core/currency — SKILL.md

## Purpose
This module defines the monetary value type used across the entire project (client + server).
It is a shared KMP module with **zero dependencies** on other project modules.

---

## Key Type: `Amount`

```kotlin
@Serializable(with = AmountSerializer::class)
@JvmInline
value class Amount(val value: Long) : Comparable<Amount>
```

### CRITICAL Rules
- `Amount` stores money as **Long (integer minor units)**, NOT Double or Float
- Never use `Double` or `Float` for money in this project
- `Amount(500)` means 500 units (e.g. 500 Tomans, not 5.00)
- Division uses integer division: `Amount(100) / 3 = Amount(33)` (truncates, no rounding)

### Constructor Overloads
```kotlin
Amount(value: Long)       // primary
Amount(value: String)     // parses string, empty string → Amount(0)
```

### Available Operators
```kotlin
+, -, *, /                // all return Amount
unaryMinus()              // negation
compareTo(Amount/Long/Double/Int)
abs(), isPositive(), isNegative(), isZero()
```

### Multiplication with non-Amount
```kotlin
// For percent/weight calculations:
amount * multiplier.toLong()   // NOT amount * multiplier (Float won't compile)

// Percent example:
val userAmount = amount * (percent / 100f).toLong()  // loses decimal precision
// Better pattern used in TransactionRepositoryImpl:
Amount((amount.value * (percent / 100.0)).toLong())
```

### Serialization
- `AmountSerializer` serializes/deserializes as `Long` (BIGINT in DB, number in JSON)
- Both client and server use the same serializer
- JSON wire format: `"amount": 500` (not `"amount": "500"` or `"amount": 5.00`)

### Display Helper
```kotlin
fun Amount.showSeparate(): String   // "1,234,567" format with commas
fun Long.showSeparate(): String     // same for raw Long
```

---

## AmountColumnType (Server-only)
Custom Exposed ORM column type for PostgreSQL:
```kotlin
// In Transactions.kt:
fun Table.amount(name: String): Column<Amount>

// Usage:
val amount = amount("amount")      // → BIGINT column in DB
val amountPaid = amount("amount_paid")
```

**Never** use `double()` or `decimal()` column types for money in this project.

---

## Common Mistakes to Avoid

| Wrong | Correct |
|-------|---------|
| `Amount(500.0)` | `Amount(500L)` |
| `amount.value.toDouble()` for display | `amount.showSeparate()` |
| `amount * 0.5f` | `Amount((amount.value * 0.5).toLong())` |
| Storing money as Double in DB | Use `amount()` column type (BIGINT) |
| `Amount(it.share.value)` for percent calc | `Amount((amount.value * share / 100.0).toLong())` |

---

## Module Dependencies
```
core/currency → (none)
```
All other modules depend on this module. Do NOT add dependencies to other project modules here.

---

## Platforms Supported
Android, iOS, JVM (Desktop + Server), wasmJs (Web)
