package ru.practicum.android.diploma.ui.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import ru.practicum.android.diploma.ui.fragments.helpers.MainUiHelper
import ru.practicum.android.diploma.util.debounce
import ru.practicum.android.diploma.util.showToast

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by sharedViewModel()
    private val filtrationViewModel: FiltrationViewModel by activityViewModel()
    private var searchDebounce: ((String) -> Unit)? = null
    private val adapter: VacanciesAdapter by lazy {
        VacanciesAdapter(onItemClick = { vacancy -> onVacancyClick(vacancy) })
    }

    private var isProgrammaticTextChange = false
    private var isReturningFromFilters = false
    private var shouldBlockAutoSearch = false // НОВЫЙ ФЛАГ

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
        searchDebounce = debounce<String>(TIME_DELAY, viewLifecycleOwner.lifecycleScope, true) { text ->
            if (!shouldBlockAutoSearch) { // ПРОВЕРКА ФЛАГА
                searchViewModel.search(text)
            } else {
                shouldBlockAutoSearch = false // Сбрасываем флаг после блокировки
            }
        }
        MainUiHelper.setupRecycler(binding, adapter, searchViewModel)
        MainUiHelper.setupSearchField(
            binding,
            ::handleTextChange,
            ::updateSearchFieldIcons,
            ::clearSearchField,
            searchViewModel
        )
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

    override fun onPause() {
        super.onPause()
        // Устанавливаем флаг блокировки при уходе с фрагмента
        shouldBlockAutoSearch = true
    }

    private fun setupClickListeners() {
        binding.trailingButton.setOnClickListener {
            isReturningFromFilters = true
            shouldBlockAutoSearch = true // Блокируем авто-поиск при переходе к фильтрам
            findNavController().navigate(R.id.action_mainFragment_to_filtrationFragment)
        }
    }

    private fun setupBackButtonHandler(view: View) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                findNavController().popBackStack()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        searchViewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.EmptyQuery -> MainUiHelper.showEmptyQueryState(binding, adapter)
                is SearchState.Loading -> MainUiHelper.showLoadingState(binding)
                is SearchState.Success -> MainUiHelper.showSuccessState(
                    binding,
                    state,
                    adapter,
                    searchViewModel.hasMorePages()
                )
                is SearchState.Error -> state.message?.let { MainUiHelper.showErrorState(binding, it) }
                is SearchState.LoadingNextPage -> adapter.setLoading(true)
                is SearchState.NextPageError -> state.message?.let { requireContext().showToast(it) }
                is SearchState.FiltersApplied -> {
                    // При применении фильтров сбрасываем флаг блокировки
                    shouldBlockAutoSearch = false
                }
            }
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

            if (!filtersAlreadyApplied()) {
                val currentQuery = searchViewModel.getCurrentQuery()
                val currentFilters = filtrationViewModel.getCurrentAppliedFilters()

                if (hasActiveFilters(currentFilters)) {
                    applyFilters(currentFilters, currentQuery)
                }
            } else {
                resetFiltersJustAppliedFlag()
            }
        }
    }

    private fun filtersAlreadyApplied(): Boolean {
        return filtrationViewModel.filtersJustApplied.value == true
    }

    private fun resetFiltersJustAppliedFlag() {
        filtrationViewModel.setFiltersJustApplied(false)
    }

    private fun hasActiveFilters(filters: FiltrationViewModel.Filters): Boolean {
        return with(filters) {
            salary != null || hideWithoutSalary ||
                industries.isNotEmpty() || country != null || region != null
        }
    }

    private fun applyFilters(filters: FiltrationViewModel.Filters, query: String) {
        searchViewModel.setFiltersWithoutSearch(filters)

        if (query.isNotEmpty()) {
            isProgrammaticTextChange = true
            binding.searchEditText.setText(query)
            updateSearchFieldIcons(query)
            isProgrammaticTextChange = false
        }
    }

    private fun handleTextChange(text: String) {
        if (isProgrammaticTextChange || isReturningFromFilters) {
            isReturningFromFilters = false
            return
        }
        val trimmed = text.trim()
        updateSearchFieldIcons(trimmed)
        searchDebounce?.invoke(trimmed)
    }

    private fun clearSearchField() {
        isProgrammaticTextChange = true
        binding.searchEditText.text.clear()
        isProgrammaticTextChange = false
        searchViewModel.search("")
        updateSearchFieldIcons("")
        shouldBlockAutoSearch = false // Сбрасываем флаг при очистке
    }

    private fun updateSearchFieldIcons(text: String) {
        MainUiHelper.updateSearchIcons(binding, text)
    }

    private fun onVacancyClick(vacancy: Vacancy) {
        val bundle = Bundle().apply { putString("vacancyId", vacancy.id) }
        findNavController().navigate(R.id.action_mainFragment_to_vacancyDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TIME_DELAY = 2000L
        const val DELAY_FOR_FILTERS = 500L
    }
}
