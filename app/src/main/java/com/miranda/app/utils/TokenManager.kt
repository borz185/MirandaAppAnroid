package com.miranda.app.utils

import android.content.Context

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("miranda_prefs", Context.MODE_PRIVATE)
    private val TOKEN_KEY = "auth_token"

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}