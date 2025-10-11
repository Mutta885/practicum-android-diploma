package ru.practicum.android.diploma.domain.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.FilterParameters

interface SharedPreferencesManager {
    suspend fun setFilterSetting(filter: FilterParameters)
    fun getFilterSetting(): Flow<FilterParameters>
}
