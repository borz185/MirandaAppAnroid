package com.miranda.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.ui.auth.LoginActivity
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var avatarText: TextView
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var accountText: TextView

    private lateinit var tariffCard: MaterialCardView
    private lateinit var tariffNameText: TextView
    private lateinit var tariffSpeedText: TextView
    private lateinit var tariffPriceText: TextView

    private lateinit var servicesRecycler: RecyclerView
    private lateinit var noServicesText: TextView
    private lateinit var servicesAdapter: ProfileServicesAdapter

    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        // Инициализация View
        swipeRefresh = view.findViewById(R.id.swipe_refresh_profile)
        avatarText = view.findViewById(R.id.profile_avatar)
        nameText = view.findViewById(R.id.profile_name)
        emailText = view.findViewById(R.id.profile_email)
        accountText = view.findViewById(R.id.profile_account)

        tariffCard = view.findViewById(R.id.profile_tariff_card)
        tariffNameText = view.findViewById(R.id.profile_tariff_name)
        tariffSpeedText = view.findViewById(R.id.profile_tariff_speed)
        tariffPriceText = view.findViewById(R.id.profile_tariff_price)

        servicesRecycler = view.findViewById(R.id.profile_services_recycler)
        noServicesText = view.findViewById(R.id.no_services_text)
        btnLogout = view.findViewById(R.id.btn_logout)

        // Настройка RecyclerView для услуг
        servicesAdapter = ProfileServicesAdapter(emptyList())
        servicesRecycler.layoutManager = LinearLayoutManager(requireContext())
        servicesRecycler.adapter = servicesAdapter

        // Загрузка данных
        loadAllData()

        // Свайп для обновления
        swipeRefresh.setOnRefreshListener {
            loadAllData()
        }

        // Выход
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadAllData() {
        swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                // 1. Загружаем профиль
                val profileResponse = RetrofitClient.getApiService(requireContext()).getProfile()
                if (profileResponse.isSuccessful && profileResponse.body()?.status == "success") {
                    val user = profileResponse.body()?.data
                    if (user != null) {
                        nameText.text = user.full_name
                        emailText.text = user.email
                        accountText.text = "Л/С: ${user.account_number}"

                        // Первая буква имени для аватара
                        val firstLetter = user.full_name.firstOrNull()?.uppercase() ?: "U"
                        avatarText.text = firstLetter
                    }
                }

                // 2. Загружаем тариф и услуги (через Dashboard и Services)
                val dashboardResponse = RetrofitClient.getApiService(requireContext()).getDashboard()
                if (dashboardResponse.isSuccessful && dashboardResponse.body()?.status == "success") {
                    val dashboard = dashboardResponse.body()?.data
                    if (dashboard?.current_tariff != null) {
                        tariffNameText.text = dashboard.current_tariff.name
                        tariffSpeedText.text = "${dashboard.current_tariff.speed} Мбит/с"
                        tariffPriceText.text = "${dashboard.current_tariff.price.toInt()} ₽/мес"
                        tariffCard.visibility = View.VISIBLE
                    } else {
                        tariffNameText.text = "Тариф не выбран"
                        tariffSpeedText.text = "0 Мбит/с"
                        tariffPriceText.text = "0 ₽/мес"
                    }
                }

                // 3. Загружаем услуги и фильтруем только включенные
                val servicesResponse = RetrofitClient.getApiService(requireContext()).getServices()
                if (servicesResponse.isSuccessful && servicesResponse.body()?.status == "success") {
                    val allServices = servicesResponse.body()?.data ?: emptyList()
                    val enabledServices = allServices.filter { it.is_enabled == true }

                    if (enabledServices.isNotEmpty()) {
                        servicesAdapter.updateData(enabledServices)
                        servicesRecycler.visibility = View.VISIBLE
                        noServicesText.visibility = View.GONE
                    } else {
                        servicesRecycler.visibility = View.GONE
                        noServicesText.visibility = View.VISIBLE
                    }
                }

            } catch (e: Exception) {
                Snackbar.make(requireView(), "Ошибка загрузки: ${e.message}", Snackbar.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                RetrofitClient.getApiService(requireContext()).logout()
            } catch (e: Exception) {
                // Игнорируем ошибки сети при выходе
            } finally {
                tokenManager.clearToken()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}