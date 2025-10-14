package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.domain.usecase.GetAreasUseCase
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel

val areasModule = module {
    viewModel { AreasViewModel(get()) }
    factory { GetAreasUseCase(get()) }
}
