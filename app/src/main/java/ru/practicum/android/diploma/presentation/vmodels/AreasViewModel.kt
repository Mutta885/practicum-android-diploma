package ru.practicum.android.diploma.presentation.vmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.usecase.GetAreasUseCase
import ru.practicum.android.diploma.ui.models.FilterAreaState
import ru.practicum.android.diploma.util.Resource

class AreasViewModel(private val getAreasUseCase: GetAreasUseCase) : ViewModel() {

    private var searchJob: Job? = null
    private val _filterAreaState = MutableLiveData<FilterAreaState>()
    val filterAreaState: LiveData<FilterAreaState> = _filterAreaState

    fun getCountries() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _filterAreaState.postValue(FilterAreaState.Loading)
            when (val result = getAreasUseCase.getCountries()) {
                is Resource.Success -> {
                    val countries = result.data
                    if (countries.isNotEmpty()) {
                        _filterAreaState.postValue(FilterAreaState.CountriesState(countries))
                    } else {
                        _filterAreaState.postValue(FilterAreaState.Error("Список стран пуст"))
                    }
                }

                is Resource.Error -> {
                    _filterAreaState.postValue(FilterAreaState.Error(result.message ?: "Ошибка загрузки стран"))
                }

                else -> {}
            }
        }
    }

    fun getRegions(countryId: Int?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _filterAreaState.postValue(FilterAreaState.Loading)
            val result = if (countryId != null) {
                getAreasUseCase.getRegionsByCountry(countryId)
            } else {
                getAreasUseCase.getAllRegions()
            }

            when (result) {
                is Resource.Success -> {
                    val regions = result.data
                    if (regions.isNotEmpty()) {
                        _filterAreaState.postValue(FilterAreaState.RegionsStateByCountry(regions))
                    } else {
                        _filterAreaState.postValue(FilterAreaState.Error("Список регионов пуст"))
                    }
                }

                is Resource.Error -> {
                    _filterAreaState.postValue(FilterAreaState.Error(result.message ?: "Ошибка загрузки регионов"))
                }

                else -> {}
            }
        }
    }

    fun getCountryNameByRegion(parentId: Int?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _filterAreaState.postValue(FilterAreaState.Loading)
            if (parentId != null) {
                when (val result = getAreasUseCase.getCountryById(parentId)) {
                    is Resource.Success -> {
                        val country = result.data
                        if (country != null) {
                            _filterAreaState.postValue(FilterAreaState.GetCountryNameState(country.name, country.id))
                        } else {
                            _filterAreaState.postValue(FilterAreaState.Error("Страна не найдена"))
                        }
                    }

                    is Resource.Error -> {
                        _filterAreaState.postValue(FilterAreaState.Error(result.message ?: "Ошибка загрузки страны"))
                    }

                    else -> {}
                }
            }
        }
    }
}
