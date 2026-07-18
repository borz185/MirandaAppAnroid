package com.miranda.app.data.models

// Модель услуги
data class ServiceData(
    val id: Int,
    val name: String,
    val monthly_fee: Double?,
    val description: String?,
    val is_enabled: Boolean?,
    val is_bonus: Boolean?
)

// Запрос на переключение услуги
data class ToggleServiceRequest(
    val is_enabled: Boolean
)

// Ответ при переключении услуги
data class ServiceToggleResponse(
    val id: Int,
    val name: String,
    val is_enabled: Boolean,
    val balance: Double
)