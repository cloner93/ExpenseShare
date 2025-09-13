package org.milad.expense_share.presentation.groups.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val memberIds: List<Int> = emptyList()
)