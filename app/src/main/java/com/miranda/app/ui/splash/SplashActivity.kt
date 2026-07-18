package com.miranda.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.miranda.app.R
import com.miranda.app.ui.auth.LoginActivity
import com.miranda.app.ui.main.MainActivity
import com.miranda.app.utils.TokenManager

class SplashActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        tokenManager = TokenManager(this)

        // Задержка 1.5 секунды для показа логотипа
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthStatus()
        }, 1500)
    }

    private fun checkAuthStatus() {
        val intent = if (tokenManager.isLoggedIn()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish() // Закрываем Splash, чтобы нельзя было вернуться назад
    }
}