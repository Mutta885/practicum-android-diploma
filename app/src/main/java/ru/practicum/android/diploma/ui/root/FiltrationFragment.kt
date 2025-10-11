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
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.domain.models.FilterParameters
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

class FiltrationFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltrationViewModel by viewModel()

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
        viewModel.fetchFilterInSharedPreferences()
    }

    private fun setupViews() {
        with(binding) {
            // Кнопка возврата
            returnButton.setOnClickListener {
                Log.d(TAG, "Return button clicked")
                findNavController().navigateUp()
            }

            // Поле ввода зарплаты
            salaryInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    Log.d(TAG, "Salary input changed: '$text'")
                    viewModel.onSalaryChanged(text)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.onSalaryChanged(s.toString())
                }
            })

            // Следим за фокусом
            salaryInput.setOnFocusChangeListener { _, hasFocus ->
                Log.d(TAG, "Salary input focus changed: $hasFocus")
                if (hasFocus) {
                    salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }

            // Кнопка очистки зарплаты
            clearSalaryButton.setOnClickListener {
                Log.d(TAG, "Clear salary button clicked")
                salaryInput.text?.clear()
            }

            // Переход на экран отраслей
            industryItem.setOnClickListener {
                Log.d(TAG, "Industry item clicked - navigating to industry fragment")
                findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
            }

            // Чекбокс "Не показывать без зарплаты"
            hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                Log.d(TAG, "Hide without salary checkbox changed: $isChecked")
                viewModel.onHideWithoutSalaryChanged(isChecked)
            }

            // Кнопка "Применить"
            applyButton.setOnClickListener {
                Log.d(TAG, "Apply button clicked")

                // Создаем объект фильтров и передаем в SearchViewModel
                val filters = FiltrationViewModel.Filters(
                    salary = viewModel.salary.value,
                    hideWithoutSalary = viewModel.hideWithoutSalary.value ?: false,
                    industries = viewModel.selectedIndustries.value ?: emptyList()
                )

                searchViewModel.setFilters(filters)
                Toast.makeText(context, "Фильтры применены", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }

            // Кнопка "Сбросить"
            resetButton.setOnClickListener {
                Log.d(TAG, "Reset button clicked")
                viewModel.resetFilters()
                salaryInput.text?.clear()
                hideWithoutSalaryCheckbox.isChecked = false
                selectedIndustryText.text = "Отрасль"
            }
        }
    }

    private fun observeViewModel() {
        viewModel.observeFilterParametersState.observe(viewLifecycleOwner){ state ->
                renderUI(state)
        }

        viewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            Log.d(TAG, "isAnyFilterActive changed: $isActive")
            binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
            binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
            if(!isActive) {
                viewModel.clearFilterInSharedPreferences()
            }
        }

        viewModel.selectedIndustries.observe(viewLifecycleOwner) { industries ->
            Log.d(TAG, "selectedIndustries observed: ${industries.size} items")
            industries.forEach { Log.d(TAG, "Received industry: ${it.name} (id: ${it.id})") }

            if (industries.isNotEmpty()) {
                if (industries.size == 1) {
                    binding.selectedIndustryText.text = industries[0].name
                    Log.d(TAG, "Setting industry text to: ${industries[0].name}")
                } else {
                    binding.selectedIndustryText.text = "Выбрано: ${industries.size}"
                    Log.d(TAG, "Setting industry text to: Выбрано: ${industries.size}")
                }
            } else {
                binding.selectedIndustryText.text = "Отрасль"
                Log.d(TAG, "Setting industry text to: Отрасль")
            }
        }

        viewModel.isSalaryInputNotEmpty.observe(viewLifecycleOwner) { isNotEmpty ->
            Log.d(TAG, "isSalaryInputNotEmpty changed: $isNotEmpty")
            binding.clearSalaryButton.visibility = if (isNotEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun renderUI(state: FilterParameters){
        if(state.onlyWithSalary) {
            renderCheckBox(true)
        }
        if(state.salary.isNotEmpty()){
            renderSalaryEditText(state.salary)
        }
    }

    private fun renderCheckBox(value: Boolean){
        binding.hideWithoutSalaryCheckbox.isChecked = value
    }

    private fun renderSalaryEditText(value: String){
        binding.salaryInput.setText(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
