package ru.practicum.android.diploma.ui.root

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.FilterArea

class CountryAdapter(
    private var countries: List<FilterArea>,
    private val countryListener: CountryListener
) : RecyclerView.Adapter<CountryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(countries[position], countryListener)
    }

    fun updateCountries(newCountries: List<FilterArea>) {
        countries = newCountries
        notifyDataSetChanged()
    }

    interface CountryListener {
        fun onCountryClick(country: FilterArea)
    }
}
