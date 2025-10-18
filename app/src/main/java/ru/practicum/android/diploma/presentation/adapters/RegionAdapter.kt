package ru.practicum.android.diploma.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.presentation.holders.RegionViewHolder

class RegionAdapter(
    private var regions: List<FilterArea>,
    private val regionListener: RegionListener
) : RecyclerView.Adapter<RegionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_region, parent, false)
        return RegionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return regions.size
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        holder.bind(regions[position], regionListener)
    }

    fun updateRegions(newRegions: List<FilterArea>) {
        regions = newRegions
        notifyDataSetChanged()
    }

    interface RegionListener {
        fun onRegionClick(region: FilterArea)
    }
}
