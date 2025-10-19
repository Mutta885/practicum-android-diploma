package ru.practicum.android.diploma.ui.fragments

import android.util.Log
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding

class FiltrationNavigationManager(
    private val fragment: androidx.fragment.app.Fragment,
    private val binding: FragmentFilterBinding
) {

    fun setupNavigation() {
        setupIndustryNavigation()
        setupWorkplaceNavigation()
        setupReturnButton()
    }

    private fun setupIndustryNavigation() {
        binding.industryItem1.setOnClickListener {
            navigateToIndustry()
        }
        binding.groupIndustryItem2.setOnClickListener {
            navigateToIndustry()
        }
        binding.closeIndustry.setOnClickListener {
            hideIndustrySelection()
        }
    }

    private fun navigateToIndustry() {
        fragment.findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
    }

    private fun hideIndustrySelection() {
        binding.groupIndustryItem2.isVisible = false
        binding.industryItem1.isVisible = true
    }

    private fun setupWorkplaceNavigation() {
        binding.workplaceItem1.setOnClickListener {
            navigateToWorkplace()
        }
        binding.groupWorkplaceItem2.setOnClickListener {
            navigateToWorkplace()
        }
        binding.closeWorkplace.setOnClickListener {
            hideWorkplaceSelection()
        }
    }

    private fun navigateToWorkplace() {
        fragment.findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
    }

    private fun hideWorkplaceSelection() {
        binding.groupWorkplaceItem2.isVisible = false
        binding.workplaceItem1.isVisible = true
    }

    private fun setupReturnButton() {
        binding.returnButton.setOnClickListener {
            Log.d(TAG, "Return button clicked - navigating back WITHOUT saving filters")
            fragment.findNavController().popBackStack()
        }
    }

    companion object {
        private const val TAG = "FiltrationNavigationManager"
    }
}
