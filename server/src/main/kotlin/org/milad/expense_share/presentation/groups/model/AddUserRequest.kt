package org.milad.expense_share.presentation.groups.model

import kotlinx.serialization.Serializable

@Serializable
data class AddUserRequest(
    val memberIds: List<Int> = emptyList()
)