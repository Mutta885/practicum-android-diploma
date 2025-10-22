package ru.practicum.android.diploma.ui.fragments.helpers

import android.view.View
import ru.practicum.android.diploma.databinding.FragmentRegionBinding

class RegionUiStateHandler {

    fun showLoadingState(binding: FragmentRegionBinding) {
        with(binding) {
            loadingContainer.visibility = View.VISIBLE
            errorContainer.visibility = View.GONE
            noResultsContainer.visibility = View.GONE
            successContainer.visibility = View.GONE
        }
    }

    fun showErrorState(binding: FragmentRegionBinding, message: String, errorImageRes: Int) {
        with(binding) {
            errorText.text = message
            loadingContainer.visibility = View.GONE
            ivError.setImageResource(errorImageRes)
            errorContainer.visibility = View.VISIBLE
            noResultsContainer.visibility = View.GONE
            successContainer.visibility = View.GONE
        }
    }

    fun showNoResultsState(binding: FragmentRegionBinding) {
        with(binding) {
            loadingContainer.visibility = View.GONE
            errorContainer.visibility = View.GONE
            noResultsContainer.visibility = View.VISIBLE
            successContainer.visibility = View.GONE
        }
    }

    fun showSuccessState(binding: FragmentRegionBinding) {
        with(binding) {
            loadingContainer.visibility = View.GONE
            errorContainer.visibility = View.GONE
            noResultsContainer.visibility = View.GONE
            successContainer.visibility = View.VISIBLE
        }
    }
}
