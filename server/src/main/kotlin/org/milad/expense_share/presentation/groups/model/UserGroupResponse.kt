package org.milad.expense_share.presentation.groups.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.data.models.Transaction
import org.milad.expense_share.data.models.User

@Serializable
data class UserGroupResponse(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val members: List<User>,
    val transactions: List<Transaction>
)