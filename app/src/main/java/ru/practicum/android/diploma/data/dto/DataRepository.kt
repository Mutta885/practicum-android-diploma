package ru.practicum.android.diploma.data.dto

import ru.practicum.android.diploma.data.network.ApiService

class DataRepository(private val apiService: ApiService) {

    suspend fun getVacancies(): List<VacancyDto> {
        return apiService.getVacancies()
    }
}
