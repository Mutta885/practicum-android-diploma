package ru.practicum.android.diploma.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.data.dto.AreaDto
import ru.practicum.android.diploma.data.dto.ContactDto
import ru.practicum.android.diploma.data.dto.EmployerDto
import ru.practicum.android.diploma.data.dto.EmploymentDto
import ru.practicum.android.diploma.data.dto.ExperienceDto
import ru.practicum.android.diploma.data.dto.toDomain
import ru.practicum.android.diploma.data.dto.FilterIndustryDto
import ru.practicum.android.diploma.data.dto.IndustryRequest
import ru.practicum.android.diploma.data.dto.IndustryResponse
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.ScheduleDto
import ru.practicum.android.diploma.data.dto.VacancyDetailSearchResponse
import ru.practicum.android.diploma.data.dto.VacancyDto
import ru.practicum.android.diploma.data.dto.VacancySearchResponse
import ru.practicum.android.diploma.data.network.HhApi
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.network.converters.ConvertersDto
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.Contact
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Employment
import ru.practicum.android.diploma.domain.models.Experience
import ru.practicum.android.diploma.domain.models.FilterIndustry
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.models.Phones
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Schedule
import ru.practicum.android.diploma.domain.models.SearchResult
import ru.practicum.android.diploma.domain.models.SearchResultVacancyDetail
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.isCountry
import ru.practicum.android.diploma.domain.api.DataRepository
import ru.practicum.android.diploma.util.Resource
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class DataRepositoryImpl(
    private val api: HhApi,
    private val networkClient: NetworkClient,
    private val converters: ConvertersDto
) : DataRepository {

    companion object {
        private const val HTTP_OK = 200
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500
        private const val TAG = "DataRepositoryImpl"
    }

    // ОБНОВЛЕНО: Добавлен параметр area
    override suspend fun searchVacancies(
        query: String,
        page: Int,
        industry: String?,
        salary: Int?,
        onlyWithSalary: Boolean,
        area: String? // ДОБАВЛЕНО
    ): Resource<SearchResult> {
        println(
            "DEBUG: Repository search - query: '$query', page: $page, " +
                "industry: $industry, salary: $salary, onlyWithSalary: $onlyWithSalary, area: $area"
        )

        return try {
            val response = api.searchVacancies(
                query = query,
                page = page,
                industry = industry,
                salary = salary,
                onlyWithSalary = onlyWithSalary,
                area = area
            )

            println("DEBUG: API response code: ${response.code()}, message: ${response.message()}")

            when (response.code()) {
                HTTP_OK -> handleSuccessResponse(response.body())
                HTTP_UNAUTHORIZED -> Resource.Error("Ошибка авторизации")
                HTTP_FORBIDDEN -> Resource.Error("Доступ запрещен")
                HTTP_NOT_FOUND -> Resource.Error("Ресурс не найдена")
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

    // Остальные методы остаются без изменений...
    override fun getIndustries(): Flow<Result<List<Industry>?>> {
        return flow {
            networkClient.doRequest(IndustryRequest()).collect { result ->
                if (result.resultCode == HTTP_OK) {
                    emit(
                        Result.success(
                            (result as IndustryResponse).result.map {
                                converters.map(it)
                            }
                        )
                    )
                } else {
                    emit(Result.failure(Throwable(result.resultCode.toString())))
                }
            }
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
                HTTP_NOT_FOUND -> Resource.Error("Вакансия не найдена или удалена")
                HTTP_SERVER_ERROR -> Resource.Error("Ошибка сервера")
                else -> Resource.Error("Ошибка: ${response.code()} - ${response.message()}")
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error", e)
            Resource.Error("Нет интернета")
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

    override suspend fun getAreas(): Resource<List<FilterArea>> {
        println("DEBUG: Repository getAreas called")
        return try {
            val response = api.searchAreas()
            println("DEBUG: Areas API response received: ${response.size} items")

            if (response.isNotEmpty()) {
                val areas = response.map { it.toDomain() }
                Resource.Success(areas)
            } else {
                Resource.Error("Пустой список регионов")
            }
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network connection error loading areas", e)
            println("DEBUG: Network error loading areas: ${e.message}")
            Resource.Error("Проверьте подключение к интернету")
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Timeout loading areas", e)
            println("DEBUG: Timeout loading areas: ${e.message}")
            Resource.Error("Превышено время ожидания")
        } catch (e: IOException) {
            Log.w(TAG, "IO error loading areas", e)
            println("DEBUG: IO error loading areas: ${e.message}")
            Resource.Error("Ошибка загрузки данных: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    override suspend fun getCountries(): Resource<List<FilterArea>> {
        println("DEBUG: Repository getCountries called")
        return when (val areasResult = getAreas()) {
            is Resource.Success -> {
                val countries = areasResult.data.filter { it.isCountry() }
                println("DEBUG: Found ${countries.size} countries")
                Resource.Success(countries)
            }

            is Resource.Error -> areasResult
            Resource.Loading -> TODO()
        }
    }

    override suspend fun getRegionsByCountry(countryId: Int): Resource<List<FilterArea>> {
        println("DEBUG: Repository getRegionsByCountry called for country: $countryId")
        return when (val areasResult = getAreas()) {
            is Resource.Success -> {
                val country = areasResult.data.find { it.id == countryId }
                if (country != null) {
                    // Исправляем регионы с null parentId
                    val regions = country.areas.map { region ->
                        if (region.parentId == null) {
                            FilterArea(
                                id = region.id,
                                name = region.name,
                                parentId = countryId,
                                areas = region.areas
                            )
                        } else {
                            region
                        }
                    }
                    println("DEBUG: Found ${regions.size} regions for country $countryId")
                    Resource.Success(regions)
                } else {
                    println("DEBUG: Country $countryId not found")
                    Resource.Error("Страна не найдена")
                }
            }

            is Resource.Error -> areasResult
            else -> Resource.Error("Неизвестная ошибка")
        }
    }

    override suspend fun getAllRegions(): Resource<List<FilterArea>> {
        println("DEBUG: Repository getAllRegions called")
        return when (val areasResult = getAreas()) {
            is Resource.Success -> {
                val allRegions = areasResult.data.flatMap { country ->
                    country.areas.filter { it.areas.isEmpty() }
                }
                println("DEBUG: Found ${allRegions.size} total regions")
                Resource.Success(allRegions)
            }

            is Resource.Error -> areasResult
            Resource.Loading -> TODO()
        }
    }

    override suspend fun getCountryById(countryId: Int): Resource<FilterArea?> {
        println("DEBUG: Repository getCountryById called for: $countryId")
        return when (val areasResult = getAreas()) {
            is Resource.Success -> {
                val country = areasResult.data.find { it.id == countryId }
                Resource.Success(country)
            }

            is Resource.Error -> areasResult
            Resource.Loading -> TODO()
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

    private fun mapFilterIndustry(industryDto: FilterIndustryDto?) = industryDto?.let { dto ->
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
            phones = dto.phones?.map {
                Phones(
                    comment = it.comment,
                    formatted = it.formatted
                )
            }
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
                industry = mapFilterIndustry(body.industry),
                area = mapArea(body.area),
                experience = mapExperience(body.experience),
                schedule = mapSchedule(body.schedule),
                employment = mapEmployment(body.employment),
                contact = mapContacts(body.contacts)
            )
        )
}
