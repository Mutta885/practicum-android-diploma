package ru.practicum.android.diploma.ui.industry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.databinding.FragmentIndustryBinding
import ru.practicum.android.diploma.presentation.industry.IndustryAdapter
import ru.practicum.android.diploma.presentation.industry.IndustryViewModel
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel

class IndustryFragment : Fragment() {

    private var _binding: FragmentIndustryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IndustryViewModel by viewModel()
    private val filtrationViewModel: FiltrationViewModel by lazy {
        ViewModelProvider(requireActivity())[FiltrationViewModel::class.java]
    }
    private lateinit var adapter: IndustryAdapter

    companion object {
        private const val TAG = "IndustryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentIndustryBinding.inflate(inflater, container, false)

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

            // Кнопка "Выбрать"
            selectButton.setOnClickListener {
                Log.d(TAG, "Select button clicked")
                val selectedIndustries = viewModel.getSelectedIndustries()
                Log.d(TAG, "Passing ${selectedIndustries.size} selected industries to filtration VM")

                filtrationViewModel.onIndustriesSelected(selectedIndustries)
                findNavController().navigateUp()
            }

            // Адаптер
            adapter = IndustryAdapter { industry ->
                Log.d(TAG, "Industry selected: ${industry.name} (selected: ${industry.isSelected})")
                selectButton.visibility = View.VISIBLE
            }
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

            // Поиск через EditText + иконки
            val editText = searchEditText
            val searchIcon = searchIcon
            val clearIcon = clearIcon

            // При клике на крестик — очистка и поиск пустого запроса
            clearIcon.setOnClickListener {
                editText.text.clear()
                viewModel.search("")
                clearIcon.visibility = View.GONE
                searchIcon.visibility = View.VISIBLE
            }

            // Слушатель изменений текста
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString().trim()

                    // Управляем видимостью иконок
                    if (text.isNotEmpty()) {
                        searchIcon.visibility = View.GONE
                        clearIcon.visibility = View.VISIBLE
                    } else {
                        searchIcon.visibility = View.VISIBLE
                        clearIcon.visibility = View.GONE
                    }

                    // Выполняем поиск
                    viewModel.search(text)
                }
            })

            // При нажатии Enter (или Search на клавиатуре)
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = editText.text.toString().trim()
                    viewModel.search(query)
                    true
                } else {
                    false
                }
            }

            errorText.setOnClickListener {
                Log.d(TAG, "Error text clicked - retrying")
                viewModel.retry()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.industries.observe(viewLifecycleOwner) { industries ->
            Log.d(TAG, "Industries observed: ${industries.size} items")
            val selectedCount = industries.count { it.isSelected }
            Log.d(TAG, "Selected industries count: $selectedCount")

            adapter.submitList(industries)
            binding.loadingIndicator.visibility = View.GONE
            binding.errorText.visibility = View.GONE

            // Показываем кнопку если есть выбранные отрасли
            binding.selectButton.visibility = if (selectedCount > 0) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "Loading state: $isLoading")
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.d(TAG, "Error observed: $error")
            if (error != null) {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = error
                binding.loadingIndicator.visibility = View.GONE
            } else {
                binding.errorText.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
