package ru.practicum.android.diploma.ui.root

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.FilterArea

class RegionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val regionText: TextView = itemView.findViewById(R.id.region_name)

    fun bind(region: FilterArea, regionListener: RegionAdapter.RegionListener) {
        regionText.text = region.name
        itemView.setOnClickListener {
            regionListener.onRegionClick(region)
        }
    }
}
