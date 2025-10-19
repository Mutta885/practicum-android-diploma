package ru.practicum.android.diploma.domain.usecaseimpl

import ru.practicum.android.diploma.domain.api.DataRepository
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.presentation.vmodels.FiltrationViewModel
import ru.practicum.android.diploma.util.Resource

class SearchVacanciesUseCase(
    private val repository: DataRepository
) {

    private companion object {
        private const val DEBUG_TAG = "SearchVacanciesUseCase"
    }

    suspend fun execute(
        query: String,
        page: Int,
        filters: FiltrationViewModel.Filters
    ): Resource<SearchResult> {
        println("$DEBUG_TAG: execute() called")
        println("$DEBUG_TAG: Original query: '$query'")
        println("$DEBUG_TAG: Filters: $filters")

        val industry = getIndustryId(filters)
        val salary = getSalary(filters)
        val onlyWithSalary = filters.hideWithoutSalary
        val area = getAreaId(filters)

        println(
            "$DEBUG_TAG: API params - " +
                "industry: $industry, " +
                "salary: $salary, " +
                "onlyWithSalary: $onlyWithSalary, " +
                "area: $area"
        )

        val result = repository.searchVacancies(
            query = query,
            page = page,
            industry = industry,
            salary = salary,
            onlyWithSalary = onlyWithSalary,
            area = area
        )

        println("$DEBUG_TAG: Search result: $result")
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
