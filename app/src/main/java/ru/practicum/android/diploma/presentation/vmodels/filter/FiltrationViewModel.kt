package ru.practicum.android.diploma.presentation.vmodels.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.practicum.android.diploma.domain.models.Industry

class FiltrationViewModel : ViewModel() {

    // Состояние фильтров - ИСПРАВЛЕНО: инициализируем значения
    private val _salary = MutableLiveData<String?>(null)
    val salary: LiveData<String?> = _salary

    private val _hideWithoutSalary = MutableLiveData<Boolean>(false) // по умолчанию false
    val hideWithoutSalary: LiveData<Boolean> = _hideWithoutSalary

    private val _selectedIndustries = MutableLiveData<List<Industry>>(emptyList())
    val selectedIndustries: LiveData<List<Industry>> = _selectedIndustries

    private val _isAnyFilterActive = MutableLiveData<Boolean>(false)
    val isAnyFilterActive: LiveData<Boolean> = _isAnyFilterActive

    private val _isSalaryInputNotEmpty = MutableLiveData<Boolean>(false)
    val isSalaryInputNotEmpty: LiveData<Boolean> = _isSalaryInputNotEmpty

    init {
        updateFilterState()
        updateSalaryInputState()
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

    fun resetFilters() {
        _salary.value = null
        _hideWithoutSalary.value = false
        _selectedIndustries.value = emptyList()
        updateFilterState()
        updateSalaryInputState()
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

    // Data class для хранения фильтров
    data class Filters(
        val salary: String? = null,
        val hideWithoutSalary: Boolean = false,
        val industries: List<Industry> = emptyList()
    )
}
