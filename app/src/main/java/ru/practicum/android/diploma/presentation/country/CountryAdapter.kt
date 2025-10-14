package ru.practicum.android.diploma.presentation.country

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Country

class CountryAdapter(
    private val onItemClicked: (Country) -> Unit
) : RecyclerView.Adapter<CountryViewholder>() {

    private val countries = mutableListOf<Country>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewholder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_country, parent, false)
        return CountryViewholder(itemView, onItemClicked)
    }

    override fun onBindViewHolder(holder: CountryViewholder, position: Int) {
        holder.bind(countries[position])
    }

    override fun getItemCount(): Int = countries.size
}
