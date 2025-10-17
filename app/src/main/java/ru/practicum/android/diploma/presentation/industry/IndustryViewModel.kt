package ru.practicum.android.diploma.presentation.industry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.api.GetIndustriesUseCase
import ru.practicum.android.diploma.domain.models.Industry

class IndustryViewModel(
    private val getIndustriesUseCase: GetIndustriesUseCase
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
            val result = getIndustriesUseCase.execute()
            result.collect { response ->
                _isLoading.value = false
                with(response) {
                    when{
                         isSuccess-> {
                            getOrNull()?.let {
                                _industries.postValue(it)
                            } ?: _industries.postValue(emptyList())
                        }
                        isFailure -> {
                            response.exceptionOrNull()
                            exceptionOrNull().let {
                                _error.postValue(it?.message)
                            }
                        }
                    }
                }
            }
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
