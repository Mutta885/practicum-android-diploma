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
import androidx.core.view.isVisible
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        setupViews()
        observeViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackButtonHandler(view)
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
        setupReturnButton()
        setupSalaryInput()
        setupIndustryNavigation()
        setupWorkplaceNavigation()
        setupSalaryCheckbox()
        setupActionButtons()
        setupClearButtons()
    }

    private fun setupReturnButton() {
        binding.returnButton.setOnClickListener {
            Log.d(TAG, "Return button clicked - navigating back WITHOUT saving filters")
            findNavController().popBackStack()
        }
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
        updateClearSalaryButtonVisibility(text)
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
        findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
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
        findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
    }

    private fun hideWorkplaceSelection() {
        binding.groupWorkplaceItem2.isVisible = false
        binding.workplaceItem1.isVisible = true
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
            updateActionButtonsVisibility(isActive)
        }
    }

    private fun updateActionButtonsVisibility(isActive: Boolean) {
        binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
        binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
    }

    private fun observeIndustries() {
        viewModel.selectedIndustries.observe(viewLifecycleOwner) { industries ->
            Log.d(TAG, "selectedIndustries observed: ${industries.size} items")
            updateIndustryText(industries)
        }
    }

    private fun observeWorkplace() {
        listOf(viewModel.selectedCountry, viewModel.selectedRegion).forEach { liveData ->
            liveData.observe(viewLifecycleOwner) {
                updateWorkplaceText()
            }
        }
    }

    private fun observeSalaryState() {
        viewModel.salary.observe(viewLifecycleOwner) { salary ->
            updateSalaryInput(salary)
            updateClearSalaryButtonVisibility(salary ?: "")
        }
        viewModel.isSalaryInputNotEmpty.observe(viewLifecycleOwner) { isNotEmpty ->
            updateClearSalaryButtonVisibility(binding.salaryInput.text?.toString() ?: "")
        }
    }

    private fun observeCheckboxState() {
        viewModel.hideWithoutSalary.observe(viewLifecycleOwner) { isChecked ->
            updateCheckboxState(isChecked)
        }
    }

    private fun updateIndustryText(industries: List<ru.practicum.android.diploma.domain.models.Industry>) {
        val text = when {
            industries.isEmpty() -> getString(R.string.industry)
            industries.size == 1 -> industries[0].name
            else -> "${getString(R.string.this_select)}: ${industries.size}"
        }
        binding.industryItem2.text = text
        updateIndustryVisibility(text != getString(R.string.industry))
    }

    private fun updateIndustryVisibility(hasSelection: Boolean) {
        binding.industryItem1.isVisible = !hasSelection
        binding.groupIndustryItem2.isVisible = hasSelection
    }

    private fun updateWorkplaceText() {
        val country = viewModel.selectedCountry.value
        val region = viewModel.selectedRegion.value
        val workplaceText = buildWorkplaceText(country, region)
        binding.workplaceItem2.text = workplaceText
        updateWorkplaceVisibility(workplaceText != getString(R.string.area_job))
    }

    private fun buildWorkplaceText(country: String?, region: String?): String {
        return when {
            country != null && region != null -> "$country, $region"
            country != null -> country
            region != null -> region
            else -> getString(R.string.area_job)
        }
    }

    private fun updateWorkplaceVisibility(hasSelection: Boolean) {
        binding.workplaceItem1.isVisible = !hasSelection
        binding.groupWorkplaceItem2.isVisible = hasSelection
    }

    private fun updateSalaryInput(salary: String?) {
        if (salary != null && binding.salaryInput.text?.toString() != salary) {
            binding.salaryInput.setText(salary)
        }
    }

    private fun updateCheckboxState(isChecked: Boolean) {
        binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener(null)
        binding.hideWithoutSalaryCheckbox.isChecked = isChecked
        binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, checked ->
            viewModel.onHideWithoutSalaryChanged(checked)
        }
    }

    private fun updateClearSalaryButtonVisibility(salaryText: String) {
        binding.clearSalaryButton.visibility = if (salaryText.isNotEmpty()) View.VISIBLE else View.GONE
        Log.d(TAG, "Salary clear button visibility: ${binding.clearSalaryButton.visibility}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FiltrationFragment"
    }
}
