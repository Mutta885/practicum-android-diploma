package ru.practicum.android.diploma.presentation.models

import ru.practicum.android.diploma.domain.models.VacancyDetail

sealed class VacancyDetailState {
    data class Success(val vacancyDetail: VacancyDetail) : VacancyDetailState()
    data object Loading : VacancyDetailState()
    data class Error(val message: String) : VacancyDetailState()
}
