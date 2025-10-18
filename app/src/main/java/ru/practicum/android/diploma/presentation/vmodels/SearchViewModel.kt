package ru.practicum.android.diploma.presentation.vmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.usecaseimpl.SearchVacanciesUseCase
import ru.practicum.android.diploma.util.Resource

class SearchViewModel(
    private val searchVacanciesUseCase: SearchVacanciesUseCase
) : ViewModel() {

    companion object {
        private const val DEBOUNCE_PERIOD = 2000L
    }

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var currentQuery = ""
    private var currentPage = 0
    private var totalPages = 1
    private var isLoading = false
    private var isLoadingNextPage = false
    private var searchJob: Job? = null
    private var currentFilters: FiltrationViewModel.Filters = FiltrationViewModel.Filters()
    private val debouncePeriod = DEBOUNCE_PERIOD
    private val _allVacancies = mutableListOf<Vacancy>()
    val allVacancies: List<Vacancy> get() = _allVacancies

    fun setFiltersWithoutSearch(filters: FiltrationViewModel.Filters) {
        println("DEBUG: setFiltersWithoutSearch() called with: $filters")
        println("DEBUG: Current query: '$currentQuery'")
        val filtersChanged = currentFilters != filters
        currentFilters = filters
        println("DEBUG: Filters updated without search: $currentFilters, changed: $filtersChanged")
        if (filtersChanged) {
            _searchState.value = SearchState.FiltersApplied(filters)
        }
    }

    fun setFilters(filters: FiltrationViewModel.Filters) {
        println("DEBUG: setFilters() called with: $filters")
        println("DEBUG: Current query: '$currentQuery'")
        val filtersChanged = currentFilters != filters
        currentFilters = filters
        println("DEBUG: Filters updated: $currentFilters, changed: $filtersChanged")

        // Автоматически выполняем поиск только если есть активный запрос
        if (currentQuery.isNotEmpty() && filtersChanged) {
            println("DEBUG: Auto-searching with new filters and query: '$currentQuery'")
            _allVacancies.clear()
            currentPage = 0
            totalPages = 1
            _searchState.value = SearchState.Loading
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                performSearch(query = currentQuery, page = 0, isNewSearch = true)
            }
        } else {
            println("DEBUG: No auto-search - no active query or filters not changed")
            // Просто обновляем состояние фильтров без поиска
            if (filtersChanged) {
                _searchState.value = SearchState.FiltersApplied(filters)
            }
        }
    }

    // НОВЫЙ МЕТОД: Применяем фильтры и ВСЕГДА выполняем поиск (для кнопки "Применить")
    fun applyFiltersWithSearch(filters: FiltrationViewModel.Filters) {
        println("DEBUG: applyFiltersWithSearch() called with: $filters")
        println("DEBUG: Current query: '$currentQuery'")
        val filtersChanged = currentFilters != filters
        currentFilters = filters
        println("DEBUG: Filters updated with search: $currentFilters, changed: $filtersChanged")

        // Всегда выполняем поиск при явном применении фильтров
        if (currentQuery.isNotEmpty()) {
            println("DEBUG: Performing search with applied filters and query: '$currentQuery'")
            _allVacancies.clear()
            currentPage = 0
            totalPages = 1
            _searchState.value = SearchState.Loading
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                performSearch(query = currentQuery, page = 0, isNewSearch = true)
            }
        } else {
            println("DEBUG: No search - query is empty, but filters saved")
            if (filtersChanged) {
                _searchState.value = SearchState.FiltersApplied(filters)
            }
        }
    }

    fun search(query: String) {
        val trimmedQuery = query.trim()
        println("DEBUG: search() called with: '$trimmedQuery'")
        println("DEBUG: Current filters: $currentFilters")

        if (trimmedQuery.isEmpty()) {
            _allVacancies.clear()
            _searchState.value = SearchState.EmptyQuery
            return
        }

        _allVacancies.clear()
        currentPage = 0
        totalPages = 1
        currentQuery = trimmedQuery
        _searchState.value = SearchState.Loading
        println("DEBUG: New search query: '$currentQuery', cleared vacancies")

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(debouncePeriod)
            performSearch(query = currentQuery, page = 0, isNewSearch = true)
        }
    }

    fun loadNextPage() {
        val canLoadNextPage = !isLoading &&
            !isLoadingNextPage &&
            hasMorePages() &&
            currentQuery.isNotEmpty()

        if (canLoadNextPage) {
            val nextPage = currentPage + 1
            println(
                "DEBUG: Loading next page: current=$currentPage, " +
                    "next=$nextPage, allVacancies size=${_allVacancies.size}"
            )
            performSearch(query = currentQuery, page = nextPage, isNewSearch = false)
        } else {
            println(
                "DEBUG: Cannot load next page - isLoading=$isLoading, " +
                    "isLoadingNextPage=$isLoadingNextPage, " +
                    "hasMorePages=${hasMorePages()}, " +
                    "queryEmpty=${currentQuery.isEmpty()}"
            )
        }
    }

    fun retry() {
        if (currentQuery.isNotEmpty()) {
            if (currentPage == 0) {
                performSearch(query = currentQuery, page = 0, isNewSearch = true)
            } else {
                performSearch(query = currentQuery, page = currentPage, isNewSearch = false)
            }
        }
    }

    fun clearSearch() {
        currentQuery = ""
        _allVacancies.clear()
        currentPage = 0
        totalPages = 1
        _searchState.value = SearchState.EmptyQuery
        println("DEBUG: Search cleared")
    }

    fun getCurrentFilters(): FiltrationViewModel.Filters {
        return currentFilters
    }

    fun getCurrentQuery(): String {
        return currentQuery
    }

    fun isLoading(): Boolean {
        return isLoading || isLoadingNextPage
    }

    private fun performSearch(query: String, page: Int, isNewSearch: Boolean) {
        if (isLoading || isLoadingNextPage) {
            println(
                "DEBUG: Search already in progress - " +
                    "isLoading=$isLoading, isLoadingNextPage=$isLoadingNextPage"
            )
            return
        }

        setLoadingStates(isNewSearch)
        viewModelScope.launch {
            println("DEBUG: Performing search with filters: $currentFilters")
            when (val result = searchVacanciesUseCase.execute(query, page, currentFilters)) {
                is Resource.Success -> handleSearchSuccess(result, isNewSearch)
                is Resource.Error -> handleSearchError(result, isNewSearch)
                is Resource.Loading -> handleSearchLoading()
            }
        }
    }

    private fun setLoadingStates(isNewSearch: Boolean) {
        if (isNewSearch) {
            isLoading = true
            _searchState.value = SearchState.Loading
            println(
                "DEBUG: Starting new search - query='$currentQuery', " +
                    "page=$currentPage, filters=$currentFilters"
            )
        } else {
            isLoadingNextPage = true
            _searchState.value = SearchState.LoadingNextPage
            println(
                "DEBUG: Starting next page search - query='$currentQuery', " +
                    "page=$currentPage, filters=$currentFilters"
            )
        }
    }

    private fun handleSearchSuccess(result: Resource.Success<*>, isNewSearch: Boolean) {
        val searchResult = result.data as ru.practicum.android.diploma.domain.models.SearchResult
        totalPages = searchResult.pages
        currentPage = searchResult.page

        println(
            "DEBUG: Search success - query='$currentQuery', " +
                "requestedPage=$currentPage, returnedPage=${searchResult.page}, " +
                "vacancies=${searchResult.vacancies.size}, totalPages=$totalPages, " +
                "found=${searchResult.found}, filters=$currentFilters"
        )

        if (isNewSearch) {
            handleNewSearchResults(searchResult)
        } else {
            handleNextPageResults(searchResult)
        }

        println(
            "DEBUG: Final state - currentPage=$currentPage, " +
                "totalPages=$totalPages, hasMorePages=${hasMorePages()}"
        )
    }

    private fun handleNewSearchResults(
        searchResult: ru.practicum.android.diploma.domain.models.SearchResult
    ) {
        _allVacancies.clear()
        _allVacancies.addAll(searchResult.vacancies)
        println("DEBUG: New search completed - allVacancies size: ${_allVacancies.size}")
        _searchState.value = SearchState.Success(
            vacancies = _allVacancies.toList(),
            found = searchResult.found,
            isFirstPage = true
        )
        isLoading = false
    }

    private fun handleNextPageResults(
        searchResult: ru.practicum.android.diploma.domain.models.SearchResult
    ) {
        val newVacancies = searchResult.vacancies
        println("DEBUG: Using all new vacancies: ${newVacancies.size}")
        _allVacancies.addAll(newVacancies)
        println("DEBUG: After adding - allVacancies size: ${_allVacancies.size}")
        _searchState.value = SearchState.Success(
            vacancies = _allVacancies.toList(),
            found = searchResult.found,
            isFirstPage = false
        )
        isLoadingNextPage = false
    }

    private fun handleSearchError(result: Resource.Error, isNewSearch: Boolean) {
        if (isNewSearch) {
            _searchState.value = SearchState.Error(result.message)
            isLoading = false
        } else {
            _searchState.value = SearchState.NextPageError(result.message)
            isLoadingNextPage = false
        }
        println("DEBUG: Search error - page=$currentPage, error=${result.message}")
    }

    private fun handleSearchLoading() {
        println("DEBUG: Search loading - page=$currentPage")
    }

    fun hasMorePages(): Boolean {
        val hasMore = currentPage < totalPages - 1
        println(
            "DEBUG: hasMorePages check - currentPage=$currentPage, " +
                "totalPages=$totalPages, result=$hasMore"
        )
        return hasMore
    }
}

sealed class SearchState {
    object EmptyQuery : SearchState()
    object Loading : SearchState()
    object LoadingNextPage : SearchState()
    data class Success(
        val vacancies: List<Vacancy>,
        val found: Int,
        val isFirstPage: Boolean
    ) : SearchState()
    data class Error(val message: String?) : SearchState()
    data class NextPageError(val message: String?) : SearchState()
    data class FiltersApplied(val filters: FiltrationViewModel.Filters) : SearchState()
}
