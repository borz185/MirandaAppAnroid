package com.miranda.app.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.miranda.app.data.models.PromotionData

class PromotionsAdapter(
    private var promotionsList: MutableList<PromotionData>
) : RecyclerView.Adapter<PromotionsAdapter.PromotionViewHolder>() {

    class PromotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(com.miranda.app.R.id.promotion_title)
        val descriptionText: TextView = itemView.findViewById(com.miranda.app.R.id.promotion_description)
        val discountText: TextView = itemView.findViewById(com.miranda.app.R.id.promotion_discount)
        val imageView: ImageView = itemView.findViewById(com.miranda.app.R.id.promotion_image)
        val cardView: MaterialCardView = itemView as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.miranda.app.R.layout.item_promotion, parent, false)
        return PromotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        val promotion = promotionsList[holder.adapterPosition]

        holder.titleText.text = promotion.title
        holder.descriptionText.text = promotion.description

        // Показываем скидку, если есть
        if (promotion.discount != null && promotion.discount > 0) {
            holder.discountText.text = "-${promotion.discount}%"
            holder.discountText.visibility = View.VISIBLE
        } else {
            holder.discountText.visibility = View.GONE
        }

        // Устанавливаем цвет фона
        val backgroundColor = if (!promotion.color.isNullOrEmpty()) {
            try {
                Color.parseColor(promotion.color)
            } catch (e: Exception) {
                // Цвета по умолчанию для разных акций
                val defaultColors = listOf(
                    Color.parseColor("#FF6600"),  // Оранжевый
                    Color.parseColor("#7B2FBE"),  // Фиолетовый
                    Color.parseColor("#4CAF50"),  // Зелёный
                    Color.parseColor("#2196F3"),  // Синий
                    Color.parseColor("#FF5722")   // Красный
                )
                defaultColors[position % defaultColors.size]
            }
        } else {
            // Цвета по умолчанию
            val defaultColors = listOf(
                Color.parseColor("#FF6600"),
                Color.parseColor("#7B2FBE"),
                Color.parseColor("#4CAF50")
            )
            defaultColors[position % defaultColors.size]
        }

        holder.cardView.setCardBackgroundColor(backgroundColor)
    }

    override fun getItemCount(): Int = promotionsList.size

    fun updateData(newData: List<PromotionData>) {
        promotionsList.clear()
        promotionsList.addAll(newData)
        notifyDataSetChanged()
    }
}