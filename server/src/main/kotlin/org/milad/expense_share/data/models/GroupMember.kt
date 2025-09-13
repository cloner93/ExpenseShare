package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val groupId: Int,
    val userId: Int
)