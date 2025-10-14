package ru.practicum.android.diploma.data.repository

import android.util.Log
import ru.practicum.android.diploma.data.dto.AreaDto
import ru.practicum.android.diploma.data.dto.ContactDto
import ru.practicum.android.diploma.data.dto.EmployerDto
import ru.practicum.android.diploma.data.dto.EmploymentDto
import ru.practicum.android.diploma.data.dto.ExperienceDto
import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.ScheduleDto
import ru.practicum.android.diploma.data.dto.VacancyDetailSearchResponse
import ru.practicum.android.diploma.data.dto.VacancyDto
import ru.practicum.android.diploma.data.dto.VacancySearchResponse
import ru.practicum.android.diploma.data.network.HhApi
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.Contact
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Employment
import ru.practicum.android.diploma.domain.models.Experience
import ru.practicum.android.diploma.domain.models.FilterIndustry
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Schedule
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.domain.models.SearchResultVacancyDetail
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.repository.DataRepository
import ru.practicum.android.diploma.util.Resource
import java.io.IOException
import java.net.SocketTimeoutException
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
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Request timeout", e)
            Resource.Error("Превышено время ожидания ответа")
        } catch (e: SSLHandshakeException) {
            Log.w(TAG, "SSL handshake error", e)
            Resource.Error("Ошибка безопасности соединения")
        } catch (e: IOException) {
            Log.w(TAG, "IO error during network request", e)
            Resource.Error("Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    override suspend fun getIndustries(): List<Industry> {
        println("DEBUG: Repository getIndustries called")
        return try {
            val response = api.getIndustries()
            println("DEBUG: Industries loaded: ${response.size} items")
            response.map { dto -> Industry(id = dto.id, name = dto.name) }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error loading industries", e)
            println("DEBUG: Network error loading industries: ${e.message}")
            emptyList()
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Timeout loading industries", e)
            println("DEBUG: Timeout loading industries: ${e.message}")
            emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "IO error loading industries", e)
            println("DEBUG: IO error loading industries: ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchVacancyDetail(query: String): Resource<SearchResultVacancyDetail> {
        println("DEBUG: Repository searchVacancyDetail called for id: $query")
        return try {
            val response = api.searchVacancyDetail(query)
            println("DEBUG: Vacancy detail response code: ${response.code()}, message: ${response.message()}")

            when (response.code()) {
                HTTP_OK -> {
                    handleSuccessResponseVacancyDetail(response.body())
                }
                HTTP_UNAUTHORIZED -> Resource.Error("Ошибка авторизации")
                HTTP_FORBIDDEN -> Resource.Error("Доступ запрещен")
                HTTP_NOT_FOUND -> Resource.Error("Вакансия не найдена")
                HTTP_SERVER_ERROR -> Resource.Error("Ошибка сервера")
                else -> Resource.Error("Ошибка: ${response.code()} - ${response.message()}")
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error", e)
            Resource.Error("Проверьте подключение к интернету")
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Request timeout", e)
            Resource.Error("Превышено время ожидания ответа")
        } catch (e: SSLHandshakeException) {
            Log.w(TAG, "SSL handshake error", e)
            Resource.Error("Ошибка безопасности соединения")
        } catch (e: IOException) {
            Log.w(TAG, "IO error during network request", e)
            Resource.Error("Ошибка сети: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    override suspend fun getAreas(): List<FilterArea> {
        println("DEBUG: Repository getIndustries called")
        return try {
            val response = api.searchAreas()
            println("DEBUG: areas loaded: ${response.size} items")
            response.map { dto -> FilterArea(id = dto.id, parentId = dto.parentId, name = dto.name, areas = mapAreas(dto.areas)) }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error loading industries", e)
            println("DEBUG: Network error loading industries: ${e.message}")
            emptyList()
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Timeout loading industries", e)
            println("DEBUG: Timeout loading industries: ${e.message}")
            emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "IO error loading industries", e)
            println("DEBUG: IO error loading industries: ${e.message}")
            emptyList()
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

    private fun mapAreas(items: List<FilterAreaDto>): List<FilterArea> {
        return items.map { dto ->
            FilterArea(
                id = dto.id,
                name = dto.name,
                parentId = dto.parentId,
                areas = mapAreas(dto.areas)
            )
        }
    }

    private fun mapSalary(salaryDto: SalaryDto?) = salaryDto?.let { dto ->
        Salary(
            from = dto.from,
            to = dto.to,
            currency = dto.currency
        )
    }

    private fun mapEmployer(employerDto: EmployerDto?) = employerDto?.let { dto ->
        Employer(
            id = dto.id,
            name = dto.name,
            logoUrl = dto.logo
        )
    }

    private fun mapArea(areaDto: AreaDto?) = areaDto?.let { dto ->
        Area(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapIndustry(industryDto: FilterIndustryDto?) = industryDto?.let { dto ->
        FilterIndustry(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapExperience(experienceDto: ExperienceDto?) = experienceDto?.let { dto ->
        Experience(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapSchedule(scheduleDto: ScheduleDto?) = scheduleDto?.let { dto ->
        Schedule(
            id = dto.id,
            name = dto.name
        )
    }

    private fun mapEmployment(employmentDto: EmploymentDto?) = employmentDto?.let { dto ->
        Employment(
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
