package ru.practicum.android.diploma.data.network

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.data.dto.IndustryDto
import ru.practicum.android.diploma.data.dto.Response

interface NetworkClient {
    suspend fun doRequest(dto: Any): Response
    fun doIndustry(): Flow<Result<List<IndustryDto>>>
}
