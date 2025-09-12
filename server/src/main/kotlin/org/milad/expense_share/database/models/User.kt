package org.milad.expense_share.database.models

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val username: String, val phone: String)