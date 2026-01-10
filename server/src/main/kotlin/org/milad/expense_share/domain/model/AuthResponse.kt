package org.milad.expense_share.domain.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.data.models.User

@Serializable
data class AuthResponse(
    val token: String? = null,
    val user: User? = null,
)