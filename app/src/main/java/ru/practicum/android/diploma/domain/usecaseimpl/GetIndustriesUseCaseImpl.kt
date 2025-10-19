package ru.practicum.android.diploma.domain.usecaseimpl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.api.DataRepository
import ru.practicum.android.diploma.domain.api.GetIndustriesUseCase
import ru.practicum.android.diploma.domain.models.Industry

class GetIndustriesUseCaseImpl(
    private val repository: DataRepository
) : GetIndustriesUseCase {

    override fun execute(): Flow<Result<List<Industry>?>> {
        return repository.getIndustries()
    }
}
