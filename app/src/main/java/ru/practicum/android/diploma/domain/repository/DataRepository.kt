package ru.practicum.android.diploma.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.data.dto.Response
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.domain.models.SearchResultVacancyDetail
import ru.practicum.android.diploma.util.Resource

interface DataRepository {
    suspend fun searchVacancies(
        query: String,
        page: Int,
        industry: String? = null,
        salary: Int? = null,
        onlyWithSalary: Boolean = false,
        area: String? = null
    ): Resource<SearchResult>

    fun getIndustries(): Flow<Result<List<Industry>?>>
    suspend fun searchVacancyDetail(query: String): Resource<SearchResultVacancyDetail>
    suspend fun getAreas(): Resource<List<FilterArea>>
    suspend fun getCountries(): Resource<List<FilterArea>>
    suspend fun getRegionsByCountry(countryId: Int): Resource<List<FilterArea>>
    suspend fun getAllRegions(): Resource<List<FilterArea>>
    suspend fun getCountryById(countryId: Int): Resource<FilterArea?>
}
