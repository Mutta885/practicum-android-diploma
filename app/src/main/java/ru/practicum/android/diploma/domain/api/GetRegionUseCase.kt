package ru.practicum.android.diploma.domain.api

import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.util.Resource

interface GetRegionUseCase {
    suspend fun execute(): Resource<List<FilterArea>>
    suspend fun getCountries(): Resource<List<FilterArea>>
    suspend fun getRegionsByCountry(countryId: Int): Resource<List<FilterArea>>
    suspend fun getAllRegions(): Resource<List<FilterArea>>
    suspend fun getCountryById(countryId: Int): Resource<FilterArea?>
}
