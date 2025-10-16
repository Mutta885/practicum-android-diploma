package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel
import ru.practicum.android.diploma.data.sharedprefs.StorageManagerImpl
import ru.practicum.android.diploma.domain.api.StorageManager
import ru.practicum.android.diploma.presentation.industry.IndustryViewModel
import ru.practicum.android.diploma.presentation.vmodels.VacancyDetailViewModel
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

val viewModelModule = module {
    viewModel { IndustryViewModel(getIndustriesUseCase = get(),get()) }
    viewModel { SearchViewModel(searchVacanciesUseCase = get()) }
    viewModel {
        VacancyDetailViewModel(
            searchVacancyDetailUseCase = get(),
            interactorFavorites = get(),
            shared = get()
        )
    }
    viewModel {
        AreasViewModel(getAreasUseCase = get())
    }
    viewModel { FiltrationViewModel(storageManager = get()) }
    single<StorageManager> { StorageManagerImpl(get(), get()) }
}
