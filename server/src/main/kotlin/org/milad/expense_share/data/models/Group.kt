package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val totalDebt: Double = 0.0,
    val totalCredit: Double = 0.0
)