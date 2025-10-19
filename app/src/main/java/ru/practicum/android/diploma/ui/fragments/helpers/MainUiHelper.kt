package ru.practicum.android.diploma.ui.fragments.helpers

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.FragmentMainBinding
import ru.practicum.android.diploma.presentation.adapters.VacanciesAdapter
import ru.practicum.android.diploma.presentation.vmodels.SearchState
import ru.practicum.android.diploma.presentation.vmodels.SearchViewModel

object MainUiHelper {

    private const val LOAD_MORE_THRESHOLD = 3

    fun setupRecycler(binding: FragmentMainBinding, adapter: VacanciesAdapter, viewModel: SearchViewModel) {
        binding.vacanciesRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        binding.vacanciesRecyclerView.adapter = adapter
        binding.vacanciesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !viewModel.isLoading() && viewModel.hasMorePages()) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val total = lm.itemCount
                    val last = lm.findLastVisibleItemPosition()
                    if (last >= total - LOAD_MORE_THRESHOLD) viewModel.loadNextPage()
                }
            }
        })
    }

    fun setupSearchField(
        binding: FragmentMainBinding,
        onTextChanged: (String) -> Unit,
        updateIcons: (String) -> Unit,
        onClear: () -> Unit,
        viewModel: SearchViewModel
    ) {
        val editText = binding.searchEditText
        updateIcons(editText.text.toString())

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                onTextChanged(s.toString())
            }
        })

        binding.searchIcon.setOnClickListener {
            viewModel.search(editText.text.toString().trim())
        }

        binding.clearIcon.setOnClickListener {
            onClear()
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(editText.text.toString().trim())
                true
            } else {
                false
            }
        }
    }

    fun updateSearchIcons(binding: FragmentMainBinding, text: String) {
        val notEmpty = text.isNotEmpty()
        binding.searchIcon.visibility = if (notEmpty) View.GONE else View.VISIBLE
        binding.clearIcon.visibility = if (notEmpty) View.VISIBLE else View.GONE
    }

    fun showEmptyQueryState(binding: FragmentMainBinding, adapter: VacanciesAdapter) {
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

    fun showLoadingState(binding: FragmentMainBinding) {
        with(binding) {
            loadingProgressBar.isVisible = true
            vacanciesRecyclerView.isVisible = false
            errorStateContainer.isVisible = false
            noResultsContainer.isVisible = false
            emptyStateContainer.isVisible = false
            resultsCountText.isVisible = false
        }
    }

    fun showSuccessState(
        binding: FragmentMainBinding,
        state: SearchState.Success,
        adapter: VacanciesAdapter,
        hasMorePages: Boolean
    ) {
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
                resultsCountText.isVisible = true
                resultsCountText.text = "Найдено ${state.found} вакансий"
            }
        }
        adapter.submitVacancies(state.vacancies)
        adapter.setHasMore(hasMorePages)
    }

    fun showErrorState(binding: FragmentMainBinding, message: String) {
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
}
