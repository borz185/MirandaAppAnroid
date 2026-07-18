package com.miranda.app.data.models

// Запрос на вход
data class LoginRequest(
    val email: String,
    val password: String
)

// Ответ при входе
data class LoginResponse(
    val token: String,
    val user: UserDataSimple
)

data class UserDataSimple(
    val id: Int,
    val email: String,
    val full_name: String,
    val account_number: String
)

// Запрос на регистрацию
data class RegisterRequest(
    val user: RegisterUserData
)

data class RegisterUserData(
    val email: String,
    val password: String,
    val password_confirmation: String,
    val full_name: String,
    val address: String
)