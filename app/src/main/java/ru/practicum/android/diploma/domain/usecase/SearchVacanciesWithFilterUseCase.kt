package ru.practicum.android.diploma.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.util.Resource

interface SearchVacanciesWithFilterUseCase {
    fun execute(): Flow<Resource<SearchResult>>
}
