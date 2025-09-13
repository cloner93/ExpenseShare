package org.milad.expense_share.presentation.auth.model

import kotlinx.serialization.Serializable


@Serializable
data class RegisterRequest(val username: String, val phone: String, val password: String)