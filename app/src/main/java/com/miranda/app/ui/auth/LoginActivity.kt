package com.miranda.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.LoginRequest
import com.miranda.app.databinding.ActivityLoginBinding
import com.miranda.app.ui.main.MainActivity
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }

        binding.registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        // Валидация
        if (email.isEmpty()) {
            binding.emailLayout.error = "Введите email"
            return
        }
        binding.emailLayout.error = null

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Введите пароль"
            return
        }
        binding.passwordLayout.error = null

        // Показываем индикатор загрузки
        showLoading(true)

        // Выполняем запрос
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(this@LoginActivity).login(
                    LoginRequest(email, password)
                )

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        // Сохраняем токен
                        tokenManager.saveToken(data.token)

                        // ✅ ДОБАВЬТЕ ЭТУ СТРОКУ:
                        android.util.Log.d("LoginActivity", "Токен сохранен: ${data.token.take(20)}...")

                        Snackbar.make(
                            binding.root,
                            "Вход выполнен успешно",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        // Переходим на главный экран
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Неверный email или пароль"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
        binding.passwordInput.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }
}