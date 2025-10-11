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

    fun refresh() {
        loadIndustries()
    }

    init {
        loadIndustries()
    }

    private fun loadIndustries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = getIndustriesUseCase()
            if (result.isSuccess) {
                _industries.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Ошибка загрузки отраслей"
            }

            _isLoading.value = false
        }
    }

    fun search(query: String) {
        val filtered = if (query.isEmpty()) {
            _industries.value ?: emptyList()
        } else {
            _industries.value?.filter { it.name.contains(query, ignoreCase = true) } ?: emptyList()
        }
        _industries.value = filtered
    }
}
