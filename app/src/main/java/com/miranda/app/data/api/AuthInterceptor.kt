package com.miranda.app.data.api

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val tokenManager = com.miranda.app.utils.TokenManager(context)

    init {
        Log.d("AuthInterceptor", "Interceptor создан")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        Log.d("AuthInterceptor", "Запрос: ${originalRequest.url}")
        Log.d("AuthInterceptor", "Токен из SharedPreferences: ${if (token.isNullOrEmpty()) "ПУСТОЙ" else "ЕСТЬ (${token.take(20)}...)"}")

        val requestWithAuth = if (!token.isNullOrEmpty()) {
            Log.d("AuthInterceptor", "Добавляем заголовок Authorization")
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.d("AuthInterceptor", "Токен пустой, отправляем без заголовка")
            originalRequest
        }

        val response = chain.proceed(requestWithAuth)
        Log.d("AuthInterceptor", "Ответ: ${response.code}")

        return response
    }
}