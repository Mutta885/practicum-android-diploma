package ru.practicum.android.diploma.ui.fragments

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.domain.models.Industry

class FiltrationUiStateManager(private val binding: FragmentFilterBinding) {

    fun updateIndustryText(industries: List<Industry>) {
        val text = when {
            industries.isEmpty() -> getString(R.string.industry)
            industries.size == 1 -> industries[0].name
            else -> "${getString(R.string.this_select)}: ${industries.size}"
        }
        binding.industryItem2.text = text
        updateIndustryVisibility(text != getString(R.string.industry))
    }

    private fun updateIndustryVisibility(hasSelection: Boolean) {
        binding.industryItem1.isVisible = !hasSelection
        binding.groupIndustryItem2.isVisible = hasSelection
    }

    fun updateWorkplaceText(country: String?, region: String?) {
        val workplaceText = buildWorkplaceText(country, region)
        binding.workplaceItem2.text = workplaceText
        updateWorkplaceVisibility(workplaceText != getString(R.string.area_job))
    }

    private fun buildWorkplaceText(country: String?, region: String?): String {
        return when {
            country != null && region != null -> "$country, $region"
            country != null -> country
            region != null -> region
            else -> getString(R.string.area_job)
        }
    }

    private fun updateWorkplaceVisibility(hasSelection: Boolean) {
        binding.workplaceItem1.isVisible = !hasSelection
        binding.groupWorkplaceItem2.isVisible = hasSelection
    }

    fun updateActionButtonsVisibility(isActive: Boolean) {
        binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
        binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
    }

    fun updateClearSalaryButtonVisibility(salaryText: String) {
        binding.clearSalaryButton.visibility = if (salaryText.isNotEmpty()) View.VISIBLE else View.GONE
        Log.d(TAG, "Salary clear button visibility: ${binding.clearSalaryButton.visibility}")
    }

    fun updateSalaryInput(currentText: String?, newSalary: String?) {
        if (newSalary != null && currentText != newSalary) {
            binding.salaryInput.setText(newSalary)
        }
    }

    fun updateCheckboxState(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener(null)
        binding.hideWithoutSalaryCheckbox.isChecked = isChecked
        binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, checked ->
            onCheckedChange(checked)
        }
    }

    private fun getString(resId: Int): String {
        return binding.root.context.getString(resId)
    }

    companion object {
        private const val TAG = "FiltrationUiStateManager"
    }
}
