package com.miranda.app.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.miranda.app.R
import com.miranda.app.data.models.ServiceData

class ProfileServicesAdapter(
    private var servicesList: List<ServiceData>
) : RecyclerView.Adapter<ProfileServicesAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.service_name_text)
        val priceText: TextView = itemView.findViewById(R.id.service_price_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = servicesList[position]
        holder.nameText.text = service.name
        val price = service.monthly_fee ?: 0.0
        holder.priceText.text = "${price.toInt()} ₽/мес"
    }

    override fun getItemCount(): Int = servicesList.size

    fun updateData(newData: List<ServiceData>) {
        servicesList = newData
        notifyDataSetChanged()
    }
}