package com.example.labs
// PaymentModel.kt
import java.time.LocalDateTime

data class PaymentOperation(
    val operationId: String,
    val status: String,
    val datetime: LocalDateTime,
    val title: String?,
    val patternId: String?,
    val direction: String,
    val amount: Double,
    val label: String?,
    val type: String?
)

data class PaymentLink(
    val baseUrl: String,
    val redirectedUrl: String,
    val paymentUrl: String
)