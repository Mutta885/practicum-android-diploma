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
        filters: FiltrationViewModel.Filters
    ): Resource<SearchResult> {
        println("DEBUG: SearchVacanciesUseCase.execute() called")
        println("DEBUG: Original query: '$query'")
        println("DEBUG: Filters: $filters")

        val industry = getIndustryId(filters)
        val salary = getSalary(filters)
        val onlyWithSalary = filters.hideWithoutSalary
        val area = getAreaId(filters)

        println("DEBUG: API params - industry: $industry, salary: $salary, onlyWithSalary: $onlyWithSalary, area: $area")

        val result = repository.searchVacancies(
            query = query,
            page = page,
            industry = industry,
            salary = salary,
            onlyWithSalary = onlyWithSalary,
            area = area
        )

        println("DEBUG: Search result: $result")
        return result
    }

    private fun getIndustryId(filters: FiltrationViewModel.Filters): String? {
        return filters.industries.firstOrNull()?.id
    }

    private fun getSalary(filters: FiltrationViewModel.Filters): Int? {
        return filters.salary?.toIntOrNull()?.takeIf { it > 0 }
    }

    private fun getAreaId(filters: FiltrationViewModel.Filters): String? {
        return filters.regionId ?: filters.countryId
    }
}
