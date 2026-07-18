package com.miranda.app.data.models

data class PromotionData(
    val id: Int,
    val title: String,
    val description: String,
    val image_url: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val discount: Int? = null,
    val color: String? = null
)