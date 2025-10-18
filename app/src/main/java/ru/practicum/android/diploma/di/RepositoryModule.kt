package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.data.repository.DataRepositoryImpl
import ru.practicum.android.diploma.domain.api.DataRepository

val repositoryModule = module {
    single<DataRepository> {
        DataRepositoryImpl(api = get(), networkClient = get(), converters = get())
    }
}
