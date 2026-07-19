package com.miranda.app.ui.topup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.miranda.app.R
import com.miranda.app.data.api.RetrofitClient
import com.miranda.app.data.models.TopUpRequest
import com.miranda.app.utils.TokenManager
import kotlinx.coroutines.launch

class TopUpFragment : Fragment() {

    private lateinit var tokenManager: TokenManager
    private lateinit var amountInput: EditText
    private lateinit var cardNumberInput: EditText
    private lateinit var cardHolderInput: EditText
    private lateinit var expiryDateInput: EditText
    private lateinit var cvvInput: EditText
    private lateinit var payButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_top_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())

        // Инициализация полей
        amountInput = view.findViewById(R.id.amount_input)
        cardNumberInput = view.findViewById(R.id.card_number_input)
        cardHolderInput = view.findViewById(R.id.card_holder_input)
        expiryDateInput = view.findViewById(R.id.expiry_date_input)
        cvvInput = view.findViewById(R.id.cvv_input)
        payButton = view.findViewById(R.id.btn_pay)
        val btnReturnBack = view.findViewById<Button>(R.id.btn_return_back)

        // Применяем маски к полям
        setupMasks()

        // Кнопка "Вернуться назад"
        btnReturnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Обработка клика на оплату
        payButton.setOnClickListener {
            processPayment()
        }
    }

    private fun setupMasks() {
        // Маска для номера карты: 0000 0000 0000 0000
        cardNumberInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true
                val cleanString = s.toString().replace("\\D".toRegex(), "")
                val formatted = buildString {
                    for (i in cleanString.indices) {
                        if (i > 0 && i % 4 == 0) append(" ")
                        append(cleanString[i])
                    }
                }
                cardNumberInput.setText(formatted)
                cardNumberInput.setSelection(formatted.length)
                isFormatting = false
            }
        })

        // Маска для срока действия: ММ/ГГ
        expiryDateInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true
                val cleanString = s.toString().replace("\\D".toRegex(), "")
                val formatted = if (cleanString.length >= 2) {
                    "${cleanString.substring(0, 2)}/${cleanString.substring(2, cleanString.length)}"
                } else {
                    cleanString
                }
                expiryDateInput.setText(formatted)
                expiryDateInput.setSelection(formatted.length)
                isFormatting = false
            }
        })

        // Только цифры для CVV
        cvvInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                val cleanString = s.toString().replace("\\D".toRegex(), "")
                if (cleanString != s.toString()) {
                    cvvInput.setText(cleanString)
                    cvvInput.setSelection(cleanString.length)
                }
            }
        })
    }

    private fun processPayment() {
        val amount = amountInput.text.toString().trim().toDoubleOrNull()
        val cardNumber = cardNumberInput.text.toString().trim().replace(" ", "")
        val cardHolder = cardHolderInput.text.toString().trim()
        val expiryDate = expiryDateInput.text.toString().trim()
        val cvv = cvvInput.text.toString().trim()

        // Валидация
        if (amount == null || amount <= 0) {
            showError("Введите корректную сумму")
            return
        }

        if (cardNumber.length != 16) {
            showError("Номер карты должен содержать 16 цифр")
            return
        }

        if (cardHolder.isEmpty()) {
            showError("Укажите имя владельца")
            return
        }

        if (expiryDate.length != 5 || !expiryDate.contains("/")) {
            showError("Введите срок в формате ММ/ГГ")
            return
        }

        if (cvv.length != 3) {
            showError("CVV должен содержать 3 цифры")
            return
        }

        // Реальный запрос к API
        payButton.isEnabled = false
        payButton.text = "Обработка..."

        lifecycleScope.launch {
            try {
                val request = TopUpRequest(amount)
                val response = RetrofitClient.getApiService(requireContext()).topUpBalance(request)

                if (response.isSuccessful && response.body()?.status == "success") {
                    // Переход на экран успеха
                    val intent = Intent(requireContext(), TopUpSuccessActivity::class.java)
                    startActivity(intent)
                } else {
                    showError("Ошибка оплаты: ${response.body()?.message}")
                    payButton.isEnabled = true
                    payButton.text = "Оплатить"
                }
            } catch (e: Exception) {
                showError("Ошибка соединения: ${e.message}")
                payButton.isEnabled = true
                payButton.text = "Оплатить"
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
}