package ru.practicum.android.diploma.ui.root

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
        listOf(binding.industryItem, binding.industryArrow).forEach {
            it.setOnClickListener {
                Log.d(TAG, "Industry item clicked - navigating to industry fragment")
                findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
            }
        }
    }

    private fun setupWorkplaceNavigation() {
        listOf(binding.workplaceItem, binding.workplaceArrow).forEach {
            it.setOnClickListener {
                Log.d(TAG, "Workplace item clicked - navigating to workPlace fragment")
                findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
            }
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
        binding.clearIndustryButton.setOnClickListener {
            Log.d(TAG, "Clear industry button clicked")
            viewModel.clearIndustries()
        }

        binding.clearWorkplaceButton.setOnClickListener {
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
            updateClearIndustryButtonVisibility(industries)
        }
    }

    private fun observeWorkplace() {
        listOf(viewModel.selectedCountry, viewModel.selectedRegion).forEach { liveData ->
            liveData.observe(viewLifecycleOwner) {
                updateWorkplaceText()
                updateClearWorkplaceButtonVisibility()
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
            industries.isEmpty() -> "Отрасль"
            industries.size == 1 -> industries[0].name
            else -> "Выбрано: ${industries.size}"
        }
        binding.industryItem.text = text
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
        binding.workplaceItem.text = workplaceText
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

    private fun updateClearIndustryButtonVisibility(industries: List<ru.practicum.android.diploma.domain.models.Industry>) {
        val hasSelection = industries.isNotEmpty()
        binding.clearIndustryButton.visibility = if (hasSelection) View.VISIBLE else View.GONE
        binding.industryArrow.visibility = if (hasSelection) View.GONE else View.VISIBLE
        Log.d(TAG, "Industry visibility - hasSelection: $hasSelection, clearButton: ${binding.clearIndustryButton.visibility}, arrow: ${binding.industryArrow.visibility}")
    }

    private fun updateClearWorkplaceButtonVisibility() {
        val country = viewModel.selectedCountry.value
        val region = viewModel.selectedRegion.value
        val hasSelection = country != null || region != null
        binding.clearWorkplaceButton.visibility = if (hasSelection) View.VISIBLE else View.GONE
        binding.workplaceArrow.visibility = if (hasSelection) View.GONE else View.VISIBLE
        Log.d(TAG, "Workplace visibility - hasSelection: $hasSelection, clearButton: ${binding.clearWorkplaceButton.visibility}, arrow: ${binding.workplaceArrow.visibility}")
    }

    private fun updateClearSalaryButtonVisibility(salaryText: String) {
        binding.clearSalaryButton.visibility = if (salaryText.isNotEmpty()) View.VISIBLE else View.GONE
        Log.d(TAG, "Salary clear button visibility: ${binding.clearSalaryButton.visibility}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
