package ru.practicum.android.diploma.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.presentation.vmodels.FiltrationViewModel
import ru.practicum.android.diploma.presentation.vmodels.SearchViewModel

class FiltrationFragment : Fragment() {
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FiltrationViewModel by activityViewModel()
    private val searchViewModel: SearchViewModel by sharedViewModel()

    private lateinit var uiStateManager: FiltrationUiStateManager
    private lateinit var navigationManager: FiltrationNavigationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        uiStateManager = FiltrationUiStateManager(binding)
        navigationManager = FiltrationNavigationManager(this, binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackButtonHandler(view)
        setupViews()
        observeViewModel()
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
            Log.d(TAG, "System back button pressed - navigating back WITHOUT saving filters")
            findNavController().popBackStack()
            return true
        }
        return false
    }

    private fun setupViews() {
        navigationManager.setupNavigation()
        setupSalaryInput()
        setupSalaryCheckbox()
        setupActionButtons()
        setupClearButtons()
    }

    private fun setupSalaryInput() {
        with(binding) {
            salaryInput.addTextChangedListener(createSalaryTextWatcher())
            setupSalaryFocusListener()
            setupClearSalaryButton()
        }
    }

    private fun createSalaryTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                handleSalaryTextChange(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        }
    }

    private fun handleSalaryTextChange(text: String) {
        Log.d(TAG, "Salary input changed: '$text'")
        viewModel.onSalaryChanged(text)
        uiStateManager.updateClearSalaryButtonVisibility(text)
    }

    private fun FragmentFilterBinding.setupSalaryFocusListener() {
        salaryInput.setOnFocusChangeListener { _, hasFocus ->
            val colorRes = if (hasFocus) R.color.blue else R.color.grey
            salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
        }
    }

    private fun FragmentFilterBinding.setupClearSalaryButton() {
        clearSalaryButton.setOnClickListener {
            Log.d(TAG, "Clear salary button clicked")
            salaryInput.text?.clear()
            viewModel.onSalaryChanged("")
        }
    }

    private fun setupSalaryCheckbox() {
        with(binding) {
            setupCheckboxListener()
            setupCheckboxContainer()
        }
    }

    private fun FragmentFilterBinding.setupCheckboxListener() {
        hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Hide without salary checkbox changed: $isChecked")
            viewModel.onHideWithoutSalaryChanged(isChecked)
        }
    }

    private fun FragmentFilterBinding.setupCheckboxContainer() {
        hideSalaryContainer.setOnClickListener {
            Log.d(TAG, "Hide salary container clicked")
            hideWithoutSalaryCheckbox.isChecked = !hideWithoutSalaryCheckbox.isChecked
        }
    }

    private fun setupActionButtons() {
        setupApplyButton()
        setupResetButton()
    }

    private fun setupClearButtons() {
        binding.closeIndustry.setOnClickListener {
            Log.d(TAG, "Clear industry button clicked")
            viewModel.clearIndustries()
        }

        binding.closeWorkplace.setOnClickListener {
            Log.d(TAG, "Clear workplace button clicked")
            viewModel.clearWorkplace()
        }
    }

    private fun setupApplyButton() {
        binding.applyButton.setOnClickListener {
            Log.d(TAG, "Apply button clicked - saving and applying filters WITH SEARCH")
            applyFilters()
        }
    }

    private fun setupResetButton() {
        binding.resetButton.setOnClickListener {
            Log.d(TAG, "Reset button clicked")
            resetFilters()
        }
    }

    private fun applyFilters() {
        val filters = viewModel.getCurrentFilters()
        viewModel.saveFiltersToStorage()
        viewModel.setFiltersJustApplied(true)
        searchViewModel.applyFiltersWithSearch(filters)
        showToast("Фильтры применены")
        navigateBack()
    }

    private fun resetFilters() {
        viewModel.resetFilters()
        binding.salaryInput.text?.clear()
        viewModel.saveFiltersToStorage()
        viewModel.setFiltersJustApplied(true)
        searchViewModel.applyFiltersWithSearch(FiltrationViewModel.Filters())
        showToast("Фильтры сброшены")
        navigateBack()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    private fun observeViewModel() {
        observeFilterState()
        observeIndustries()
        observeWorkplace()
        observeSalaryState()
        observeCheckboxState()
    }

    private fun observeFilterState() {
        viewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            Log.d(TAG, "isAnyFilterActive changed: $isActive")
            uiStateManager.updateActionButtonsVisibility(isActive)
        }
    }

    private fun observeIndustries() {
        viewModel.selectedIndustries.observe(viewLifecycleOwner) { industries ->
            Log.d(TAG, "selectedIndustries observed: ${industries.size} items")
            uiStateManager.updateIndustryText(industries)
        }
    }

    private fun observeWorkplace() {
        listOf(viewModel.selectedCountry, viewModel.selectedRegion).forEach { liveData ->
            liveData.observe(viewLifecycleOwner) {
                uiStateManager.updateWorkplaceText(
                    viewModel.selectedCountry.value,
                    viewModel.selectedRegion.value
                )
            }
        }
    }

    private fun observeSalaryState() {
        viewModel.salary.observe(viewLifecycleOwner) { salary ->
            uiStateManager.updateSalaryInput(binding.salaryInput.text?.toString(), salary)
            uiStateManager.updateClearSalaryButtonVisibility(salary ?: "")
        }
        viewModel.isSalaryInputNotEmpty.observe(viewLifecycleOwner) { isNotEmpty ->
            uiStateManager.updateClearSalaryButtonVisibility(binding.salaryInput.text?.toString() ?: "")
        }
    }

    private fun observeCheckboxState() {
        viewModel.hideWithoutSalary.observe(viewLifecycleOwner) { isChecked ->
            uiStateManager.updateCheckboxState(isChecked) { checked ->
                viewModel.onHideWithoutSalaryChanged(checked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FiltrationFragment"
    }
}
