package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentMainBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.ui.search.SearchState
import ru.practicum.android.diploma.ui.search.SearchViewModel
import ru.practicum.android.diploma.ui.search.VacanciesAdapter
import ru.practicum.android.diploma.util.showToast

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by sharedViewModel()
    private val filtrationViewModel: FiltrationViewModel by activityViewModel()
    private val adapter: VacanciesAdapter by lazy {
        VacanciesAdapter(onItemClick = { vacancy -> onVacancyClick(vacancy) })
    }

    // Флаг для блокировки автоматического поиска при программном изменении текста
    private var isProgrammaticTextChange = false
    // Флаг для блокировки поиска при возврате из фильтров
    private var isReturningFromFilters = false

    private companion object {
        const val LOAD_MORE_THRESHOLD = 3
        const val DELAY_FOR_FILTERS = 500L
        private const val DEBUG_TAG = "MainFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchField()
        setupClickListeners()
        observeViewModel()
        observeFilterState()
        applySavedFiltersOnStart()

        // Обработка системной кнопки назад
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                findNavController().popBackStack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем состояние иконок при возобновлении фрагмента
        val currentText = binding.searchEditText.text.toString()
        updateSearchFieldIcons(currentText)
        isReturningFromFilters = false
    }

    private fun setupRecyclerView() {
        binding.vacanciesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.vacanciesRecyclerView.adapter = adapter
        binding.vacanciesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && !searchViewModel.isLoading() && searchViewModel.hasMorePages()) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastVisibleItemPosition >= totalItemCount - LOAD_MORE_THRESHOLD) {
                        searchViewModel.loadNextPage()
                    }
                }
            }
        })
    }

    private fun setupSearchField() {
        val editText = binding.searchEditText
        val searchIcon = binding.searchIcon
        val clearIcon = binding.clearIcon

        // Инициализируем иконки при создании
        updateSearchFieldIcons(editText.text.toString())

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                // Если изменение текста программное - не запускаем поиск
                if (isProgrammaticTextChange) {
                    return
                }

                // Если возвращаемся из фильтров - не запускаем поиск
                if (isReturningFromFilters) {
                    isReturningFromFilters = false
                    return
                }

                val text = s.toString().trim()
                updateSearchFieldIcons(text)
                searchViewModel.search(text)
            }
        })

        searchIcon.setOnClickListener {
            searchViewModel.search(editText.text.toString().trim())
        }

        clearIcon.setOnClickListener {
            isProgrammaticTextChange = true
            editText.text.clear()
            isProgrammaticTextChange = false
            searchViewModel.search("")
            updateSearchFieldIcons("")
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchViewModel.search(editText.text.toString().trim())
                true
            } else {
                false
            }
        }
    }

    private fun updateSearchFieldIcons(text: String) {
        val searchIcon = binding.searchIcon
        val clearIcon = binding.clearIcon

        if (text.isNotEmpty()) {
            searchIcon.visibility = View.GONE
            clearIcon.visibility = View.VISIBLE
        } else {
            searchIcon.visibility = View.VISIBLE
            clearIcon.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.trailingButton.setOnClickListener {
            // Устанавливаем флаг, что переходим в фильтры
            isReturningFromFilters = true
            findNavController().navigate(R.id.action_mainFragment_to_filtrationFragment)
        }
    }

    private fun observeViewModel() {
        searchViewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.EmptyQuery -> showEmptyQueryState()
                is SearchState.Loading -> showLoadingState()
                is SearchState.Success -> showSuccessState(state)
                is SearchState.Error -> state.message?.let { showErrorState(it) }
                is SearchState.LoadingNextPage -> adapter.setLoading(true)
                is SearchState.NextPageError -> state.message?.let { requireContext().showToast(it) }
                is SearchState.FiltersApplied -> {
                    // Просто обновляем состояние без дополнительных действий
                }
            }
        }
    }

    private fun observeFilterState() {
        filtrationViewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            binding.trailingButton.setImageResource(
                if (isActive) R.drawable.trailing_icon_2 else R.drawable.trailing_icon
            )
        }
    }

    private fun applySavedFiltersOnStart() {
        lifecycleScope.launch {
            delay(DELAY_FOR_FILTERS)
            handleSavedFilters()
        }
    }

    private suspend fun handleSavedFilters() {
        val filtersJustApplied = filtrationViewModel.filtersJustApplied.value == true
        if (!filtersJustApplied) {
            applySavedFiltersOnAppStart()
        } else {
            filtrationViewModel.setFiltersJustApplied(false)
        }
    }

    private fun applySavedFiltersOnAppStart() {
        val currentQuery = searchViewModel.getCurrentQuery()
        val currentFilters = filtrationViewModel.getCurrentFilters()
        val hasActiveFilters = currentFilters.salary != null ||
            currentFilters.hideWithoutSalary ||
            currentFilters.industries.isNotEmpty() ||
            currentFilters.country != null ||
            currentFilters.region != null

        println("DEBUG_MAIN: applySavedFiltersOnAppStart - query: '$currentQuery', hasActiveFilters: $hasActiveFilters")

        // Применяем фильтры только если есть активный запрос
        if (hasActiveFilters && currentQuery.isNotEmpty()) {
            println("DEBUG_MAIN: Calling setFiltersWithoutSearch with active query")
            // Используем setFiltersWithoutSearch чтобы не вызывать лишний поиск
            searchViewModel.setFiltersWithoutSearch(currentFilters)

            // Устанавливаем текст БЕЗ вызова поиска
            isProgrammaticTextChange = true
            binding.searchEditText.setText(currentQuery)
            // Обновляем состояние иконок после установки текста
            updateSearchFieldIcons(currentQuery)
            isProgrammaticTextChange = false
        } else if (hasActiveFilters) {
            println("DEBUG_MAIN: Calling setFiltersWithoutSearch without query")
            // Если есть фильтры, но нет запроса - просто сохраняем их состояние
            searchViewModel.setFiltersWithoutSearch(currentFilters)
        } else {
            println("DEBUG_MAIN: No filters to apply")
        }
    }

    private fun showEmptyQueryState() {
        with(binding) {
            loadingProgressBar.isVisible = false
            vacanciesRecyclerView.isVisible = false
            errorStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            resultsCountText.isVisible = false
            emptyStateContainer.isVisible = true
        }
        adapter.submitVacancies(emptyList())
    }

    private fun showLoadingState() {
        with(binding) {
            loadingProgressBar.isVisible = true
            vacanciesRecyclerView.isVisible = false
            errorStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            emptyStateContainer.isVisible = false
            resultsCountText.isVisible = false
        }
    }

    private fun showSuccessState(state: SearchState.Success) {
        with(binding) {
            loadingProgressBar.isVisible = false
            errorStateContainer.isVisible = false
            emptyStateContainer.isVisible = false

            if (state.vacancies.isEmpty()) {
                vacanciesRecyclerView.isVisible = false
                noResultsContainer.isVisible = true
            } else {
                vacanciesRecyclerView.isVisible = true
                noResultsContainer.isVisible = false
                showResultsCount(state.found, state.vacancies.size)
            }
        }
        adapter.submitVacancies(state.vacancies)
        adapter.setHasMore(searchViewModel.hasMorePages())
    }

    private fun showErrorState(message: String) {
        with(binding) {
            loadingProgressBar.isVisible = false
            vacanciesRecyclerView.isVisible = false
            emptyStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            errorStateContainer.isVisible = true
            errorStateText.text = message
            resultsCountText.isVisible = false
        }
    }

    private fun showResultsCount(totalFound: Int, displayed: Int) {
        with(binding.resultsCountText) {
            isVisible = totalFound > 0
            text = "Найдено $totalFound вакансий"
        }
    }

    private fun onVacancyClick(vacancy: Vacancy) {
        requireContext().showToast("Открываем вакансию: ${vacancy.title}")
        val bundle = Bundle().apply { putString("vacancyId", vacancy.id) }
        findNavController().navigate(R.id.action_mainFragment_to_vacancyDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
