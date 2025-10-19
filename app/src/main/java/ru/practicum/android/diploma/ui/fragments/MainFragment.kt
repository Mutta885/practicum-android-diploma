package ru.practicum.android.diploma.ui.fragments

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
import ru.practicum.android.diploma.presentation.adapters.VacanciesAdapter
import ru.practicum.android.diploma.presentation.vmodels.FiltrationViewModel
import ru.practicum.android.diploma.presentation.vmodels.SearchState
import ru.practicum.android.diploma.presentation.vmodels.SearchViewModel
import ru.practicum.android.diploma.util.showToast

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by sharedViewModel()
    private val filtrationViewModel: FiltrationViewModel by activityViewModel()
    private val adapter: VacanciesAdapter by lazy {
        VacanciesAdapter(onItemClick = { vacancy -> onVacancyClick(vacancy) })
    }

    private var isProgrammaticTextChange = false
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
        setupBackButtonHandler(view)
    }

    override fun onResume() {
        super.onResume()
        updateSearchFieldIcons(binding.searchEditText.text.toString())
        isReturningFromFilters = false
    }

    private fun setupRecyclerView() {
        binding.vacanciesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.vacanciesRecyclerView.adapter = adapter
        binding.vacanciesRecyclerView.addOnScrollListener(createScrollListener())
    }

    private fun createScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
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
        }
    }

    private fun setupSearchField() {
        val editText = binding.searchEditText
        updateSearchFieldIcons(editText.text.toString())
        editText.addTextChangedListener(createTextWatcher())
        setupSearchIconsListeners()
        setupEditorActionListener(editText)
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                handleTextChange(s)
            }
        }
    }

    private fun handleTextChange(s: Editable?) {
        if (isProgrammaticTextChange) return
        if (isReturningFromFilters) {
            isReturningFromFilters = false
            return
        }
        val text = s.toString().trim()
        updateSearchFieldIcons(text)
        searchViewModel.search(text)
    }

    private fun setupSearchIconsListeners() {
        binding.searchIcon.setOnClickListener {
            searchViewModel.search(binding.searchEditText.text.toString().trim())
        }

        binding.clearIcon.setOnClickListener {
            clearSearchField()
        }
    }

    private fun clearSearchField() {
        isProgrammaticTextChange = true
        binding.searchEditText.text.clear()
        isProgrammaticTextChange = false
        searchViewModel.search("")
        updateSearchFieldIcons("")
    }

    private fun setupEditorActionListener(editText: android.widget.EditText) {
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
        val isTextNotEmpty = text.isNotEmpty()
        binding.searchIcon.visibility = if (isTextNotEmpty) View.GONE else View.VISIBLE
        binding.clearIcon.visibility = if (isTextNotEmpty) View.VISIBLE else View.GONE
    }

    private fun setupClickListeners() {
        binding.trailingButton.setOnClickListener {
            navigateToFilters()
        }
    }

    private fun navigateToFilters() {
        isReturningFromFilters = true
        findNavController().navigate(R.id.action_mainFragment_to_filtrationFragment)
    }

    private fun setupBackButtonHandler(view: View) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            handleBackButton(keyCode, event)
        }
    }

    private fun handleBackButton(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            findNavController().popBackStack()
            return true
        }
        return false
    }

    private fun observeViewModel() {
        searchViewModel.searchState.observe(viewLifecycleOwner) { state ->
            handleSearchState(state)
        }
    }

    private fun handleSearchState(state: SearchState) {
        when (state) {
            is SearchState.EmptyQuery -> showEmptyQueryState()
            is SearchState.Loading -> showLoadingState()
            is SearchState.Success -> showSuccessState(state)
            is SearchState.Error -> state.message?.let { showErrorState(it) }
            is SearchState.LoadingNextPage -> adapter.setLoading(true)
            is SearchState.NextPageError -> state.message?.let { requireContext().showToast(it) }
            is SearchState.FiltersApplied -> Unit // Просто обновляем состояние без дополнительных действий
        }
    }

    private fun observeFilterState() {
        filtrationViewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            val iconRes = if (isActive) R.drawable.trailing_icon_2 else R.drawable.trailing_icon
            binding.trailingButton.setImageResource(iconRes)
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
        val hasActiveFilters = hasActiveFilters(currentFilters)

        if (hasActiveFilters && currentQuery.isNotEmpty()) {
            applyFiltersWithQuery(currentQuery, currentFilters)
        } else if (hasActiveFilters) {
            searchViewModel.setFiltersWithoutSearch(currentFilters)
        }
    }

    private fun hasActiveFilters(filters: FiltrationViewModel.Filters): Boolean {
        return filters.salary != null ||
            filters.hideWithoutSalary ||
            filters.industries.isNotEmpty() ||
            filters.country != null ||
            filters.region != null
    }

    private fun applyFiltersWithQuery(query: String, filters: FiltrationViewModel.Filters) {
        searchViewModel.setFiltersWithoutSearch(filters)
        isProgrammaticTextChange = true
        binding.searchEditText.setText(query)
        updateSearchFieldIcons(query)
        isProgrammaticTextChange = false
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
