package com.miranda.app.data.models

// Модель транзакции (платежа)
data class PaymentData(
    val id: Int,
    val amount: Double,
    val description: String,
    val created_at: String,
    val status: String? = "completed" // Добавили дефолтное значение, т.к. в JSON его нет
)

// Запрос на пополнение баланса
data class TopUpRequest(
    val amount: Double
)

// Ответ при пополнении (сделаем поля nullable на всякий случай)
data class TopUpResponse(
    val balance: Double? = 0.0,
    val payment: PaymentData? = null
)