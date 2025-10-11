package ru.practicum.android.diploma.domain.usecases

import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.DataRepository

class GetIndustriesUseCase(private val repository: DataRepository) {
    suspend operator fun invoke(): Result<List<Industry>> {
        return try {
            val industries = repository.getIndustries()
            Result.success(industries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
