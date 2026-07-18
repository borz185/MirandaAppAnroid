package com.miranda.app.ui.tariffs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.TariffData
import kotlinx.coroutines.launch

class TariffsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: TariffsAdapter
    private var tariffsList = mutableListOf<TariffData>()
    private var currentTariffId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tariffs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_tariffs)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)

        // Настройка RecyclerView
        adapter = TariffsAdapter(tariffsList) { tariff ->
            showConfirmDialog(tariff)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Загрузка данных
        loadTariffs()

        // Обновление по свайпу
        swipeRefresh.setOnRefreshListener {
            loadTariffs()
        }
    }

    private fun loadTariffs() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                // 1. Получаем список всех тарифов
                val response = RetrofitClient.getApiService(requireContext()).getTariffs()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        tariffsList.clear()
                        tariffsList.addAll(data)
                        adapter.notifyDataSetChanged()

                        // 2. Получаем текущий тариф пользователя, чтобы подсветить его
                        loadCurrentTariff()
                    }
                } else {
                    showError("Не удалось загрузить тарифы")
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun loadCurrentTariff() {
        lifecycleScope.launch {
            try {
                // ✅ ИСПРАВЛЕНО: вызываем getDashboard(), а не getTariffs()
                val response = RetrofitClient.getApiService(requireContext()).getDashboard()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val dashboardData = response.body()?.data
                    currentTariffId = dashboardData?.current_tariff?.id
                    adapter.updateCurrentTariff(currentTariffId)
                }
            } catch (e: Exception) {
                // Игнорируем ошибку, текущий тариф просто не подсветится
            }
        }
    }

    private fun showConfirmDialog(tariff: TariffData) {
        if (tariff.id == currentTariffId) {
            Snackbar.make(
                requireView(),
                "Этот тариф уже подключен",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Подключить тариф")
            .setMessage("Вы уверены, что хотите подключить тариф \"${tariff.name}\" за ${tariff.price} ₽/мес?")
            .setPositiveButton("Подключить") { _, _ ->
                connectTariff(tariff)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun connectTariff(tariff: TariffData) {
        lifecycleScope.launch {
            try {
                // ✅ ИСПРАВЛЕНО: вызываем selectTariff(tariff.id), а не getTariffs()
                val response = RetrofitClient.getApiService(requireContext()).selectTariff(tariff.id)

                if (response.isSuccessful && response.body()?.status == "success") {
                    Snackbar.make(
                        requireView(),
                        "Тариф \"${tariff.name}\" успешно подключен!",
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(requireContext().getColor(R.color.success))
                        .show()

                    // Обновляем список и баланс
                    loadTariffs()
                } else {
                    val errorMessage = response.body()?.message ?: "Не удалось подключить тариф"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error))
            .show()
    }
}