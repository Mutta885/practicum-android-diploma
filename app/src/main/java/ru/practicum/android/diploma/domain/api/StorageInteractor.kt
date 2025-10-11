package ru.practicum.android.diploma.domain.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.FilterParameters

interface StorageInteractor {
    suspend fun setFilter(filter: FilterParameters)
    fun getFilter(): Flow<FilterParameters?>
    suspend fun clearFilter()
}
