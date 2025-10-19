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

    private val filterStateHandler = FilterStateHandler()

    // Public LiveData для UI
    val salary: LiveData<String?> get() = filterStateHandler.salary
    val hideWithoutSalary: LiveData<Boolean> get() = filterStateHandler.hideWithoutSalary
    val selectedIndustries: LiveData<List<Industry>> get() = filterStateHandler.selectedIndustries
    val selectedCountry: LiveData<String?> get() = filterStateHandler.selectedCountry
    val selectedCountryId: LiveData<String?> get() = filterStateHandler.selectedCountryId
    val selectedRegion: LiveData<String?> get() = filterStateHandler.selectedRegion
    val selectedRegionId: LiveData<String?> get() = filterStateHandler.selectedRegionId
    val isAnyFilterActive: LiveData<Boolean> get() = filterStateHandler.isAnyFilterActive
    val isSalaryInputNotEmpty: LiveData<Boolean> get() = filterStateHandler.isSalaryInputNotEmpty
    val filtersJustApplied: LiveData<Boolean> get() = filterStateHandler.filtersJustApplied

    init {
        loadSavedFilters()
    }

    private fun loadSavedFilters() {
        viewModelScope.launch {
            storageManager.getFilterSetting().collect { savedFilter ->
                handleSavedFilter(savedFilter)
            }
        }
    }

    private fun handleSavedFilter(savedFilter: FilterParameters?) {
        if (savedFilter != null) {
            loadFiltersFromStorage(savedFilter)
        } else {
            setDefaultFilters()
        }
        filterStateHandler.updateFilterState()
        filterStateHandler.updateSalaryInputState()
        saveInitialState()
    }

    private fun loadFiltersFromStorage(savedFilter: FilterParameters) {
        filterStateHandler.restoreFiltersFromState(
            Filters(
                salary = savedFilter.salary.takeIf { it.isNotEmpty() },
                hideWithoutSalary = savedFilter.onlyWithSalary,
                industries = savedFilter.industries,
                country = savedFilter.country,
                countryId = savedFilter.countryId,
                region = savedFilter.region,
                regionId = savedFilter.regionId
            )
        )

        filterStateHandler.updateCurrentAppliedFilters(filterStateHandler.getCurrentFiltersFromUI())

        println(
            "FiltrationViewModel: Filters loaded from storage: " +
                "salary=${filterStateHandler.salary.value}, " +
                "hideWithoutSalary=${filterStateHandler.hideWithoutSalary.value}, " +
                "industries=${filterStateHandler.selectedIndustries.value?.size}, " +
                "country=${filterStateHandler.selectedCountry.value}, countryId=${filterStateHandler.selectedCountryId.value}, " +
                "region=${filterStateHandler.selectedRegion.value}, regionId=${filterStateHandler.selectedRegionId.value}"
        )
    }

    private fun setDefaultFilters() {
        filterStateHandler.resetFilters()
        filterStateHandler.updateCurrentAppliedFilters(Filters())
        println("FiltrationViewModel: No saved filters found")
    }

    // Public методы для UI взаимодействий
    fun onSalaryChanged(value: String) {
        filterStateHandler.onSalaryChanged(value)
    }

    fun onHideWithoutSalaryChanged(value: Boolean) {
        filterStateHandler.onHideWithoutSalaryChanged(value)
    }

    fun onIndustriesSelected(industries: List<Industry>) {
        filterStateHandler.onIndustriesSelected(industries)
    }

    fun clearIndustries() {
        filterStateHandler.clearIndustries()
    }

    fun clearWorkplace() {
        filterStateHandler.clearWorkplace()
    }

    fun onWorkplaceSelected(
        countryName: String?,
        countryId: Int?,
        regionName: String?,
        regionId: Int?
    ) {
        filterStateHandler.onWorkplaceSelected(countryName, countryId, regionName, regionId)
    }

    fun saveFiltersToStorage() {
        viewModelScope.launch {
            val filter = createFilterParameters()
            storageManager.setFilterSetting(filter)

            filterStateHandler.updateCurrentAppliedFilters(filterStateHandler.getCurrentFiltersFromUI())
            println("FiltrationViewModel: Filters saved to storage: ${filterStateHandler.currentAppliedFilters}")
        }
    }

    private fun createFilterParameters(): FilterParameters {
        return FilterParameters(
            onlyWithSalary = filterStateHandler.hideWithoutSalary.value ?: false,
            salary = filterStateHandler.salary.value ?: "",
            industries = filterStateHandler.selectedIndustries.value ?: emptyList(),
            country = filterStateHandler.selectedCountry.value,
            countryId = filterStateHandler.selectedCountryId.value,
            region = filterStateHandler.selectedRegion.value,
            regionId = filterStateHandler.selectedRegionId.value
        )
    }

    fun getCurrentFiltersForApply(): Filters {
        return filterStateHandler.getCurrentFiltersFromUI()
    }

    fun getCurrentAppliedFilters(): Filters {
        return filterStateHandler.currentAppliedFilters
    }

    fun resetFilters() {
        filterStateHandler.resetFilters()
    }

    fun setFiltersJustApplied(applied: Boolean) {
        filterStateHandler.setFiltersJustApplied(applied)
    }

    // Методы для восстановления состояния
    fun saveInitialState() {
        filterStateHandler.saveInitialState()
    }

    fun restoreInitialState() {
        filterStateHandler.restoreInitialState()
    }

    fun applySavedFiltersToSearch(searchViewModel: SearchViewModel) {
        viewModelScope.launch {
            delay(FILTERS_DELAY_MS)
            handleFiltersApplication(searchViewModel)
        }
    }

    private suspend fun handleFiltersApplication(searchViewModel: SearchViewModel) {
        if (filterStateHandler.shouldSkipAutoApplication()) {
            return
        }
        applyFiltersToSearchOnAppStart(searchViewModel)
    }

    private fun applyFiltersToSearchOnAppStart(searchViewModel: SearchViewModel) {
        val currentFilters = filterStateHandler.currentAppliedFilters
        val hasActiveFilters = currentFilters.salary != null ||
            currentFilters.hideWithoutSalary ||
            currentFilters.industries.isNotEmpty() ||
            currentFilters.country != null ||
            currentFilters.region != null

        if (hasActiveFilters) {
            println(
                "FiltrationViewModel: Applying saved filters on app start: " +
                    "$currentFilters"
            )
            searchViewModel.setFiltersWithoutSearch(currentFilters)
        } else {
            println("FiltrationViewModel: No active filters to apply on app start")
        }
    }

    companion object {
        const val FILTERS_DELAY_MS = 200L
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
