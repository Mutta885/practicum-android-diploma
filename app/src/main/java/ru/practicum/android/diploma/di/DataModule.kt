package ru.practicum.android.diploma.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.koin.dsl.module

val dataModule = module {
    single<SharedPreferences> {
        get<Context>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    single { Gson() }
}
