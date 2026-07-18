package com.miranda.app.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.RegisterRequest
import com.miranda.app.data.models.RegisterUserData
import com.miranda.app.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            attemptRegister()
        }

        binding.loginText.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegister() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val passwordConfirm = binding.passwordConfirmInput.text.toString()
        val fullName = binding.fullNameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()

        // Валидация
        if (email.isEmpty()) {
            binding.emailLayout.error = "Введите email"
            return
        }
        binding.emailLayout.error = null

        if (password.length < 6) {
            binding.passwordLayout.error = "Пароль должен быть не менее 6 символов"
            return
        }
        binding.passwordLayout.error = null

        if (password != passwordConfirm) {
            binding.passwordConfirmLayout.error = "Пароли не совпадают"
            return
        }
        binding.passwordConfirmLayout.error = null

        if (fullName.isEmpty()) {
            binding.fullNameLayout.error = "Введите имя"
            return
        }
        binding.fullNameLayout.error = null

        // Показываем индикатор загрузки
        showLoading(true)

        // Выполняем запрос
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    user = RegisterUserData(
                        email = email,
                        password = password,
                        password_confirmation = passwordConfirm,
                        full_name = fullName,
                        address = address
                    )
                )

                val response = RetrofitClient.getApiService(this@RegisterActivity).register(request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    Snackbar.make(
                        binding.root,
                        "Регистрация успешна! Теперь войдите в систему",
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(getColor(R.color.success)).show()

                    finish() // Возвращаемся на экран входа
                } else {
                    val errorMessage = response.body()?.message ?: "Ошибка регистрации"
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
        binding.registerButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
        binding.passwordInput.isEnabled = !isLoading
        binding.passwordConfirmInput.isEnabled = !isLoading
        binding.fullNameInput.isEnabled = !isLoading
        binding.addressInput.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .show()
    }
}