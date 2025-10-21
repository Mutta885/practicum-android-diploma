package ru.practicum.android.diploma.domain.usecase

import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.repository.DataRepository
import ru.practicum.android.diploma.util.Resource

class GetAreasUseCase(
    private val repository: DataRepository
) {
    suspend fun execute(): Resource<List<FilterArea>> {
        return repository.getAreas()
    }

    suspend fun getCountries(): Resource<List<FilterArea>> {
        return repository.getCountries()
    }

    suspend fun getRegionsByCountry(countryId: Int): Resource<List<FilterArea>> {
        return repository.getRegionsByCountry(countryId)
    }

    suspend fun getAllRegions(): Resource<List<FilterArea>> {
        return repository.getAllRegions()
    }

    suspend fun getCountryById(countryId: Int): Resource<FilterArea?> {
        return repository.getCountryById(countryId)
    }
}
