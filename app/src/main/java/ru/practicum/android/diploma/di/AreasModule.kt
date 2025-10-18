package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.domain.usecase.GetRegionUseCaseImpl
import ru.practicum.android.diploma.presentation.vmodels.RegionViewModel

val areasModule = module {
    viewModel { RegionViewModel(get()) }
    factory { GetRegionUseCaseImpl(get()) }
}
