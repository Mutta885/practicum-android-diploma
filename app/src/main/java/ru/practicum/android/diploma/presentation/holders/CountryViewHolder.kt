package ru.practicum.android.diploma.presentation.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.presentation.adapters.CountryAdapter

class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val countryText: TextView = itemView.findViewById(R.id.country_name)

    fun bind(country: FilterArea, countryListener: CountryAdapter.CountryListener) {
        countryText.text = country.name
        itemView.setOnClickListener {
            countryListener.onCountryClick(country)
        }
    }
}
