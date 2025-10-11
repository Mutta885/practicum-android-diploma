package ru.practicum.android.diploma.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import ru.practicum.android.diploma.data.dto.VacancyDetailSearchResponse
import ru.practicum.android.diploma.data.dto.VacancySearchResponse

interface HhApi {
    @GET("vacancies")
    suspend fun searchVacancies(
        @Query("text") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20
    ): Response<VacancySearchResponse>

    @GET("vacancies/{id}")
    suspend fun searchVacancyDetail(
        @Path("id") query: String,
    ): Response<VacancyDetailSearchResponse>

    @GET("vacancies")
    suspend fun searchVacancyWithFilter(
        @QueryMap options: Map<String, String> = hashMapOf(Pair("only_with_salary", "true"))
    ): Response<VacancySearchResponse>
}

