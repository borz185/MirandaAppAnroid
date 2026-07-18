package com.miranda.app.ui.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miranda.app.R
import com.miranda.app.data.models.ServiceData

class ServicesAdapter(
    private var servicesList: MutableList<ServiceData>,
    private val onToggleClick: (ServiceData, Boolean) -> Unit
) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.service_card_view)
        val nameText: TextView = itemView.findViewById(R.id.service_name)
        val priceText: TextView = itemView.findViewById(R.id.service_price)
        val descriptionText: TextView = itemView.findViewById(R.id.service_description)
        val switch: SwitchCompat = itemView.findViewById(R.id.service_switch)  // ← Изменили на SwitchCompat
        val statusText: TextView = itemView.findViewById(R.id.service_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = servicesList[holder.adapterPosition]

        holder.nameText.text = service.name

        val price = service.monthly_fee ?: 0.0
        holder.priceText.text = "${price.toInt()} ₽/мес"

        holder.descriptionText.text = service.description ?: "Дополнительная услуга"

        // Устанавливаем переключатель без триггера изменения
        holder.switch.setOnCheckedChangeListener(null)

        val isEnabled = service.is_enabled ?: false
        holder.switch.isChecked = isEnabled

        // Устанавливаем слушатель после установки состояния
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            onToggleClick(service, isChecked)
        }

        // Обновляем статус и цвет
        if (isEnabled) {
            holder.statusText.text = "Подключено"
            holder.statusText.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.success)
            )
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.success_light)
            )
        } else {
            holder.statusText.text = "Не подключено"
            holder.statusText.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.text_secondary)
            )
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.card_background)
            )
        }

        val isBonus = service.is_bonus ?: false
        if (isBonus) {
            holder.nameText.setCompoundDrawablesWithIntrinsicBounds(
                android.R.drawable.btn_star, 0, 0, 0
            )
        } else {
            holder.nameText.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, 0, 0
            )
        }
    }

    override fun getItemCount(): Int = servicesList.size
}