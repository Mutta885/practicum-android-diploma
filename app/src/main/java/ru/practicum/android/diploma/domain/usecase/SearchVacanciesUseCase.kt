package ru.practicum.android.diploma.domain.usecase

import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.domain.repository.DataRepository
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.util.Resource

class SearchVacanciesUseCase(
    private val repository: DataRepository
) {

    suspend fun execute(
        query: String,
        page: Int,
        filters: FiltrationViewModel.Filters // УБРАНО значение по умолчанию
    ): Resource<SearchResult> {

        println("DEBUG: SearchVacanciesUseCase.execute() called")
        println("DEBUG: Original query: '$query'")
        println("DEBUG: Filters: $filters")

        // Собираем финальный запрос с фильтрами
        val finalQuery = buildQueryWithFilters(query, filters)
        println("DEBUG: Final search query: '$finalQuery'")

        val result = repository.searchVacancies(finalQuery, page)
        println("DEBUG: Search result: $result")

        return result
    }

    private fun buildQueryWithFilters(
        baseQuery: String,
        filters: FiltrationViewModel.Filters
    ): String {
        val queryBuilder = StringBuilder(baseQuery.trim())

        println("DEBUG: Building query with filters: $filters")

        // Добавляем фильтр по отрасли
        if (filters.industries.isNotEmpty()) {
            // Берем ID первой выбранной отрасли
            val industryId = filters.industries.first().id
            if (queryBuilder.isNotEmpty()) {
                queryBuilder.append(" ")
            }
            queryBuilder.append("industry:$industryId")
            println("DEBUG: Added industry filter: industry:$industryId")
        }

        // Добавляем фильтр по зарплате
        filters.salary?.let { salary ->
            if (salary.isNotEmpty()) {
                val salaryValue = salary.toIntOrNull() ?: 0
                if (queryBuilder.isNotEmpty()) {
                    queryBuilder.append(" ")
                }
                queryBuilder.append("salary:$salaryValue")
                println("DEBUG: Added salary filter: salary:$salaryValue")
            }
        }

        // Добавляем фильтр "только с зарплатой"
        if (filters.hideWithoutSalary) {
            if (queryBuilder.isNotEmpty()) {
                queryBuilder.append(" ")
            }
            queryBuilder.append("with_salary:true")
            println("DEBUG: Added 'only with salary' filter")
        }

        val finalQuery = queryBuilder.toString().trim()
        println("DEBUG: Built final query: '$finalQuery'")
        return finalQuery
    }
}
