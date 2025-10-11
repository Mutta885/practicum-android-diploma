package ru.practicum.android.diploma.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.practicum.android.diploma.presentation.industry.IndustryViewModel
import ru.practicum.android.diploma.presentation.vmodels.VacancyDetailViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

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
}
