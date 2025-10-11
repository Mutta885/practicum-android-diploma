package ru.practicum.android.diploma.presentation.industry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.ItemIndustryBinding
import ru.practicum.android.diploma.domain.models.Industry

class IndustryAdapter(
    private val onItemClick: (Industry) -> Unit
) : RecyclerView.Adapter<IndustryAdapter.IndustryViewHolder>() {

    private var industries: MutableList<Industry> = mutableListOf()
    private var selectedPosition = -1

    fun submitList(newIndustries: List<Industry>) {
        industries.clear()
        industries.addAll(newIndustries)
        selectedPosition = industries.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndustryViewHolder {
        val binding = ItemIndustryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IndustryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        holder.bind(industries[position], position)
    }

    override fun getItemCount(): Int = industries.size

    inner class IndustryViewHolder(
        private val binding: ItemIndustryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Industry, position: Int) {
            binding.industryName.text = item.name
            binding.radioButton.isChecked = position == selectedPosition

            binding.root.setOnClickListener {
                // Если клик по уже выбранной — ничего не делаем
                if (selectedPosition == position) return@setOnClickListener

                val previousSelected = selectedPosition
                selectedPosition = position

                // Обновляем состояния
                if (previousSelected != -1) {
                    industries[previousSelected].isSelected = false
                    notifyItemChanged(previousSelected)
                }
                industries[position].isSelected = true
                notifyItemChanged(position)

                onItemClick(item)
            }
        }
    }
}
