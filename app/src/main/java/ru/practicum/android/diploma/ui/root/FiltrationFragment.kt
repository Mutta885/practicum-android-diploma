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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentFilterBinding
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel

class FiltrationFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FiltrationViewModel by lazy {
        ViewModelProvider(this)[FiltrationViewModel::class.java]
    }

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

    private fun setupViews() {
        with(binding) {
            // Кнопка возврата
            returnButton.setOnClickListener {
                findNavController().navigateUp()
            }

            // Поле ввода зарплаты
            salaryInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.onSalaryChanged(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
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
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isAnyFilterActive.observe(viewLifecycleOwner) { isActive ->
            binding.applyButton.visibility = if (isActive) View.VISIBLE else View.GONE
            binding.resetButton.visibility = if (isActive) View.VISIBLE else View.GONE
        }

        viewModel.isSalaryInputNotEmpty.observe(viewLifecycleOwner) { isNotEmpty ->
            binding.clearSalaryButton.visibility = if (isNotEmpty) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
