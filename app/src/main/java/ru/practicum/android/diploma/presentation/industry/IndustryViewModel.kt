package ru.practicum.android.diploma.presentation.industry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.usecase.GetIndustriesUseCase

class IndustryViewModel(
    private val getIndustriesUseCase: GetIndustriesUseCase
) : ViewModel() {

    private val _industries = MutableLiveData<List<Industry>>()
    val industries: LiveData<List<Industry>> = _industries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var originalIndustries: List<Industry> = emptyList()

    init {
        println("DEBUG: IndustryViewModel init")
        loadIndustries()
    }

    private fun loadIndustries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            println("DEBUG: Loading industries from use case")
            val result = getIndustriesUseCase()

            if (result.isSuccess) {
                originalIndustries = result.getOrNull() ?: emptyList()
                println("DEBUG: Loaded ${originalIndustries.size} industries")
                _industries.value = originalIndustries
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Ошибка загрузки отраслей"
                println("DEBUG: Error loading industries: ${_error.value}")
            }

            _isLoading.value = false
        }
    }

    fun search(query: String) {
        println("DEBUG: Searching industries with query: '$query'")
        val filtered = if (query.isEmpty()) {
            originalIndustries
        } else {
            originalIndustries.filter { it.name.contains(query, ignoreCase = true) }
        }
        println("DEBUG: Found ${filtered.size} industries after search")
        _industries.value = filtered
    }

    fun getSelectedIndustries(): List<Industry> {
        val selected = _industries.value?.filter { it.isSelected } ?: emptyList()
        println("DEBUG: getSelectedIndustries - total: ${_industries.value?.size}, selected: ${selected.size}")
        selected.forEach { println("DEBUG: Selected industry: ${it.name} (id: ${it.id})") }
        return selected
    }

    fun retry() {
        println("DEBUG: Retry loading industries")
        loadIndustries()
    }
}
