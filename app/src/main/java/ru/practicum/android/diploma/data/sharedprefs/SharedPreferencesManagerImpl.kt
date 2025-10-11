package ru.practicum.android.diploma.data.sharedprefs

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.api.SharedPreferencesManager
import ru.practicum.android.diploma.domain.models.FilterParameters

class SharedPreferencesManagerImpl(

) : SharedPreferencesManager {
    override suspend fun setFilterSetting(filter: FilterParameters) {
        TODO("Not yet implemented")
    }

    override fun getFilterSetting(): Flow<FilterParameters> {
        TODO("Not yet implemented")
    }
}
