package com.miranda.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.ui.auth.LoginActivity
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var accountText: TextView
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        nameText = view.findViewById(R.id.profile_name)
        emailText = view.findViewById(R.id.profile_email)
        accountText = view.findViewById(R.id.profile_account)
        btnLogout = view.findViewById(R.id.btn_logout)

        // Загружаем данные профиля
        loadProfile()

        // Обработчик кнопки выхода
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getProfile()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val user = response.body()?.data
                    if (user != null) {
                        nameText.text = user.full_name
                        emailText.text = user.email
                        accountText.text = "Лицевой счёт: ${user.account_number}"
                    }
                } else {
                    showError("Не удалось загрузить профиль")
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                // Отправляем запрос на выход (опционально, но хорошая практика)
                RetrofitClient.getApiService(requireContext()).logout()
            } catch (e: Exception) {
                // Игнорируем ошибки сети при выходе, главное — очистить локально
            } finally {
                // Очищаем токен локально
                tokenManager.clearToken()

                // Перенаправляем на экран входа
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                Snackbar.make(requireView(), "Вы успешно вышли из системы", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error))
            .show()
    }
}