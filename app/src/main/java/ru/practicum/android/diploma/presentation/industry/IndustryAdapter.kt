package ru.practicum.android.diploma.presentation.industry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.ItemIndustryBinding
import ru.practicum.android.diploma.domain.models.Industry

class IndustryAdapter(
    private val onSelectionChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<IndustryAdapter.IndustryViewHolder>() {

    // Внутренний класс для хранения состояния выбора
    data class Item(
        val industry: Industry,
        var isSelectedInternal: Boolean = false
    )

    private val originalList = mutableListOf<Industry>()
    private val items = mutableListOf<Item>()
    private var selectedPosition: Int = -1

    fun submitList(newIndustries: List<Industry>) {
        originalList.clear()
        originalList.addAll(newIndustries)
        items.clear()
        items.addAll(newIndustries.map { Item(it, false) })
        selectedPosition = items.indexOfFirst { it.isSelectedInternal }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun getSelectedIndustry(): Industry? {
        return items.getOrNull(selectedPosition)?.industry
    }

    fun getCurrentList(): List<Industry> {
        return items.map { it.industry }
    }

    fun filter(query: String) {
        val filtered = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { it.name.contains(query, ignoreCase = true) }
        }

        // Сохраняем состояние выбора для совпадающих элементов
        items.clear()
        items.addAll(filtered.map { original ->
            val wasSelected = items.find { it.industry.id == original.id }?.isSelectedInternal ?: false
            Item(original, wasSelected)
        })
        selectedPosition = items.indexOfFirst { it.isSelectedInternal }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
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
                if (selectedPosition != position) {
                    val previousSelected = selectedPosition
                    selectedPosition = position

                    if (previousSelected != -1) {
                        items[previousSelected].isSelectedInternal = false
                        notifyItemChanged(previousSelected)
                    }

                    items[position].isSelectedInternal = true
                    notifyItemChanged(position)

                    onSelectionChanged?.invoke()
                }
            }
        }
    }
}
