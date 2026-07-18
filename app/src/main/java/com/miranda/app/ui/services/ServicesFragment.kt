package com.miranda.app.ui.services

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
import com.miranda.app.data.models.ServiceData
import com.miranda.app.data.models.ToggleServiceRequest
import kotlinx.coroutines.launch

class ServicesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: ServicesAdapter
    private var servicesList = mutableListOf<ServiceData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_services)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)

        // Настройка RecyclerView
        adapter = ServicesAdapter(servicesList) { service, isEnabled ->
            toggleService(service, isEnabled)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Загрузка данных
        loadServices()

        // Обновление по свайпу
        swipeRefresh.setOnRefreshListener {
            loadServices()
        }
    }

    private fun loadServices() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getServices()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        servicesList.clear()
                        servicesList.addAll(data)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    showError("Не удалось загрузить услуги")
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun toggleService(service: ServiceData, isEnabled: Boolean) {
        val actionText = if (isEnabled) "подключить" else "отключить"

        AlertDialog.Builder(requireContext())
            .setTitle("${if (isEnabled) "Подключить" else "Отключить"} услугу")
            .setMessage("Вы уверены, что хотите ${actionText} услугу \"${service.name}\" за ${service.monthly_fee} ₽/мес?")
            .setPositiveButton("Да") { _, _ ->
                confirmToggle(service, isEnabled)
            }
            .setNegativeButton("Отмена") { _, _ ->
                // Отменяем переключение
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun confirmToggle(service: ServiceData, isEnabled: Boolean) {
        lifecycleScope.launch {
            try {
                // Создаём запрос с правильным типом
                val request = ToggleServiceRequest(is_enabled = isEnabled)

                val response = RetrofitClient.getApiService(requireContext())
                    .toggleService(service.id, request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    val message = if (isEnabled) {
                        "Услуга \"${service.name}\" подключена!"
                    } else {
                        "Услуга \"${service.name}\" отключена!"
                    }

                    Snackbar.make(
                        requireView(),
                        message,
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(
                        requireContext().getColor(
                            if (isEnabled) R.color.success else R.color.warning
                        )
                    ).show()

                    // Обновляем список
                    loadServices()
                } else {
                    val errorMessage = response.body()?.message ?: "Не удалось изменить услугу"
                    showError(errorMessage)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error))
            .show()
    }
}