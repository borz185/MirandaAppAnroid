package com.miranda.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.RegisterRequest
import com.miranda.app.data.models.RegisterUserData
import com.miranda.app.ui.main.MainActivity
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tokenManager = TokenManager(this)

        // Инициализация View
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        registerButton = findViewById(R.id.register_button)
        loginLink = findViewById(R.id.login_link)

        // Кнопка Регистрация
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(name, email, password)
        }

        // Ссылка на вход
        loginLink.setOnClickListener {
            finish()
        }
    }

    private fun register(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    user = RegisterUserData(
                        full_name = name,
                        email = email,
                        password = password,
                        password_confirmation = password,
                        address = ""
                    )
                )

                val response = RetrofitClient.getApiService(this@RegisterActivity).register(request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    // ✅ ПРАВИЛЬНО: токен НЕ возвращается при регистрации
                    // Нужно сразу перейти на экран входа
                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна! Теперь войдите",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Очищаем поля
                    nameInput.text?.clear()
                    emailInput.text?.clear()
                    passwordInput.text?.clear()
                    confirmPasswordInput.text?.clear()

                    // Возвращаемся на экран входа
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "Ошибка регистрации"
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Ошибка соединения: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}