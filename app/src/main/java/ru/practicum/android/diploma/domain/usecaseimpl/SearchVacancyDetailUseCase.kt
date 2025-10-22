package ru.practicum.android.diploma.domain.usecaseimpl

import ru.practicum.android.diploma.domain.api.DataRepository
import ru.practicum.android.diploma.domain.models.SearchResultVacancyDetail
import ru.practicum.android.diploma.util.Resource


class SearchVacancyDetailUseCase(
    private val repository: DataRepository
) {
    suspend fun execute(query: String): Resource<SearchResultVacancyDetail> {
        return repository.searchVacancyDetail(query)
    }
}
