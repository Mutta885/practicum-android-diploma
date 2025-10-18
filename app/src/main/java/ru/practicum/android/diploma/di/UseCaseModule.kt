package ru.practicum.android.diploma.di

import org.koin.dsl.module
import ru.practicum.android.diploma.domain.api.GetIndustriesUseCase
import ru.practicum.android.diploma.domain.api.GetRegionUseCase
import ru.practicum.android.diploma.domain.usecaseimpl.GetRegionUseCaseImpl
import ru.practicum.android.diploma.domain.usecaseimpl.SearchVacanciesUseCase
import ru.practicum.android.diploma.domain.usecaseimpl.SearchVacancyDetailUseCase
import ru.practicum.android.diploma.domain.usecaseimpl.GetIndustriesUseCaseImpl

val useCaseModule = module {
    factory { SearchVacanciesUseCase(repository = get()) }
    factory { SearchVacancyDetailUseCase(repository = get()) }
    factory<GetRegionUseCase> { GetRegionUseCaseImpl(repository = get()) }
    factory<GetIndustriesUseCase> { GetIndustriesUseCaseImpl(repository = get()) }
}
