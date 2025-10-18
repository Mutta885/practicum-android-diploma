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

        // Обработка системной кнопки назад
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                Log.d(TAG, "System back button pressed - navigating back WITHOUT saving filters")
                // НЕ сохраняем фильтры и НЕ применяем поиск
                findNavController().popBackStack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
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
            // НЕ сохраняем фильтры и НЕ применяем поиск
            findNavController().popBackStack()
        }
    }

    private fun setupSalaryInput() {
        with(binding) {
            salaryInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    Log.d(TAG, "Salary input changed: '$text'")
                    viewModel.onSalaryChanged(text)
                    updateClearSalaryButtonVisibility(text)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            })

            salaryInput.setOnFocusChangeListener { _, hasFocus ->
                salaryLabel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (hasFocus) R.color.blue else R.color.grey
                    )
                )
            }

            clearSalaryButton.setOnClickListener {
                Log.d(TAG, "Clear salary button clicked")
                salaryInput.text?.clear()
                viewModel.onSalaryChanged("")
            }
        }
    }

    private fun setupIndustryNavigation() {
        binding.industryItem1.setOnClickListener {
            findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
        }
        binding.groupIndustryItem2.setOnClickListener {
            findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
        }
        binding.closeIndustry.setOnClickListener {
            binding.groupIndustryItem2.isVisible = false
            binding.industryItem1.isVisible = true
        }
    }

    private fun setupWorkplaceNavigation() {
        binding.workplaceItem1.setOnClickListener {
            findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
        }
        binding.groupWorkplaceItem2.setOnClickListener {
            findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
        }
        binding.closeWorkplace.setOnClickListener {
            binding.groupWorkplaceItem2.isVisible = false
            binding.workplaceItem1.isVisible = true
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
                hideWithoutSalaryCheckbox.isChecked = !hideWithoutSalaryCheckbox.isChecked
            }
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
        // Сохраняем фильтры в хранилище ТОЛЬКО при явном применении
        viewModel.saveFiltersToStorage()
        viewModel.setFiltersJustApplied(true)
        // ВАЖНО: Используем applyFiltersWithSearch для явного применения фильтров
        searchViewModel.applyFiltersWithSearch(filters)
        Toast.makeText(context, "Фильтры применены", Toast.LENGTH_SHORT).show()

        // Возвращаемся назад через popBackStack чтобы сохранить состояние MainFragment
        findNavController().popBackStack()
    }

    private fun resetFilters() {
        viewModel.resetFilters()
        binding.salaryInput.text?.clear()
        // Сохраняем сброшенные фильтры в хранилище ТОЛЬКО при явном сбросе
        viewModel.saveFiltersToStorage()
        viewModel.setFiltersJustApplied(true)
        // ВАЖНО: Используем applyFiltersWithSearch для сброса фильтров
        searchViewModel.applyFiltersWithSearch(FiltrationViewModel.Filters())
        Toast.makeText(context, "Фильтры сброшены", Toast.LENGTH_SHORT).show()

        // Возвращаемся назад через popBackStack чтобы сохранить состояние MainFragment
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

        if (text != getString(R.string.industry)) {
            binding.industryItem1.isVisible = false
            binding.groupIndustryItem2.isVisible = true
        } else {
            binding.industryItem1.isVisible = true
            binding.groupIndustryItem2.isVisible = false
        }
    }

    private fun updateWorkplaceText() {
        val country = viewModel.selectedCountry.value
        val region = viewModel.selectedRegion.value
        val workplaceText = when {
            country != null && region != null -> "$country, $region"
            country != null -> country
            region != null -> region
            else -> {
                getString(R.string.area_job)
            }
        }

        binding.workplaceItem2.text = workplaceText

        if (workplaceText != getString(R.string.area_job)) {
            binding.workplaceItem1.isVisible = false
            binding.groupWorkplaceItem2.isVisible = true
        } else {
            binding.workplaceItem1.isVisible = true
            binding.groupWorkplaceItem2.isVisible = false
        }
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
