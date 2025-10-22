package ru.practicum.android.diploma.domain.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.Industry

interface GetIndustriesUseCase {
    fun execute(): Flow<Result<List<Industry>?>>
}
