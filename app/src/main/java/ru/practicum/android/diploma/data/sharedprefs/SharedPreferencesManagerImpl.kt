package ru.practicum.android.diploma.data.sharedprefs

import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.domain.api.SharedPreferencesManager
import ru.practicum.android.diploma.domain.models.FilterParameters

class SharedPreferencesManagerImpl(
    val sharedPrefs: SharedPreferences,
    val gson: Gson
) : SharedPreferencesManager {
    override suspend fun setFilterSetting(filter: FilterParameters) {
        withContext(Dispatchers.IO) {
            sharedPrefs.edit()
                .putString(
                    KEY, gson.toJson(filter)
                )
                .apply()
        }
    }

    override fun getFilterSetting(): Flow<FilterParameters> {
        return flow {
            val string = sharedPrefs.getString(KEY, null)
            val prefs = gson.fromJson<FilterParameters>(string, FilterParameters::class.java)
            emit(prefs)
        }.flowOn(Dispatchers.IO)
    }

    private companion object{
        private const val KEY = "filter"
    }
}
