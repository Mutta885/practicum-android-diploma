package ru.practicum.android.diploma.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.domain.api.SharedPreferencesFilterInteractor
import ru.practicum.android.diploma.domain.api.SharedPreferencesManager
import ru.practicum.android.diploma.domain.models.FilterParameters

class SharedPreferencesFilterInteractorImpl(
    private val sharedPrefsManager: SharedPreferencesManager
) : SharedPreferencesFilterInteractor {
    override suspend fun setFilter(filter: FilterParameters) {
        sharedPrefsManager.setFilterSetting(filter = filter)
    }

    override fun getFilter(): Flow<FilterParameters> {
        return sharedPrefsManager.getFilterSetting()
    }

    override suspend fun clearFilter() {
        sharedPrefsManager.clearFilterSetting()
    }
}
