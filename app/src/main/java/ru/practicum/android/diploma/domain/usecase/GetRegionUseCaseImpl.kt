package ru.practicum.android.diploma.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.api.GetRegionUseCase
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.repository.DataRepository
import ru.practicum.android.diploma.util.Resource

class GetRegionUseCaseImpl(
    private val repository: DataRepository
):GetRegionUseCase {
    override suspend fun execute(): Resource<List<FilterArea>> {
        return repository.getAreas()
    }

    override suspend fun getCountries(): Resource<List<FilterArea>> {
        return repository.getCountries()
    }

    override suspend fun getRegionsByCountry(countryId: Int): Resource<List<FilterArea>> {
        return repository.getRegionsByCountry(countryId)
    }

    override fun getAllRegions(): Flow<Result<List<FilterArea>>> {
        return repository.getAllRegions()
    }

    override suspend fun getCountryById(countryId: Int): Resource<FilterArea?> {
        return repository.getCountryById(countryId)
    }
}
