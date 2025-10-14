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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.ui.search.SearchViewModel

class FiltrationFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltrationViewModel by activityViewModel()

    private val searchViewModel: SearchViewModel by activityViewModel()

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
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
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

            // Чекбокс "Не показывать без зарплаты" - ИСПРАВЛЕНО
            hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                Log.d(TAG, "Hide without salary checkbox changed: $isChecked")
                viewModel.onHideWithoutSalaryChanged(isChecked)
            }

            // ДОБАВЛЕНО: Обработка клика на всю строку чекбокса
            hideSalaryContainer.setOnClickListener {
                Log.d(TAG, "Hide salary container clicked")
                val currentState = hideWithoutSalaryCheckbox.isChecked
                hideWithoutSalaryCheckbox.isChecked = !currentState
                // Слушатель onCheckedChangeListener автоматически вызовется
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

                // Возвращаемся к предыдущему экрану
                findNavController().navigateUp()
            }

            // Кнопка "Сбросить"
            resetButton.setOnClickListener {
                Log.d(TAG, "Reset button clicked")
                viewModel.resetFilters()
                salaryInput.text?.clear()

                // НЕ устанавливаем чекбокс напрямую - он обновится через observeViewModel()
                selectedIndustryText.text = "Отрасль"

                // Также сбрасываем фильтры в SearchViewModel
                searchViewModel.setFilters(FiltrationViewModel.Filters())
                Toast.makeText(context, "Фильтры сброшены", Toast.LENGTH_SHORT).show()
            }
        }

        binding.workplaceItem.setOnClickListener {
            findNavController().navigate(R.id.action_filtrationFragment_to_workPlaceFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            Log.d(TAG, "isAnyFilterActive changed: $isActive")
            binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
            binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
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

        // Следим за состоянием чекбокса
        viewModel.hideWithoutSalary.observe(viewLifecycleOwner) { isChecked ->
            Log.d(TAG, "hideWithoutSalary observed: $isChecked")
            // Временно отключаем слушатель чтобы избежать рекурсии
            binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener(null)
            binding.hideWithoutSalaryCheckbox.isChecked = isChecked
            binding.hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, checked ->
                viewModel.onHideWithoutSalaryChanged(checked)
            }
        }

        // Следим за зарплатой для предзаполнения поля
        viewModel.salary.observe(viewLifecycleOwner) { salary ->
            Log.d(TAG, "salary observed: '$salary'")
            if (salary != null && binding.salaryInput.text?.toString() != salary) {
                binding.salaryInput.setText(salary)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
