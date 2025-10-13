package ru.practicum.android.diploma.presentation.vmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.usecase.GetAreasUseCase
import ru.practicum.android.diploma.ui.models.FilterAreaState

class AreasViewModel(private val getAreasUseCase: GetAreasUseCase) : ViewModel() {

    private var searchJob: Job? = null
    private val _filterAreaState = MutableLiveData<FilterAreaState>()
    val filterAreaState: LiveData<FilterAreaState> = _filterAreaState

    fun getCountries() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = getAreasUseCase.execute()
            val searchResult = result.getOrThrow()
            val countriesList = searchResult.filter { it.parentId == 0 }
            _filterAreaState.postValue(FilterAreaState.CountriesState(countriesList))
        }
    }

    fun getRegions(countryId: Int?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = getAreasUseCase.execute()
            val searchResult = result.getOrThrow()
            val countries = searchResult.filter { it.id == countryId }
            if (countryId != null) {
                val regionsByCountry = countries.first().areas
                _filterAreaState.postValue(FilterAreaState.RegionsStateByCountry(regionsByCountry))
            }
        }
    }

    fun getCountryNameByRegion(parentId: Int?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val result = getAreasUseCase.execute()
            val searchResult = result.getOrThrow()
            val countryName = searchResult.filter { it.id == parentId }.first().name.toString()
            _filterAreaState.postValue(FilterAreaState.GetCountryNameState(countryName, parentId!!))
        }
    }
}
