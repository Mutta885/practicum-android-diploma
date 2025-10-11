package ru.practicum.android.diploma.domain.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.domain.repository.DataRepository
import ru.practicum.android.diploma.domain.usecase.SearchVacanciesWithFilterUseCase
import ru.practicum.android.diploma.util.Resource

class SearchVacanciesWithFilterUseCaseImpl(
    val repository: DataRepository
) : SearchVacanciesWithFilterUseCase {
    override fun execute(query: Map<String,String>, page: Int): Flow<Resource<SearchResult>> {
        return  repository.searchVacanciesWithFilter(query = query, page = page)
    }
}
