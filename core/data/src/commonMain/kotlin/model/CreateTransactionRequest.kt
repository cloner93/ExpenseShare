package model

data class CreateTransactionRequest(val title: String, val amount: Double, val description: String?)
