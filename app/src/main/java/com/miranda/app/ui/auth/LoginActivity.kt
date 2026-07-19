package com.miranda.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.LoginRequest
import com.miranda.app.data.models.LoginResponse
import com.miranda.app.ui.main.MainActivity
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tokenManager = TokenManager(this)

        // Инициализация View
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        // Проверка авторизации
        if (tokenManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        // Кнопка Войти
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(email, password)
        }

        // Кнопка Регистрация
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@LoginActivity)
                    .login(LoginRequest(email, password))

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        // Сохраняем токен
                        tokenManager.saveToken(data.token)

                        Toast.makeText(
                            this@LoginActivity,
                            "Вход выполнен успешно",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateToMain()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Ошибка входа"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Ошибка соединения: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}