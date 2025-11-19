package org.milad.expense_share.presentation.groups.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val memberIds: List<Int> = emptyList()
)

//@Serializable
//data class CreateGroupRequest(
//    val title: String,
//    val amount: Double,
//    val description: String?,
//    val payers: List<PayerRequest>?,
//    val splitDetails: ShareDetailsRequest?,
//)