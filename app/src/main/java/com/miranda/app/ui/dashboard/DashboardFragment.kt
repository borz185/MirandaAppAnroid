package com.miranda.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var welcomeText: TextView
    private lateinit var balanceText: TextView
    private lateinit var bonusText: TextView
    private lateinit var accountNumberText: TextView
    private lateinit var tariffNameText: TextView
    private lateinit var tariffSpeedText: TextView
    private lateinit var tariffPriceText: TextView
    private lateinit var nextBillingText: TextView
    private lateinit var tariffCard: MaterialCardView
    private lateinit var noTariffText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        // Инициализация View
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        welcomeText = view.findViewById(R.id.welcome_text)
        balanceText = view.findViewById(R.id.balance_text)
        bonusText = view.findViewById(R.id.bonus_text)
        accountNumberText = view.findViewById(R.id.account_number_text)
        tariffNameText = view.findViewById(R.id.tariff_name_text)
        tariffSpeedText = view.findViewById(R.id.tariff_speed_text)
        tariffPriceText = view.findViewById(R.id.tariff_price_text)
        nextBillingText = view.findViewById(R.id.next_billing_text)
        tariffCard = view.findViewById(R.id.tariff_card)
        noTariffText = view.findViewById(R.id.no_tariff_text)

        // Загрузка данных
        loadDashboard()

        // Обновление по свайпу
        swipeRefresh.setOnRefreshListener {
            loadDashboard()
        }
    }

    private fun loadDashboard() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getDashboard()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        updateUI(data)
                    }
                }
            } catch (e: Exception) {
                // Обработка ошибки
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI(data: com.miranda.app.data.models.DashboardResponse) {
        // Приветствие
        val firstName = data.user.full_name.split(" ").firstOrNull() ?: "Пользователь"
        welcomeText.text = "Здравствуйте, $firstName!"

        // Баланс
        val balanceColor = if (data.user.balance >= 0) {
            requireContext().getColor(R.color.balance_positive)
        } else {
            requireContext().getColor(R.color.balance_negative)
        }
        balanceText.text = "${String.format("%.2f", data.user.balance)} ₽"
        balanceText.setTextColor(balanceColor)

        // Бонусы
        bonusText.text = "${data.user.bonus_points} баллов"

        // Лицевой счёт
        accountNumberText.text = "Лицевой счёт: ${data.user.account_number}"

        // Тариф
        if (data.current_tariff != null) {
            tariffCard.visibility = View.VISIBLE
            noTariffText.visibility = View.GONE

            tariffNameText.text = data.current_tariff.name
            tariffSpeedText.text = "${data.current_tariff.speed} Мбит/с"
            tariffPriceText.text = "${String.format("%.0f", data.current_tariff.price)} ₽/мес"
            nextBillingText.text = "Следующее списание: ${data.current_tariff.next_billing_date}"
        } else {
            tariffCard.visibility = View.GONE
            noTariffText.visibility = View.VISIBLE
        }
    }
}