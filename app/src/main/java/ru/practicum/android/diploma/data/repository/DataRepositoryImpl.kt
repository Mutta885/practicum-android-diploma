package ru.practicum.android.diploma.data.repository

import android.util.Log
import ru.practicum.android.diploma.data.dto.*
import ru.practicum.android.diploma.data.network.HhApi
import ru.practicum.android.diploma.domain.models.*
import ru.practicum.android.diploma.domain.repository.DataRepository
import ru.practicum.android.diploma.util.Resource
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class DataRepositoryImpl(
    private val api: HhApi
) : DataRepository {

    companion object {
        private const val HTTP_OK = 200
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500
        private const val TAG = "DataRepositoryImpl"
    }

    override suspend fun searchVacancies(
        query: String,
        page: Int,
        industry: String?,
        salary: Int?,
        onlyWithSalary: Boolean
    ): Resource<SearchResult> {
        println(
            "DEBUG: Repository search - query: '$query', page: $page, " +
                "industry: $industry, salary: $salary, onlyWithSalary: $onlyWithSalary"
        )

        return try {
            val response = api.searchVacancies(
                query = query,
                page = page,
                industry = industry,
                salary = salary,
                onlyWithSalary = onlyWithSalary
            )

            println("DEBUG: API response code: ${response.code()}, message: ${response.message()}")

            when (response.code()) {
                HTTP_OK -> handleSuccessResponse(response.body())
                HTTP_UNAUTHORIZED -> Resource.Error("Ошибка авторизации")
                HTTP_FORBIDDEN -> Resource.Error("Доступ запрещен")
                HTTP_NOT_FOUND -> Resource.Error("Ресурс не найден")
                HTTP_SERVER_ERROR -> Resource.Error("Ошибка сервера")
                else -> Resource.Error("Ошибка: ${response.code()} - ${response.message()}")
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error", e)
            Resource.Error("Проверьте подключение к интернету")
        } catch (e: SSLHandshakeException) {
            Log.w(TAG, "SSL handshake error", e)
            Resource.Error("Ошибка безопасности соединения")
        } catch (e: IOException) {
            Log.w(TAG, "IO error during network request", e)
            Resource.Error("Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error", e)
            Resource.Error("Неизвестная ошибка: ${e.message ?: "Попробуйте позже"}")
        }
    }

    override suspend fun getIndustries(): List<Industry> {
        println("DEBUG: Repository getIndustries called")
        return try {
            val response = api.getIndustries()
            println("DEBUG: Industries loaded: ${response.size} items")
            response.map { dto -> Industry(id = dto.id, name = dto.name) }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading industries", e)
            println("DEBUG: Error loading industries: ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchVacancyDetail(query: String): Resource<SearchResultVacancyDetail> {
        println("DEBUG: Repository searchVacancyDetail called for id: $query")
        return try {
            val response = api.searchVacancyDetail(query)
            println("DEBUG: Vacancy detail response code: ${response.code()}, message: ${response.message()}")

            when (response.code()) {
                HTTP_OK -> handleSuccessResponseVacancyDetail(response.body())
                HTTP_UNAUTHORIZED -> Resource.Error("Ошибка авторизации")
                HTTP_FORBIDDEN -> Resource.Error("Доступ запрещен")
                HTTP_NOT_FOUND -> Resource.Error("Вакансия не найдена")
                HTTP_SERVER_ERROR -> Resource.Error("Ошибка сервера")
                else -> Resource.Error("Ошибка: ${response.code()} - ${response.message()}")
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error", e)
            Resource.Error("Проверьте подключение к интернету")
        } catch (e: SSLHandshakeException) {
            Log.w(TAG, "SSL handshake error", e)
            Resource.Error("Ошибка безопасности соединения")
        } catch (e: IOException) {
            Log.w(TAG, "IO error during network request", e)
            Resource.Error("Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error", e)
            Resource.Error("Неизвестная ошибка: ${e.message ?: "Попробуйте позже"}")
        }
    }

    private fun handleSuccessResponse(body: VacancySearchResponse?): Resource<SearchResult> =
        if (body != null) {
            println(
                "DEBUG: Search response - found: ${body.found}, pages: ${body.pages}, " +
                    "page: ${body.page}, items: ${body.items.size}"
            )
            createSuccessResult(body, mapVacancies(body.items))
        } else {
            println("DEBUG: Empty search response body")
            Resource.Error("Пустой ответ от сервера")
        }

    private fun handleSuccessResponseVacancyDetail(
        body: VacancyDetailSearchResponse?
    ): Resource<SearchResultVacancyDetail> = if (body != null) {
        println("DEBUG: Vacancy detail loaded - id: ${body.id}, name: ${body.name}")
        createSuccessResultVacancyDetail(body)
    } else {
        println("DEBUG: Empty vacancy detail response body")
        Resource.Error("Пустой ответ от сервера")
    }

    private fun mapVacancies(items: List<VacancyDto>): List<Vacancy> =
        items.map { dto ->
            Vacancy(
                id = dto.id,
                title = dto.name,
                salary = mapSalary(dto.salary),
                employer = mapEmployer(dto.employer),
                area = mapArea(dto.area),
                description = dto.description ?: "Описание не указано"
            )
        }

    private fun mapSalary(salaryDto: SalaryDto?) = salaryDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.Salary(
            from = dto.from,
            to = dto.to,
            currency = dto.currency
        )
    }

    private fun mapEmployer(employerDto: EmployerDto?) = employerDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.Employer(
            id = dto.id,
            name = dto.name,
            logoUrl = dto.logo
        )
    }

    private fun mapArea(areaDto: AreaDto?) = areaDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.Area(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapIndustry(industryDto: FilterIndustryDto?) = industryDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.FilterIndustry(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapExperience(experienceDto: ExperienceDto?) = experienceDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.Experience(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapSchedule(scheduleDto: ScheduleDto?) = scheduleDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.Schedule(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapEmployment(employmentDto: EmploymentDto?) = employmentDto?.let { dto ->
        ru.practicum.android.diploma.domain.models.Employment(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapContacts(contactDto: ContactDto?) = contactDto?.let { dto ->
        Contact(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            phone = dto.phone
        )
    }

    private fun createSuccessResult(
        body: VacancySearchResponse,
        vacancies: List<Vacancy>
    ): Resource<SearchResult> =
        Resource.Success(
            SearchResult(
                found = body.found,
                pages = body.pages,
                page = body.page,
                vacancies = vacancies
            )
        )

    private fun createSuccessResultVacancyDetail(
        body: VacancyDetailSearchResponse
    ): Resource<SearchResultVacancyDetail> =
        Resource.Success(
            SearchResultVacancyDetail(
                id = body.id,
                name = body.name,
                description = body.description,
                salary = mapSalary(body.salary),
                employer = mapEmployer(body.employer),
                industry = mapIndustry(body.industry),
                area = mapArea(body.area),
                experience = mapExperience(body.experience),
                schedule = mapSchedule(body.schedule),
                employment = mapEmployment(body.employment),
                contact = mapContacts(body.contacts)
            )
        )
}
