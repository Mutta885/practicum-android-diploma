package ru.practicum.android.diploma.di

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.practicum.android.diploma.data.sharedprefs.SharedPreferencesManagerImpl
import ru.practicum.android.diploma.domain.api.SharedPreferencesManager

val sharedPrefsModule = module {
    single<SharedPreferencesManager> {
        SharedPreferencesManagerImpl()
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences("filter_preferences", MODE_PRIVATE)
    }
}
