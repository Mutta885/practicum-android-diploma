package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.presentation.vmodels.RegionViewModel
import ru.practicum.android.diploma.data.sharedprefs.StorageManagerImpl
import ru.practicum.android.diploma.domain.api.StorageManager
import ru.practicum.android.diploma.presentation.vmodels.IndustryViewModel
import ru.practicum.android.diploma.presentation.vmodels.VacancyDetailViewModel
import ru.practicum.android.diploma.presentation.vmodels.FiltrationViewModel
import ru.practicum.android.diploma.presentation.vmodels.SearchViewModel

val viewModelModule = module {
    viewModel { IndustryViewModel(getIndustriesUseCase = get()) }
    viewModel { SearchViewModel(searchVacanciesUseCase = get()) }
    viewModel {
        VacancyDetailViewModel(
            searchVacancyDetailUseCase = get(),
            interactorFavorites = get(),
            shared = get()
        )
    }
    viewModel {
        RegionViewModel(getRegionUseCase = get())
    }
    viewModel { FiltrationViewModel(storageManager = get()) }
    single<StorageManager> { StorageManagerImpl(get(), get()) }
}
