package ru.practicum.android.diploma.ui.industry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private val adapter: IndustryAdapter by lazy {
        IndustryAdapter { binding.selectButton.visibility = View.VISIBLE }
    }

    companion object {
        private const val TAG = "IndustryFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndustryBinding.inflate(inflater, container, false)

        setupViews()
        observeViewModel()

        return binding.root
    }

    private fun setupViews() {
        setupButtons()
        setupRecyclerView()
        setupSearch()
        setupErrorHandling()
    }

    private fun setupButtons() {
        binding.returnButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.selectButton.setOnClickListener {
            val selectedIndustries = viewModel.getSelectedIndustries()
            filtrationViewModel.onIndustriesSelected(selectedIndustries)
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        val editText = binding.searchEditText
        val searchIcon = binding.searchIcon
        val clearIcon = binding.clearIcon

        clearIcon.setOnClickListener {
            editText.text.clear()
            viewModel.search("")
            clearIcon.visibility = View.GONE
            searchIcon.visibility = View.VISIBLE
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable) {
                val text = s.toString().trim()
                searchIcon.visibility = if (text.isNotEmpty()) View.GONE else View.VISIBLE
                clearIcon.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.search(text)
            }
        })

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(editText.text.toString().trim())
                true
            } else {
                false
            }
        }
    }

    private fun setupErrorHandling() {
        binding.errorText.setOnClickListener { viewModel.retry() }
    }

    private fun observeViewModel() {
        viewModel.industries.observe(viewLifecycleOwner) { industries ->
            val selectedCount = industries.count { it.isSelected }
            adapter.submitList(industries)
            binding.loadingIndicator.visibility = View.GONE
            binding.errorText.visibility = View.GONE
            binding.selectButton.visibility = if (selectedCount > 0) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
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
