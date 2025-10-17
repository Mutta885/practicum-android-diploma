package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.domain.api.GetIndustriesUseCase
import ru.practicum.android.diploma.domain.usecase.GetAreasUseCase
import ru.practicum.android.diploma.domain.usecase.GetVacanciesUseCase
import ru.practicum.android.diploma.domain.usecase.SearchVacanciesUseCase
import ru.practicum.android.diploma.domain.usecase.SearchVacancyDetailUseCase
import ru.practicum.android.diploma.domain.usecase.GetIndustriesUseCaseImpl

val useCaseModule = module {
    factory { GetVacanciesUseCase(repository = get()) }
    factory { SearchVacanciesUseCase(repository = get()) }
    factory { SearchVacancyDetailUseCase(repository = get()) }
    factory { GetAreasUseCase(repository = get()) }
    factory<GetIndustriesUseCase> { GetIndustriesUseCaseImpl(repository = get()) }
}
