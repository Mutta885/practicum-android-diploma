package ru.practicum.android.diploma.domain.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.api.StorageInteractor
import ru.practicum.android.diploma.domain.api.StorageManager
import ru.practicum.android.diploma.domain.models.FilterParameters

class StorageInteractorImpl(
    private val sharedPrefsManager: StorageManager
) : StorageInteractor {
    override suspend fun setFilter(filter: FilterParameters) {
        sharedPrefsManager.setFilterSetting(filter = filter)
    }

    override fun getFilter(): Flow<FilterParameters?> {
        return sharedPrefsManager.getFilterSetting()
    }

    override suspend fun clearFilter() {
        sharedPrefsManager.clearFilterSetting()
    }
}
