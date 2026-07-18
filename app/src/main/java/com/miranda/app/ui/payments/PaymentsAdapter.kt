package com.miranda.app.ui.payments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miranda.app.R
import com.miranda.app.data.models.PaymentData
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentsAdapter(
    private var paymentsList: MutableList<PaymentData>
) : RecyclerView.Adapter<PaymentsAdapter.PaymentViewHolder>() {

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.payment_card_view)
        val descriptionText: TextView = itemView.findViewById(R.id.payment_description)
        val amountText: TextView = itemView.findViewById(R.id.payment_amount)
        val dateText: TextView = itemView.findViewById(R.id.payment_date)
        val statusText: TextView = itemView.findViewById(R.id.payment_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentsList[holder.adapterPosition]

        // Описание
        holder.descriptionText.text = payment.description

        // Форматируем сумму
        val amountText = if (payment.amount > 0) {
            "+${String.format("%.2f", payment.amount)} ₽"
        } else {
            "${String.format("%.2f", payment.amount)} ₽"
        }
        holder.amountText.text = amountText

        // Цвет суммы (зеленый для пополнений, красный для списаний)
        val amountColor = if (payment.amount > 0) {
            ContextCompat.getColor(holder.itemView.context, R.color.balance_positive)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.balance_negative)
        }
        holder.amountText.setTextColor(amountColor)

        // Форматируем дату из ISO 8601 в читаемый вид
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(payment.created_at)
            holder.dateText.text = if (date != null) {
                outputFormat.format(date)
            } else {
                payment.created_at
            }
        } catch (e: Exception) {
            holder.dateText.text = payment.created_at
        }

        // Статус (безопасная обработка null, так как в JSON его может не быть)
        val status = payment.status ?: "completed"
        holder.statusText.text = when (status) {
            "completed" -> "Выполнено"
            "pending" -> "В обработке"
            "failed" -> "Ошибка"
            else -> status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        // Цвет статуса
        val statusColor = when (status) {
            "completed" -> R.color.success
            "pending" -> R.color.warning
            "failed" -> R.color.error
            else -> R.color.text_secondary
        }
        holder.statusText.setTextColor(
            ContextCompat.getColor(holder.itemView.context, statusColor)
        )
    }

    override fun getItemCount(): Int = paymentsList.size
}