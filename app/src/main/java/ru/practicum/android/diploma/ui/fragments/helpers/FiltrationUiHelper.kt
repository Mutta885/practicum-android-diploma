package ru.practicum.android.diploma.ui.fragments.helpers

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.presentation.vmodels.FiltrationViewModel
import ru.practicum.android.diploma.presentation.vmodels.SearchViewModel
import ru.practicum.android.diploma.ui.fragments.FiltrationFragment

class FiltrationUiHelper(
    private val fragment: FiltrationFragment,
    private val binding: FragmentFilterBinding,
    private val viewModel: FiltrationViewModel,
    private val searchViewModel: SearchViewModel
) {

    fun setupViews() {
        setupReturnButton()
        setupSalaryInput()
        setupIndustryNavigation()
        setupWorkplaceNavigation()
        setupSalaryCheckbox()
        setupActionButtons()
        setupClearButtons()
    }

    fun observeViewModel() {
        with(fragment.viewLifecycleOwner) {
            observeFilterState(this)
            observeIndustries(this)
            observeWorkplace(this)
            observeSalaryState(this)
            observeCheckboxState(this)
        }
    }

    // ----------------------------- UI SETUP -----------------------------

    private fun setupReturnButton() {
        binding.returnButton.setOnClickListener {
            Log.d(TAG, "Return button clicked - restore initial filters WITHOUT saving")
            viewModel.restoreInitialState()
            fragment.navigateBack()
        }
    }

    private fun setupSalaryInput() {
        with(binding) {
            salaryInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    viewModel.onSalaryChanged(text)
                    updateClearSalaryButtonVisibility(text)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            })

            salaryInput.setOnFocusChangeListener { _, hasFocus ->
                val colorRes = if (hasFocus) R.color.blue else R.color.grey
                salaryLabel.setTextColor(ContextCompat.getColor(fragment.requireContext(), colorRes))
            }

            clearSalaryButton.setOnClickListener {
                salaryInput.text?.clear()
                viewModel.onSalaryChanged("")
            }
        }
    }

    private fun setupIndustryNavigation() {
        with(binding) {
            industryItem1.setOnClickListener { navigateToIndustry() }
            groupIndustryItem2.setOnClickListener { navigateToIndustry() }
            closeIndustry.setOnClickListener { hideIndustrySelection() }
        }
    }

    private fun navigateToIndustry() {
        fragment.findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
    }

    private fun hideIndustrySelection() {
        binding.groupIndustryItem2.isVisible = false
        binding.industryItem1.isVisible = true
    }

    private fun setupWorkplaceNavigation() {
        with(binding) {
            workplaceItem1.setOnClickListener { navigateToWorkplace() }
            groupWorkplaceItem2.setOnClickListener { navigateToWorkplace() }
            closeWorkplace.setOnClickListener { hideWorkplaceSelection() }
        }
    }

    private fun navigateToWorkplace() {
        fragment.findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
    }

    private fun hideWorkplaceSelection() {
        binding.groupWorkplaceItem2.isVisible = false
        binding.workplaceItem1.isVisible = true
    }

    private fun setupSalaryCheckbox() {
        with(binding) {
            hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onHideWithoutSalaryChanged(isChecked)
            }
            hideSalaryContainer.setOnClickListener {
                hideWithoutSalaryCheckbox.isChecked = !hideWithoutSalaryCheckbox.isChecked
            }
        }
    }

    private fun setupActionButtons() {
        with(binding) {
            applyButton.setOnClickListener { applyFilters() }
            resetButton.setOnClickListener { resetFilters() }
        }
    }

    private fun setupClearButtons() {
        binding.closeIndustry.setOnClickListener { viewModel.clearIndustries() }
        binding.closeWorkplace.setOnClickListener { viewModel.clearWorkplace() }
    }

    // ----------------------------- ACTIONS -----------------------------

    private fun applyFilters() {
        val filters = viewModel.getCurrentFiltersForApply()
        viewModel.saveFiltersToStorage()
        viewModel.setFiltersJustApplied(true)
        searchViewModel.applyFiltersWithSearch(filters)
        fragment.showToast("Фильтры применены")
        fragment.navigateBack()
    }

    private fun resetFilters() {
        viewModel.resetFilters()
        binding.salaryInput.text?.clear()
        viewModel.saveFiltersToStorage()
        viewModel.setFiltersJustApplied(true)
        searchViewModel.applyFiltersWithSearch(FiltrationViewModel.Filters())
        fragment.showToast("Фильтры сброшены")
        fragment.navigateBack()
    }

    // ----------------------------- OBSERVERS -----------------------------

    private fun observeFilterState(owner: LifecycleOwner) {
        viewModel.isAnyFilterActive.observe(owner) { isActive ->
            binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
            binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
        }
    }

    private fun observeIndustries(owner: LifecycleOwner) {
        viewModel.selectedIndustries.observe(owner) { industries ->
            updateIndustryText(industries)
        }
    }

    private fun observeWorkplace(owner: LifecycleOwner) {
        listOf(viewModel.selectedCountry, viewModel.selectedRegion).forEach { liveData ->
            liveData.observe(owner) { updateWorkplaceText() }
        }
    }

    private fun observeSalaryState(owner: LifecycleOwner) {
        viewModel.salary.observe(owner) { salary ->
            if (salary != null && binding.salaryInput.text?.toString() != salary)
                binding.salaryInput.setText(salary)
            updateClearSalaryButtonVisibility(salary ?: "")
        }
    }

    private fun observeCheckboxState(owner: LifecycleOwner) {
        viewModel.hideWithoutSalary.observe(owner) { isChecked ->
            binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener(null)
            binding.hideWithoutSalaryCheckbox.isChecked = isChecked
            binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, checked ->
                viewModel.onHideWithoutSalaryChanged(checked)
            }
        }
    }

    // ----------------------------- UI UPDATES -----------------------------

    private fun updateIndustryText(industries: List<Industry>) {
        val text = when {
            industries.isEmpty() -> fragment.getString(R.string.industry)
            industries.size == 1 -> industries[0].name
            else -> "${fragment.getString(R.string.this_select)}: ${industries.size}"
        }
        binding.industryItem2.text = text
        binding.industryItem1.isVisible = industries.isEmpty()
        binding.groupIndustryItem2.isVisible = industries.isNotEmpty()
    }

    private fun updateWorkplaceText() {
        val country = viewModel.selectedCountry.value
        val region = viewModel.selectedRegion.value
        val workplaceText = when {
            country != null && region != null -> "$country, $region"
            country != null -> country
            region != null -> region
            else -> fragment.getString(R.string.area_job)
        }
        binding.workplaceItem2.text = workplaceText
        binding.workplaceItem1.isVisible = workplaceText == fragment.getString(R.string.area_job)
        binding.groupWorkplaceItem2.isVisible = workplaceText != fragment.getString(R.string.area_job)
    }

    private fun updateClearSalaryButtonVisibility(salaryText: String) {
        binding.clearSalaryButton.visibility =
            if (salaryText.isNotEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "FiltrationUiHelper"
    }
}
