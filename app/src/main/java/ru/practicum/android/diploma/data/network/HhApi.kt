package ru.practicum.android.diploma.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.IndustryDto
import ru.practicum.android.diploma.data.dto.VacancyDetailSearchResponse
import ru.practicum.android.diploma.data.dto.VacancySearchResponse

interface HhApi {
    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("text") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20,
        @Query("industry") industry: String? = null,
        @Query("salary") salary: Int? = null,
        @Query("only_with_salary") onlyWithSalary: Boolean = false
    ): Response<VacancySearchResponse>

    @GET("industries")
    suspend fun getIndustries(): List<IndustryDto>

    @GET("vacancies/{id}")
    suspend fun searchVacancyDetail(
        @Path("id") query: String,
    ): Response<VacancyDetailSearchResponse>

    @GET("areas")
    suspend fun searchAreas(): List<FilterAreaDto>
}
