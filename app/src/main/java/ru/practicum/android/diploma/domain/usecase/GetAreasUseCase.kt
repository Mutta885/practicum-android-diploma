package ru.practicum.android.diploma.domain.usecase

import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.repository.DataRepository

class GetAreasUseCase(
    private val repository: DataRepository
) {
    suspend fun execute(): Result<List<FilterArea>> {
        val areas = repository.getAreas()
        return Result.success(areas)
    }
}
