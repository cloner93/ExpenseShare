package org.milad.expense_share.utils

import io.ktor.server.auth.jwt.JWTPrincipal
import org.milad.expense_share.model.LoginRequest
import org.milad.expense_share.model.RegisterRequest

internal fun JWTPrincipal?.getUserId(): Int? {
    return this?.payload?.getClaim("id")?.asInt()
}

internal fun io.ktor.server.application.ApplicationCall.getIntParameter(name: String): Int? {
    return parameters[name]?.toIntOrNull()
}
internal fun io.ktor.server.application.ApplicationCall.getStringParameter(name: String): String? {
    return parameters[name]?.takeIf { it.isNotBlank() }
}

internal fun RegisterRequest.validate(): String? {
    return when {
        username.isBlank() -> "Username is required"
        username.length < 3 -> "Username must be at least 3 characters"
        phone.isBlank() -> "Phone number is required"
        phone.length < 10 -> "Phone number must be at least 10 digits"
        password.isBlank() -> "Password is required"
        password.length < 4 -> "Password must be at least 4 characters"
        else -> null
    }
}

internal fun LoginRequest.validate(): String? {
    return when {
        phone.isBlank() -> "Phone number is required"
        password.isBlank() -> "Password is required"
        else -> null
    }
}