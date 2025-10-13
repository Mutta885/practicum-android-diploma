package ru.practicum.android.diploma.ui.models

import ru.practicum.android.diploma.domain.models.FilterArea

sealed class FilterAreaState {
    data class CountriesState(val countries: List<FilterArea>) : FilterAreaState()
    data class RegionsStateByCountry(val regions: List<FilterArea>) : FilterAreaState()
    data class GetCountryNameState(val countryName: String, val countryId: Int) : FilterAreaState()
}
