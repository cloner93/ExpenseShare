package org.milad.expense_share.dashboard.model

data class GroupUiModel(
    val name: String,
    val membersCount: Int,
//    val imageRes: DrawableResource,
    val balanceText: String,
    val isOwed: Boolean
)