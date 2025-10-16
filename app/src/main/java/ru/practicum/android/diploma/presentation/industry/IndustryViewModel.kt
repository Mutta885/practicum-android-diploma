package ru.practicum.android.diploma.presentation.industry

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.usecase.GetIndustriesUseCase

class IndustryViewModel(
    private val getIndustriesUseCase: GetIndustriesUseCase,
    private val appContext: Context
) : ViewModel() {

    private val _industries = MutableLiveData<List<Industry>>()
    val industries: LiveData<List<Industry>> = _industries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun refresh() {
        loadIndustries()
    }

    fun loadIndustries() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = getIndustriesUseCase()
            if (result.isSuccess) {
                _industries.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: appContext.getString(R.string.error_load_industry)
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
