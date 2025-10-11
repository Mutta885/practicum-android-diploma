package ru.practicum.android.diploma.presentation.vmodels.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.api.StorageInteractor
import ru.practicum.android.diploma.domain.models.FilterParameters
import ru.practicum.android.diploma.domain.models.Industry

class FiltrationViewModel(
    val filterInteractor: StorageInteractor
) : ViewModel() {
    private val _filterParametersState = MutableLiveData<FilterParameters>(FilterParameters())
    val observeFilterParametersState: LiveData<FilterParameters> = _filterParametersState

    // Состояние фильтров
    private val _salary = MutableLiveData<String>()
    val salary: LiveData<String> = _salary

    private val _hideWithoutSalary = MutableLiveData<Boolean>()
    val hideWithoutSalary: LiveData<Boolean> = _hideWithoutSalary

    private val _selectedIndustries = MutableLiveData<List<Industry>>()
    val selectedIndustries: LiveData<List<Industry>> = _selectedIndustries

    private val _isAnyFilterActive = MutableLiveData<Boolean>()
    val isAnyFilterActive: LiveData<Boolean> = _isAnyFilterActive

    private val _isSalaryInputNotEmpty = MutableLiveData<Boolean>()
    val isSalaryInputNotEmpty: LiveData<Boolean> = _isSalaryInputNotEmpty

    // LiveData для передачи фильтров в поиск
    private val _filters = MutableLiveData<Filters>()
    val filters: LiveData<Filters> = _filters

    init {
        updateFilterState()
        updateSalaryInputState()
    }

    fun fetchFilterInSharedPreferences() {
        viewModelScope.launch {
            filterInteractor.getFilter().collect { filter ->
                filter?.let { _filterParametersState.postValue(it) }
            }
        }
    }

    fun saveFilterInSharedPreferences() {
        viewModelScope.launch {
            _filterParametersState.value?.let {
                it.onlyWithSalary = _hideWithoutSalary.value ?: false
                it.salary = _salary.value ?: ""
                filterInteractor.setFilter(it)
            }
        }
    }

    fun clearFilterInSharedPreferences(){
        viewModelScope.launch {
            if(hideWithoutSalary.value == false && _salary.value.isNullOrEmpty()) {
                filterInteractor.clearFilter()
            }
        }
    }

    fun onSalaryChanged(value: String) {
        val filteredValue = value.filter { it.isDigit() }
        _salary.value = filteredValue
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

    fun resetFilters() {
        _salary.value = ""
        _hideWithoutSalary.value = false
        _selectedIndustries.value = emptyList()
        updateFilterState()
        updateSalaryInputState()
        // При сбросе также сбрасываем фильтры для поиска
        _filters.value = Filters()
    }

    // Новый метод для применения фильтров
    fun applyFilters() {
        val currentFilters = Filters(
            salary = _salary.value,
            hideWithoutSalary = _hideWithoutSalary.value ?: false,
            industries = _selectedIndustries.value ?: emptyList()
        )
        _filters.value = currentFilters
    }

    private fun updateFilterState() {
        val isActive = !salary.value.isNullOrEmpty() ||
            hideWithoutSalary.value == true ||
            !selectedIndustries.value.isNullOrEmpty()

        _isAnyFilterActive.value = isActive
    }

    private fun updateSalaryInputState() {
        _isSalaryInputNotEmpty.value = !salary.value.isNullOrEmpty()
    }

    // Data class для хранения фильтров
    data class Filters(
        val salary: String? = null,
        val hideWithoutSalary: Boolean = false,
        val industries: List<Industry> = emptyList()
    )
}
