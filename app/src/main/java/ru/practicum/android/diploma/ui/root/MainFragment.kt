package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private companion object {
        const val LOAD_MORE_THRESHOLD = 3
        const val DELAY_FOR_FILTERS = 500L
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

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()

                if (text.isNotEmpty()) {
                    searchIcon.visibility = View.GONE
                    clearIcon.visibility = View.VISIBLE
                } else {
                    searchIcon.visibility = View.VISIBLE
                    clearIcon.visibility = View.GONE
                }

                searchViewModel.search(text)
            }
        })

        searchIcon.setOnClickListener {
            val query = editText.text.toString().trim()
            searchViewModel.search(query)
        }

        clearIcon.setOnClickListener {
            editText.text.clear()
            searchViewModel.search("")
            clearIcon.visibility = View.GONE
            searchIcon.visibility = View.VISIBLE
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editText.text.toString().trim()
                searchViewModel.search(query)
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        binding.trailingButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_filtrationFragment)
        }
    }

    private fun observeViewModel() {
        searchViewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.EmptyQuery -> showEmptyQueryState()
                is SearchState.Loading -> showLoadingState()
                is SearchState.Success -> {
                    showSuccessState(state)
                    adapter.submitVacancies(state.vacancies)
                    adapter.setLoading(false)
                    adapter.setHasMore(searchViewModel.hasMorePages())
                }

                is SearchState.Error -> state.message?.let { showErrorState(it) }
                is SearchState.LoadingNextPage -> {
                    adapter.setLoading(true)
                    adapter.setHasMore(true)
                }

                is SearchState.NextPageError -> {
                    adapter.setLoading(false)
                    state.message?.let { requireContext().showToast(it) }
                }

                is SearchState.FiltersApplied -> {
                    val hasActiveFilters = state.filters.salary != null ||
                        state.filters.hideWithoutSalary ||
                        state.filters.industries.isNotEmpty()

                    if (hasActiveFilters) {
                        requireContext().showToast("Фильтры восстановлены")
                    }
                }

                else -> {}
            }
        }
    }

    private fun observeFilterState() {
        filtrationViewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            if (isActive) {
                binding.trailingButton.setImageResource(R.drawable.trailing_icon_2)
            } else {
                binding.trailingButton.setImageResource(R.drawable.trailing_icon)
            }
        }
    }

    private fun applySavedFiltersOnStart() {
        lifecycleScope.launch {
            delay(DELAY_FOR_FILTERS)

            // Проверяем, не были ли фильтры только что применены из FiltrationFragment
            val filtersJustApplied = filtrationViewModel.filtersJustApplied.value == true

            if (filtersJustApplied) {
                // Сбрасываем флаг и НЕ применяем фильтры повторно
                filtrationViewModel.setFiltersJustApplied(false)
                println("DEBUG: Filters were just applied - skipping auto-application in MainFragment")
                return@launch
            }

            val currentQuery = searchViewModel.getCurrentQuery()
            val currentFilters = filtrationViewModel.getCurrentFilters()

            val hasActiveFilters = currentFilters.salary != null ||
                currentFilters.hideWithoutSalary ||
                currentFilters.industries.isNotEmpty() ||
                currentFilters.country != null ||
                currentFilters.region != null

            if (hasActiveFilters) {
                println("DEBUG: MainFragment applying saved filters on start")
                searchViewModel.setFilters(currentFilters)

                if (currentQuery.isNotEmpty()) {
                    binding.searchEditText.setText(currentQuery)
                }
            }
        }
    }

    private fun showEmptyQueryState() {
        binding.apply {
            loadingProgressBar.isVisible = false
            vacanciesRecyclerView.isVisible = false
            errorStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            resultsCountText.isVisible = false
            emptyStateContainer.isVisible = true
        }
        adapter.submitVacancies(emptyList())
        adapter.setLoading(false)
        adapter.setHasMore(false)
    }

    private fun showLoadingState() {
        binding.apply {
            loadingProgressBar.isVisible = true
            vacanciesRecyclerView.isVisible = false
            errorStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            emptyStateContainer.isVisible = false
            resultsCountText.isVisible = false
        }
        adapter.setLoading(false)
        adapter.setHasMore(false)
    }

    private fun showSuccessState(state: SearchState.Success) {
        binding.apply {
            loadingProgressBar.isVisible = false
            errorStateContainer.isVisible = false
            emptyStateContainer.isVisible = false

            if (state.vacancies.isEmpty()) {
                vacanciesRecyclerView.isVisible = false
                noResultsContainer.isVisible = true
                resultsCountText.isVisible = false
            } else {
                vacanciesRecyclerView.isVisible = true
                noResultsContainer.isVisible = false
                showResultsCount(state.found, state.vacancies.size)
            }
        }

        adapter.submitVacancies(state.vacancies)
        adapter.setLoading(false)
        adapter.setHasMore(searchViewModel.hasMorePages())

        if (state.vacancies.isNotEmpty() && state.isFirstPage) {
            requireContext().showToast("Найдено вакансий: ${state.found}")
        }
    }

    private fun showErrorState(message: String) {
        binding.apply {
            loadingProgressBar.isVisible = false
            vacanciesRecyclerView.isVisible = false
            emptyStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            errorStateContainer.isVisible = true
            errorStateText.text = "Нет интернета"
            resultsCountText.isVisible = false
        }
        adapter.setLoading(false)
        adapter.setHasMore(false)
    }

    private fun showResultsCount(totalFound: Int, displayed: Int) {
        binding.resultsCountText.isVisible = totalFound > 0
        if (totalFound > 0) {
            binding.resultsCountText.text = "Найдено $totalFound вакансий"
        }
    }

    private fun onVacancyClick(vacancy: Vacancy) {
        requireContext().showToast("Открываем вакансию: ${vacancy.title}")
        val bundle = Bundle()
        bundle.putString("vacancyId", vacancy.id)
        findNavController().navigate(R.id.action_mainFragment_to_vacancyDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
