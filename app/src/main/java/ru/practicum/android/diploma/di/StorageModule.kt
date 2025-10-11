package ru.practicum.android.diploma.di

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.practicum.android.diploma.data.sharedprefs.StorageManagerImpl
import ru.practicum.android.diploma.domain.api.StorageInteractor
import ru.practicum.android.diploma.domain.api.StorageManager
import ru.practicum.android.diploma.domain.impl.StorageInteractorImpl

val sharedPrefsModule = module {
    single<StorageManager> {
        StorageManagerImpl(get(), get())
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences("filter_preferences", MODE_PRIVATE)
    }

    single<StorageInteractor> {
        StorageInteractorImpl(get())
    }
}
