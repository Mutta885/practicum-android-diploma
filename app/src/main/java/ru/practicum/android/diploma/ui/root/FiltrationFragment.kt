package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.domain.models.FilterParameters
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel

class FiltrationFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltrationViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                findNavController().navigateUp()
            }

            // Поле ввода зарплаты
            salaryInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.onSalaryChanged(s.toString())
                }
            })

            // Следим за фокусом
            salaryInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    salaryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }

            // Кнопка очистки зарплаты
            clearSalaryButton.setOnClickListener {
                salaryInput.text?.clear()
            }

            // Переход на экран отраслей (временно отключён)
            industryItem.setOnClickListener {
                // findNavController().navigate(R.id.action_filtrationFragment_to_industryFragment)
                Toast.makeText(context, "Выбор отрасли", Toast.LENGTH_SHORT).show()
            }

            // Чекбокс "Не показывать без зарплаты"
            hideWithoutSalaryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onHideWithoutSalaryChanged(isChecked)
            }

            // Кнопка "Применить"
            applyButton.setOnClickListener {
                Toast.makeText(context, "Фильтры применены", Toast.LENGTH_SHORT).show()
                viewModel.saveFilterInSharedPreferences()
                findNavController().navigateUp()
            }

            // Кнопка "Сбросить"
            resetButton.setOnClickListener {
                viewModel.resetFilters()
                salaryInput.text?.clear()
                hideWithoutSalaryCheckbox.isChecked = false
                selectedIndustryText.text = "Отрасль"
                viewModel.clearFilterInSharedPreferences()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.observeFilterParametersState.observe(viewLifecycleOwner){ state ->
                renderUI(state)
        }

        viewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
            binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
            if(!isActive) {
                viewModel.clearFilterInSharedPreferences()
            }
        }

        viewModel.isSalaryInputNotEmpty.observe(viewLifecycleOwner) { isNotEmpty ->
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
