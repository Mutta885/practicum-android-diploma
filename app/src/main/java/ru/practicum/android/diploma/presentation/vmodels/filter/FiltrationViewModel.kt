package ru.practicum.android.diploma.presentation.vmodels.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.api.StorageInteractor
import ru.practicum.android.diploma.domain.models.FilterParameters

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

    // Сигнал о том, есть ли активные фильтры
    private val _isAnyFilterActive = MutableLiveData<Boolean>()
    val isAnyFilterActive: LiveData<Boolean> = _isAnyFilterActive

    // Сигнал о том, нужно ли показывать кнопку очистки зарплаты
    private val _isSalaryInputNotEmpty = MutableLiveData<Boolean>()
    val isSalaryInputNotEmpty: LiveData<Boolean> = _isSalaryInputNotEmpty

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
        // Проверяем, что введено только число
        val filteredValue = value.filter { it.isDigit() }
        _salary.value = filteredValue
        updateFilterState()
        updateSalaryInputState()
    }

    fun onHideWithoutSalaryChanged(value: Boolean) {
        _hideWithoutSalary.value = value
        updateFilterState()
    }

    fun resetFilters() {
        _salary.value = ""
        _hideWithoutSalary.value = false
        updateFilterState()
        updateSalaryInputState()
    }

    private fun updateFilterState() {
        val isActive = !salary.value.isNullOrEmpty() || hideWithoutSalary.value == true
        _isAnyFilterActive.value = isActive
    }

    private fun updateSalaryInputState() {
        _isSalaryInputNotEmpty.value = !salary.value.isNullOrEmpty()
    }
}
