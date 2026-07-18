package com.miranda.app.ui.payments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.PaymentData
import kotlinx.coroutines.launch

class PaymentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabAddPayment: FloatingActionButton
    private lateinit var adapter: PaymentsAdapter
    private var paymentsList = mutableListOf<PaymentData>()
    private var currentBalance: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_payments)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        fabAddPayment = view.findViewById(R.id.fab_add_payment)

        // Настройка RecyclerView
        adapter = PaymentsAdapter(paymentsList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Загрузка данных
        loadPayments()

        // Обновление по свайпу
        swipeRefresh.setOnRefreshListener {
            loadPayments()
        }

        // Кнопка пополнения
        fabAddPayment.setOnClickListener {
            showTopUpDialog()
        }
    }

    private fun loadPayments() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService(requireContext()).getPayments()

                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data // Теперь это List<PaymentData>
                    if (data != null) {
                        paymentsList.clear()
                        paymentsList.addAll(data)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    showError("Не удалось загрузить историю платежей")
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showTopUpDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Сумма пополнения (₽)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(40, 20, 40, 20)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Пополнение баланса")
            .setMessage("Текущий баланс: ${String.format("%.2f", currentBalance)} ₽")
            .setView(editText)
            .setPositiveButton("Пополнить") { _, _ ->
                val amount = editText.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    topUpBalance(amount)
                } else {
                    showError("Введите корректную сумму")
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun topUpBalance(amount: Double) {
        lifecycleScope.launch {
            try {
                // ✅ Создаём объект запроса
                val request = com.miranda.app.data.models.TopUpRequest(amount = amount)

                val response = RetrofitClient.getApiService(requireContext())
                    .topUpBalance(request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    Snackbar.make(
                        requireView(),
                        "Баланс пополнен на ${String.format("%.2f", amount)} ₽",
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(requireContext().getColor(R.color.success))
                        .show()

                    loadPayments()
                } else {
                    val errorMessage = response.body()?.message ?: "Не удалось пополнить баланс"
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