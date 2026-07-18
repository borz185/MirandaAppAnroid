package com.miranda.app.ui.tariffs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miranda.app.R
import com.miranda.app.data.models.TariffData

class TariffsAdapter(
    private var tariffsList: MutableList<TariffData>,
    private val onConnectClick: (TariffData) -> Unit
) : RecyclerView.Adapter<TariffsAdapter.TariffViewHolder>() {

    private var currentTariffId: Int? = null

    class TariffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.tariff_card_view)
        val nameText: TextView = itemView.findViewById(R.id.tariff_name)
        val speedText: TextView = itemView.findViewById(R.id.tariff_speed)
        val priceText: TextView = itemView.findViewById(R.id.tariff_price)
        val descriptionText: TextView = itemView.findViewById(R.id.tariff_description)
        val connectButton: Button = itemView.findViewById(R.id.btn_connect)
        val currentBadge: TextView = itemView.findViewById(R.id.current_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TariffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tariff, parent, false)
        return TariffViewHolder(view)
    }

    override fun onBindViewHolder(holder: TariffViewHolder, position: Int) {
        val tariff = tariffsList[holder.adapterPosition]

        holder.nameText.text = tariff.name
        holder.speedText.text = "${tariff.speed} Мбит/с"
        holder.priceText.text = "${tariff.price.toInt()} ₽/мес"
        holder.descriptionText.text = tariff.description ?: "Безлимитный интернет"

        // Проверяем, является ли этот тариф текущим
        if (tariff.id == currentTariffId) {
            holder.connectButton.text = "Подключен"
            holder.connectButton.isEnabled = false
            holder.currentBadge.visibility = View.VISIBLE
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.success_light)
            )
        } else {
            holder.connectButton.text = "Подключить"
            holder.connectButton.isEnabled = true
            holder.currentBadge.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.card_background)
            )
        }

        holder.connectButton.setOnClickListener {
            onConnectClick(tariff)
        }
    }

    override fun getItemCount(): Int = tariffsList.size

    fun updateCurrentTariff(tariffId: Int?) {
        currentTariffId = tariffId
        notifyDataSetChanged()
    }
}