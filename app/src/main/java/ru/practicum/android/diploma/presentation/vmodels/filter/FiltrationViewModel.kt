package ru.practicum.android.diploma.presentation.vmodels.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FiltrationViewModel : ViewModel() {

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

    fun saveFilterInSharedPreferences() {

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
