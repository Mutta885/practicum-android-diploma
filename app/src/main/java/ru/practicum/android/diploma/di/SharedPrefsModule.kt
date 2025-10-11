package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.data.sharedprefs.SharedPreferencesManagerImpl
import ru.practicum.android.diploma.domain.api.SharedPreferencesManager

val sharedPrefsModule = module {
    single<SharedPreferencesManager> {
        SharedPreferencesManagerImpl()
    }
}
