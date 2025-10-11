package ru.practicum.android.diploma.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.domain.models.SearchResultVacancyDetail
import ru.practicum.android.diploma.util.Resource

interface DataRepository {
    suspend fun searchVacancies(query: String, page: Int): Resource<SearchResult>
    suspend fun searchVacancyDetail(query: String): Resource<SearchResultVacancyDetail>
    fun searchVacanciesWithFilter(query: Map<String, String>, page: Int): Flow<Resource<SearchResult>>
    suspend fun getIndustries(): List<Industry>
}
