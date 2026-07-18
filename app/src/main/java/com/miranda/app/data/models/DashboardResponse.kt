package com.miranda.app.data.models

data class DashboardResponse(
    val user: UserData,
    val current_tariff: TariffData?,
    val enabled_services: List<ServiceData>,
    val promotions: List<PromotionData>
)

data class UserData(
    val id: Int,
    val full_name: String,
    val account_number: String,
    val balance: Double,
    val bonus_points: Int
)

data class TariffData(
    val id: Int,
    val name: String,
    val speed: Int,
    val price: Double,
    val description: String?,
    val next_billing_date: String
)



data class PromotionData(
    val id: Int,
    val title: String,
    val description: String,
    val image_url: String
)

data class TariffSelectionResponse(
    val id: Int,
    val name: String,
    val speed: Int,
    val price: Double,
    val balance: Double,
    val next_billing_date: String
)