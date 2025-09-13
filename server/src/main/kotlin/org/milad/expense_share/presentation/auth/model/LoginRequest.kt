package org.milad.expense_share.presentation.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val phone: String, val password: String)