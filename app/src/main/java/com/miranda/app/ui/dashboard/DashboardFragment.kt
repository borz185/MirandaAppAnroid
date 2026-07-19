package com.miranda.app.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.DashboardResponse
import com.miranda.app.data.models.PromotionData
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
    private lateinit var promotionsViewPager: ViewPager2
    private lateinit var promotionsIndicator: LinearLayout

    private lateinit var promotionsAdapter: PromotionsAdapter
    private var promotionsList = mutableListOf<PromotionData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        // 1. Инициализация View
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
        promotionsViewPager = view.findViewById(R.id.promotions_viewpager)
        promotionsIndicator = view.findViewById(R.id.promotions_indicator)

        // Скрываем карусель до успешной загрузки данных
        promotionsViewPager.visibility = View.GONE
        promotionsIndicator.visibility = View.GONE

        // 2. Загружаем реальные акции с сервера
        loadPromotions()

        // 3. Загрузка данных дашборда
        loadDashboard()

        // 4. Свайп для обновления
        swipeRefresh.setOnRefreshListener {
            loadDashboard()
            loadPromotions()
        }
    }

    private fun loadDashboard() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getDashboard()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) updateUI(data)
                }
            } catch (e: Exception) {
                Log.e("DashboardFragment", "Ошибка дашборда: ${e.message}")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI(data: DashboardResponse) {
        val firstName = data.user.full_name.split(" ").firstOrNull() ?: "Пользователь"
        welcomeText.text = "Здравствуйте, $firstName!"

        balanceText.text = "${String.format("%.2f", data.user.balance)} ₽"
        balanceText.setTextColor(requireContext().getColor(android.R.color.white))
        bonusText.text = "${data.user.bonus_points} баллов"
        accountNumberText.text = "Лицевой счёт: ${data.user.account_number}"

        if (data.current_tariff != null) {
            tariffCard.visibility = View.VISIBLE
            noTariffText.visibility = View.GONE
            tariffNameText.text = data.current_tariff.name
            tariffSpeedText.text = "${data.current_tariff.speed} Мбит/с"
            tariffPriceText.text = "${String.format("%.0f", data.current_tariff.price)} ₽/мес"
            nextBillingText.text = "Следующее списание: ${com.miranda.app.utils.DateUtils.formatDateLong(data.current_tariff.next_billing_date)}"
        } else {
            tariffCard.visibility = View.GONE
            noTariffText.visibility = View.VISIBLE
        }
    }

    private fun loadPromotions() {
        lifecycleScope.launch {
            try {
                Log.d("DashboardFragment", "Загрузка акций с сервера...")
                val response = RetrofitClient.getApiService(requireContext()).getPromotions()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (!data.isNullOrEmpty()) {
                        Log.d("DashboardFragment", "✅ Загружено ${data.size} реальных акций.")

                        promotionsList.clear()
                        promotionsList.addAll(data)

                        // Показываем карусель только если есть данные
                        promotionsViewPager.visibility = View.VISIBLE
                        promotionsIndicator.visibility = View.VISIBLE

                        promotionsAdapter = PromotionsAdapter(promotionsList)
                        promotionsViewPager.adapter = promotionsAdapter

                        updateIndicator(0)
                        return@launch
                    }
                }

                // Если данных нет, убеждаемся, что карусель скрыта
                Log.d("DashboardFragment", "⚠️ Акции не найдены. Карусель скрыта.")
                promotionsViewPager.visibility = View.GONE
                promotionsIndicator.visibility = View.GONE

            } catch (e: Exception) {
                Log.e("DashboardFragment", "❌ Ошибка загрузки акций: ${e.message}")
                promotionsViewPager.visibility = View.GONE
                promotionsIndicator.visibility = View.GONE
            }
        }
    }

    private fun updateIndicator(selectedPosition: Int) {
        promotionsIndicator.removeAllViews()
        for (i in promotionsList.indices) {
            val dot = TextView(requireContext()).apply {
                text = "●"
                textSize = 10f
                setTextColor(
                    if (i == selectedPosition) requireContext().getColor(R.color.primary)
                    else requireContext().getColor(R.color.divider)
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 8, 0) }
            }
            promotionsIndicator.addView(dot)
        }
    }
}