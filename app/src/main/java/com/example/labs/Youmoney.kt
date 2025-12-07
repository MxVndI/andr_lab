package com.example.labs.model

data class YooMoneyPayment(
    val id: String,
    val name: String,
    val description: String,
    val coinsAmount: Int,
    val amount: Double, // Сумма в рублях
    val walletNumber: String, // Ваш номер кошелька 4100...
    val comment: String? = null // Комментарий для идентификации
)