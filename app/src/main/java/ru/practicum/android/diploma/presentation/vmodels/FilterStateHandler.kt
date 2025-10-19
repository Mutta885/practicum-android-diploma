package ru.practicum.android.diploma.presentation.vmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.practicum.android.diploma.domain.models.Industry

class FilterStateHandler {

    // LiveData для UI - отображают текущие изменения
    private val _salary = MutableLiveData<String?>()
    val salary: LiveData<String?> = _salary

    private val _hideWithoutSalary = MutableLiveData<Boolean>()
    val hideWithoutSalary: LiveData<Boolean> = _hideWithoutSalary

    private val _selectedIndustries = MutableLiveData<List<Industry>>()
    val selectedIndustries: LiveData<List<Industry>> = _selectedIndustries

    private val _selectedCountry = MutableLiveData<String?>()
    val selectedCountry: LiveData<String?> = _selectedCountry

    private val _selectedCountryId = MutableLiveData<String?>()
    val selectedCountryId: LiveData<String?> = _selectedCountryId

    private val _selectedRegion = MutableLiveData<String?>()
    val selectedRegion: LiveData<String?> = _selectedRegion

    private val _selectedRegionId = MutableLiveData<String?>()
    val selectedRegionId: LiveData<String?> = _selectedRegionId

    private val _isAnyFilterActive = MutableLiveData<Boolean>()
    val isAnyFilterActive: LiveData<Boolean> = _isAnyFilterActive

    private val _isSalaryInputNotEmpty = MutableLiveData<Boolean>()
    val isSalaryInputNotEmpty: LiveData<Boolean> = _isSalaryInputNotEmpty

    private val _filtersJustApplied = MutableLiveData<Boolean>(false)
    val filtersJustApplied: LiveData<Boolean> = _filtersJustApplied

    // Состояние для восстановления при отмене
    private var initialFilters: FiltrationViewModel.Filters? = null

    // Примененные фильтры
    private var _currentAppliedFilters: FiltrationViewModel.Filters = FiltrationViewModel.Filters()
    val currentAppliedFilters: FiltrationViewModel.Filters get() = _currentAppliedFilters

    fun onSalaryChanged(value: String) {
        val filteredValue = value.filter { it.isDigit() }
        _salary.value = filteredValue.ifEmpty { null }
        updateFilterState()
        updateSalaryInputState()
    }

    fun onHideWithoutSalaryChanged(value: Boolean) {
        _hideWithoutSalary.value = value
        updateFilterState()
    }

    fun onIndustriesSelected(industries: List<Industry>) {
        _selectedIndustries.value = industries
        updateFilterState()
    }

    fun clearIndustries() {
        _selectedIndustries.value = emptyList()
        updateFilterState()
    }

    fun clearWorkplace() {
        _selectedCountry.value = null
        _selectedCountryId.value = null
        _selectedRegion.value = null
        _selectedRegionId.value = null
        updateFilterState()
    }

    fun onWorkplaceSelected(
        countryName: String?,
        countryId: Int?,
        regionName: String?,
        regionId: Int?
    ) {
        _selectedCountry.value = countryName
        _selectedCountryId.value = countryId?.toString()
        _selectedRegion.value = regionName
        _selectedRegionId.value = regionId?.toString()
        updateFilterState()
    }

    fun getCurrentFiltersFromUI(): FiltrationViewModel.Filters {
        return FiltrationViewModel.Filters(
            salary = _salary.value,
            hideWithoutSalary = _hideWithoutSalary.value ?: false,
            industries = _selectedIndustries.value ?: emptyList(),
            country = _selectedCountry.value,
            countryId = _selectedCountryId.value,
            region = _selectedRegion.value,
            regionId = _selectedRegionId.value
        )
    }

    fun resetFilters() {
        _salary.value = null
        _hideWithoutSalary.value = false
        _selectedIndustries.value = emptyList()
        _selectedCountry.value = null
        _selectedCountryId.value = null
        _selectedRegion.value = null
        _selectedRegionId.value = null
        updateFilterState()
        updateSalaryInputState()
    }

    fun setFiltersJustApplied(applied: Boolean) {
        _filtersJustApplied.value = applied
        println("FilterStateHandler: setFiltersJustApplied called with: $applied")
    }

    internal fun updateFilterState() {
        val isActive = !_salary.value.isNullOrEmpty() ||
            _hideWithoutSalary.value == true ||
            _selectedIndustries.value?.isNotEmpty() == true ||
            _selectedCountry.value != null ||
            _selectedRegion.value != null

        _isAnyFilterActive.value = isActive
    }

    internal fun updateSalaryInputState() {
        _isSalaryInputNotEmpty.value = !_salary.value.isNullOrEmpty()
    }

    internal fun shouldSkipAutoApplication(): Boolean {
        val filtersJustApplied = _filtersJustApplied.value == true
        if (filtersJustApplied) {
            println("FilterStateHandler: Filters were just applied - skipping auto-application")
            _filtersJustApplied.value = false
            return true
        }
        return false
    }

    internal fun updateCurrentAppliedFilters(filters: FiltrationViewModel.Filters) {
        _currentAppliedFilters = filters
    }

    internal fun restoreFiltersFromState(filters: FiltrationViewModel.Filters) {
        _salary.value = filters.salary
        _hideWithoutSalary.value = filters.hideWithoutSalary
        _selectedIndustries.value = filters.industries
        _selectedCountry.value = filters.country
        _selectedCountryId.value = filters.countryId
        _selectedRegion.value = filters.region
        _selectedRegionId.value = filters.regionId
        updateFilterState()
        updateSalaryInputState()
    }

    fun saveInitialState() {
        initialFilters = getCurrentFiltersFromUI()
        println("FilterStateHandler: Initial filters saved: $initialFilters")
    }

    fun restoreInitialState() {
        initialFilters?.let { filters ->
            println("FilterStateHandler: Restoring initial filters: $filters")
            restoreFiltersFromState(filters)
        } ?: run {
            println("FilterStateHandler: No initial filters to restore")
        }
    }
}
