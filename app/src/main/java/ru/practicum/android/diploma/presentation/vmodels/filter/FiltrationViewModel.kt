package ru.practicum.android.diploma.presentation.vmodels.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.api.StorageManager
import ru.practicum.android.diploma.domain.models.FilterParameters
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.ui.search.SearchViewModel

class FiltrationViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

    private val _salary = MutableLiveData<String?>()
    val salary: LiveData<String?> = _salary

    private val _hideWithoutSalary = MutableLiveData<Boolean>()
    val hideWithoutSalary: LiveData<Boolean> = _hideWithoutSalary

    private val _selectedIndustries = MutableLiveData<List<Industry>>()
    val selectedIndustries: LiveData<List<Industry>> = _selectedIndustries

    private val _isAnyFilterActive = MutableLiveData<Boolean>()
    val isAnyFilterActive: LiveData<Boolean> = _isAnyFilterActive

    private val _isSalaryInputNotEmpty = MutableLiveData<Boolean>()
    val isSalaryInputNotEmpty: LiveData<Boolean> = _isSalaryInputNotEmpty

    private companion object {
        const val FILTERS_DELAY_MS = 200L
    }

    init {
        loadSavedFilters()
    }

    private fun loadSavedFilters() {
        viewModelScope.launch {
            storageManager.getFilterSetting().collect { savedFilter ->
                if (savedFilter != null) {
                    _salary.value = savedFilter.salary.takeIf { it.isNotEmpty() }
                    _hideWithoutSalary.value = savedFilter.onlyWithSalary
                    _selectedIndustries.value = savedFilter.industries

                    println(
                        "DEBUG: Filters loaded from storage: " +
                            "salary=${_salary.value}, " +
                            "hideWithoutSalary=${_hideWithoutSalary.value}, " +
                            "industries=${_selectedIndustries.value?.size}"
                    )
                } else {
                    _salary.value = null
                    _hideWithoutSalary.value = false
                    _selectedIndustries.value = emptyList()
                    println("DEBUG: No saved filters found")
                }
                updateFilterState()
                updateSalaryInputState()
            }
        }
    }

    fun onSalaryChanged(value: String) {
        val filteredValue = value.filter { it.isDigit() }
        _salary.value = filteredValue.ifEmpty { null }
        updateFilterState()
        updateSalaryInputState()
        saveFilters()
    }

    fun onHideWithoutSalaryChanged(value: Boolean) {
        _hideWithoutSalary.value = value
        updateFilterState()
        saveFilters()
    }

    fun onIndustriesSelected(industries: List<Industry>) {
        _selectedIndustries.value = industries
        updateFilterState()
        saveFilters()
    }

    fun resetFilters() {
        _salary.value = null
        _hideWithoutSalary.value = false
        _selectedIndustries.value = emptyList()
        updateFilterState()
        updateSalaryInputState()
        saveFilters()
    }

    private fun saveFilters() {
        viewModelScope.launch {
            val filter = FilterParameters(
                onlyWithSalary = _hideWithoutSalary.value ?: false,
                salary = _salary.value ?: "",
                industries = _selectedIndustries.value ?: emptyList()
            )
            storageManager.setFilterSetting(filter)
        }
    }

    private fun updateFilterState() {
        val isActive = !_salary.value.isNullOrEmpty() ||
            _hideWithoutSalary.value == true ||
            _selectedIndustries.value?.isNotEmpty() == true

        _isAnyFilterActive.value = isActive
    }

    private fun updateSalaryInputState() {
        _isSalaryInputNotEmpty.value = !_salary.value.isNullOrEmpty()
    }

    fun getCurrentFilters(): Filters {
        return Filters(
            salary = _salary.value,
            hideWithoutSalary = _hideWithoutSalary.value ?: false,
            industries = _selectedIndustries.value ?: emptyList()
        )
    }

    fun applySavedFiltersToSearch(searchViewModel: SearchViewModel) {
        viewModelScope.launch {
            delay(FILTERS_DELAY_MS)

            val currentFilters = getCurrentFilters()
            val hasActiveFilters = currentFilters.salary != null ||
                currentFilters.hideWithoutSalary ||
                currentFilters.industries.isNotEmpty()

            if (hasActiveFilters) {
                println(
                    "DEBUG: FiltrationViewModel applying saved filters to search: " +
                        "$currentFilters"
                )
                searchViewModel.setFilters(currentFilters)
            } else {
                println("DEBUG: FiltrationViewModel no active filters to apply")
            }
        }
    }

    data class Filters(
        val salary: String? = null,
        val hideWithoutSalary: Boolean = false,
        val industries: List<Industry> = emptyList()
    )
}
