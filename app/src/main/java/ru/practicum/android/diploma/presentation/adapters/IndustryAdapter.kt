package ru.practicum.android.diploma.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.ItemIndustryBinding
import ru.practicum.android.diploma.domain.models.Industry

class IndustryAdapter(
    private val onSelectionChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<IndustryAdapter.IndustryViewHolder>() {

    // Элементы адаптера полностью иммутабельны
    data class Item(
        val industry: Industry,
        val isSelected: Boolean = false
    )

    private val originalList = mutableListOf<Industry>()
    private var items = listOf<Item>()
    private var selectedId: String? = null // Хранение ID выбранного элемента
    private var selectedPosition: Int = -1

    fun submitList(newIndustries: List<Industry>) {
        originalList.clear()
        originalList.addAll(newIndustries)
        items = originalList.map { industry ->
            Item(industry, industry.id == selectedId)
        }
        selectedPosition = items.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun getSelectedIndustry(): Industry? {
        return items.find { it.industry.id == selectedId }?.industry
    }

    fun getCurrentList(): List<Industry> {
        return items.map { it.industry }
    }

    fun filter(query: String): Boolean {
        val filtered = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { it.name.contains(query, ignoreCase = true) }
        }

        items = filtered.map { industry ->
            Item(industry, industry.id == selectedId)
        }
        selectedPosition = items.indexOfFirst { it.isSelected }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
        return if (items.isEmpty()) {
            false
        } else {
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndustryViewHolder {
        val binding = ItemIndustryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IndustryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IndustryViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class IndustryViewHolder(
        private val binding: ItemIndustryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, position: Int) {
            binding.industryName.text = item.industry.name
            binding.radioButton.isChecked = position == selectedPosition

            binding.root.setOnClickListener {
                val previousId = selectedId
                selectedId = item.industry.id

                if (previousId != null) {
                    val previousIndex = items.indexOfFirst { it.industry.id == previousId }
                    if (previousIndex != -1) {
                        notifyItemChanged(previousIndex)
                    }
                }

                selectedPosition = position
                notifyItemChanged(position)
                onSelectionChanged?.invoke()
            }
        }
    }
}
