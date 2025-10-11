package ru.practicum.android.diploma.data.sharedprefs

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.domain.api.SharedPreferencesManager
import ru.practicum.android.diploma.domain.models.FilterParameters

class SharedPreferencesManagerImpl(
    private val sharedPrefs: SharedPreferences,
    private val gson: Gson
) : SharedPreferencesManager {
    override suspend fun setFilterSetting(filter: FilterParameters) {
        Log.v("my", "SPManager set  \n $filter")
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
            Log.v("my", "SPManager get  \n $prefs")
            emit(prefs?:FilterParameters())
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun clearFilterSetting() {
        Log.v("my", "SPManager clear")
        sharedPrefs.edit()
            .clear()
            .apply()
    }

    private companion object {
        private const val KEY = "filter"
    }
}
