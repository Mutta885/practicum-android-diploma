package ru.practicum.android.diploma.presentation.country

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Country

class CountryViewholder(
    itemView: View,
    private val onItemClicked: (Country) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val countryNameView: TextView = itemView.findViewById(R.id.country_name)

    fun bind(country: Country) {
        countryNameView.text = country.name
        itemView.setOnClickListener {
            onItemClicked(country)
        }
    }
}
