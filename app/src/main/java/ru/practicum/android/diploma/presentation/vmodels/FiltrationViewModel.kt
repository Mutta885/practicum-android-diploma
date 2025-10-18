package ru.practicum.android.diploma.presentation.vmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.api.StorageManager
import ru.practicum.android.diploma.domain.models.FilterParameters
import ru.practicum.android.diploma.domain.models.Industry

class FiltrationViewModel(
    private val storageManager: StorageManager
) : ViewModel() {

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

    private companion object {
        const val FILTERS_DELAY_MS = 200L
        private const val DEBUG_TAG = "FiltrationViewModel"
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
                    _selectedCountry.value = savedFilter.country
                    _selectedCountryId.value = savedFilter.countryId
                    _selectedRegion.value = savedFilter.region
                    _selectedRegionId.value = savedFilter.regionId

                    println(
                        "$DEBUG_TAG: Filters loaded from storage: " +
                            "salary=${_salary.value}, " +
                            "hideWithoutSalary=${_hideWithoutSalary.value}, " +
                            "industries=${_selectedIndustries.value?.size}, " +
                            "country=${_selectedCountry.value}, countryId=${_selectedCountryId.value}, " +
                            "region=${_selectedRegion.value}, regionId=${_selectedRegionId.value}"
                    )
                } else {
                    _salary.value = null
                    _hideWithoutSalary.value = false
                    _selectedIndustries.value = emptyList()
                    _selectedCountry.value = null
                    _selectedCountryId.value = null
                    _selectedRegion.value = null
                    _selectedRegionId.value = null
                    println("$DEBUG_TAG: No saved filters found")
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

    // Сохраняем фильтры в StorageManager (только при явном действии)
    fun saveFiltersToStorage() {
        viewModelScope.launch {
            val filter = FilterParameters(
                onlyWithSalary = _hideWithoutSalary.value ?: false,
                salary = _salary.value ?: "",
                industries = _selectedIndustries.value ?: emptyList(),
                country = _selectedCountry.value,
                countryId = _selectedCountryId.value,
                region = _selectedRegion.value,
                regionId = _selectedRegionId.value
            )
            storageManager.setFilterSetting(filter)
            println("$DEBUG_TAG: Filters saved to storage")
        }
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
        println("$DEBUG_TAG: setFiltersJustApplied called with: $applied")
    }

    private fun updateFilterState() {
        val isActive = !_salary.value.isNullOrEmpty() ||
            _hideWithoutSalary.value == true ||
            _selectedIndustries.value?.isNotEmpty() == true ||
            _selectedCountry.value != null ||
            _selectedRegion.value != null

        _isAnyFilterActive.value = isActive
    }

    private fun updateSalaryInputState() {
        _isSalaryInputNotEmpty.value = !_salary.value.isNullOrEmpty()
    }

    fun getCurrentFilters(): Filters {
        return Filters(
            salary = _salary.value,
            hideWithoutSalary = _hideWithoutSalary.value ?: false,
            industries = _selectedIndustries.value ?: emptyList(),
            country = _selectedCountry.value,
            countryId = _selectedCountryId.value,
            region = _selectedRegion.value,
            regionId = _selectedRegionId.value
        )
    }

    fun applySavedFiltersToSearch(searchViewModel: SearchViewModel) {
        viewModelScope.launch {
            delay(FILTERS_DELAY_MS)
            handleFiltersApplication(searchViewModel)
        }
    }

    private suspend fun shouldSkipAutoApplication(): Boolean {
        val filtersJustApplied = _filtersJustApplied.value == true
        if (filtersJustApplied) {
            println("$DEBUG_TAG: Filters were just applied - skipping auto-application")
            _filtersJustApplied.value = false
            return true
        }
        return false
    }

    private suspend fun handleFiltersApplication(searchViewModel: SearchViewModel) {
        if (shouldSkipAutoApplication()) {
            return
        }
        applyFiltersToSearchOnAppStart(searchViewModel)
    }

    private fun applyFiltersToSearchOnAppStart(searchViewModel: SearchViewModel) {
        val currentFilters = getCurrentFilters()
        val hasActiveFilters = currentFilters.salary != null ||
            currentFilters.hideWithoutSalary ||
            currentFilters.industries.isNotEmpty() ||
            currentFilters.country != null ||
            currentFilters.region != null

        if (hasActiveFilters) {
            println(
                "$DEBUG_TAG: Applying saved filters on app start: " +
                    "$currentFilters"
            )
            searchViewModel.setFiltersWithoutSearch(currentFilters)
        } else {
            println("$DEBUG_TAG: No active filters to apply on app start")
        }
    }

    data class Filters(
        val salary: String? = null,
        val hideWithoutSalary: Boolean = false,
        val industries: List<Industry> = emptyList(),
        val country: String? = null,
        val countryId: String? = null,
        val region: String? = null,
        val regionId: String? = null
    )
}
