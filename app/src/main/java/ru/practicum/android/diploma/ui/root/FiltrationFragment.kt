package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

class FiltrationFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltrationViewModel by activityViewModel()
    private val searchViewModel: SearchViewModel by sharedViewModel()

    companion object {
        private const val TAG = "FiltrationFragment"
    }

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

    private fun setupViews() {
        setupReturnButton()
        setupSalaryInput()
        setupIndustryNavigation()
        setupWorkplaceNavigation()
        setupSalaryCheckbox()
        setupActionButtons()
    }

    private fun setupReturnButton() {
        binding.returnButton.setOnClickListener {
            Log.d(TAG, "Return button clicked")
            findNavController().navigate(R.id.action_filtrationFragment_to_mainFragment)
        }
    }

    private fun setupSalaryInput() {
        with(binding) {
            salaryInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    Log.d(TAG, "Salary input changed: '$text'")
                    viewModel.onSalaryChanged(text)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            })

            salaryInput.setOnFocusChangeListener { _, hasFocus ->
                Log.d(TAG, "Salary input focus changed: $hasFocus")
                if (hasFocus) {
                    salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }

            clearSalaryButton.setOnClickListener {
                Log.d(TAG, "Clear salary button clicked")
                salaryInput.text?.clear()
            }
        }
    }

    private fun setupIndustryNavigation() {
        binding.industryItem.setOnClickListener {
            Log.d(TAG, "Industry item clicked - navigating to industry fragment")
            findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
        }
    }

    private fun setupWorkplaceNavigation() {
        binding.workplaceItem.setOnClickListener {
            Log.d(TAG, "Workplace item clicked - navigating to workPlace fragment")
            findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
        }
    }

    private fun setupSalaryCheckbox() {
        with(binding) {
            hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                Log.d(TAG, "Hide without salary checkbox changed: $isChecked")
                viewModel.onHideWithoutSalaryChanged(isChecked)
            }

            hideSalaryContainer.setOnClickListener {
                Log.d(TAG, "Hide salary container clicked")
                val currentState = hideWithoutSalaryCheckbox.isChecked
                hideWithoutSalaryCheckbox.isChecked = !currentState
            }
        }
    }

    private fun setupActionButtons() {
        setupApplyButton()
        setupResetButton()
    }

    private fun setupApplyButton() {
        binding.applyButton.setOnClickListener {
            Log.d(TAG, "Apply button clicked")
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
        val filters = FiltrationViewModel.Filters(
            salary = viewModel.salary.value,
            hideWithoutSalary = viewModel.hideWithoutSalary.value ?: false,
            industries = viewModel.selectedIndustries.value ?: emptyList(),
            country = viewModel.selectedCountry.value,
            countryId = viewModel.selectedCountryId.value,
            region = viewModel.selectedRegion.value,
            regionId = viewModel.selectedRegionId.value
        )

        viewModel.setFiltersJustApplied(true)
        searchViewModel.setFilters(filters)
        Toast.makeText(context, "Фильтры применены", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_filtrationFragment_to_mainFragment)
    }

    private fun resetFilters() {
        viewModel.resetFilters()
        binding.salaryInput.text?.clear()
        viewModel.setFiltersJustApplied(true)
        searchViewModel.setFilters(FiltrationViewModel.Filters())
        Toast.makeText(context, "Фильтры сброшены", Toast.LENGTH_SHORT).show()
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
            binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
            binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
        }
    }

    private fun observeIndustries() {
        viewModel.selectedIndustries.observe(viewLifecycleOwner) { industries ->
            Log.d(TAG, "selectedIndustries observed: ${industries.size} items")
            updateIndustryText(industries)
        }
    }

    private fun observeWorkplace() {
        viewModel.selectedCountry.observe(viewLifecycleOwner) { country ->
            Log.d(TAG, "selectedCountry observed: $country")
            updateWorkplaceText()
        }

        viewModel.selectedRegion.observe(viewLifecycleOwner) { region ->
            Log.d(TAG, "selectedRegion observed: $region")
            updateWorkplaceText()
        }
    }

    private fun observeSalaryState() {
        viewModel.isSalaryInputNotEmpty.observe(viewLifecycleOwner) { isNotEmpty ->
            Log.d(TAG, "isSalaryInputNotEmpty changed: $isNotEmpty")
            binding.clearSalaryButton.visibility = if (isNotEmpty) View.VISIBLE else View.GONE
        }

        viewModel.salary.observe(viewLifecycleOwner) { salary ->
            Log.d(TAG, "salary observed: '$salary'")
            updateSalaryInput(salary)
        }
    }

    private fun observeCheckboxState() {
        viewModel.hideWithoutSalary.observe(viewLifecycleOwner) { isChecked ->
            Log.d(TAG, "hideWithoutSalary observed: $isChecked")
            updateCheckboxState(isChecked)
        }
    }

    private fun updateIndustryText(industries: List<ru.practicum.android.diploma.domain.models.Industry>) {
        val text = when {
            industries.isEmpty() -> "Отрасль"
            industries.size == 1 -> industries[0].name
            else -> "Выбрано: ${industries.size}"
        }
        binding.selectedIndustryText.text = text
        Log.d(TAG, "Setting industry text to: $text")
    }

    private fun updateWorkplaceText() {
        val country = viewModel.selectedCountry.value
        val region = viewModel.selectedRegion.value

        val workplaceText = when {
            country != null && region != null -> "$country, $region"
            country != null -> country
            region != null -> region
            else -> "Место работы"
        }

        binding.workplaceText.text = workplaceText
        Log.d(TAG, "Workplace text updated to: $workplaceText")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
