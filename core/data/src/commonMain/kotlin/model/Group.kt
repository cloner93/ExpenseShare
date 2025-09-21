package model

data class Group(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val totalDebt: Double = 0.0,
    val totalCredit: Double = 0.0
)