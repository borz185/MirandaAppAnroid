package com.miranda.app.data.models

data class ApiResponse<T>(
    val status: String,
    val data: T?,
    val message: String?
)