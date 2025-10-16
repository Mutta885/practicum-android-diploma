package ru.practicum.android.diploma.ui.industry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.databinding.FragmentIndustryBinding
import ru.practicum.android.diploma.presentation.industry.IndustryAdapter
import ru.practicum.android.diploma.presentation.industry.IndustryViewModel
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel

class IndustryFragment : Fragment() {

    private var _binding: FragmentIndustryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IndustryViewModel by viewModel()
    private val filtrationViewModel: FiltrationViewModel by activityViewModel()

    private val adapter: IndustryAdapter by lazy {
        IndustryAdapter {
            binding.selectButton.visibility =
                if (adapter.getSelectedIndustry() != null) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndustryBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupButtons()
        setupSearch()
        setupErrorHandling()
        observeViewModel()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        binding.returnButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.selectButton.setOnClickListener {
            adapter.getSelectedIndustry()?.let {
                filtrationViewModel.onIndustriesSelected(listOf(it))
            }
            requireActivity().onBackPressed()
        }
    }

    private fun setupSearch() {
        val editText = binding.searchEditText
        val clearIcon = binding.clearIcon

        clearIcon.setOnClickListener {
            editText.text.clear()
            adapter.submitList(adapter.getCurrentList())
            clearIcon.visibility = View.GONE
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable) {
                val text = s.toString().trim()
                clearIcon.visibility = if (text.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                adapter.filter(text)
            }
        })

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                adapter.filter(editText.text.toString().trim())
                true
            } else {
                false
            }
        }
    }

    private fun setupErrorHandling() {
        binding.errorText.setOnClickListener { viewModel.refresh() }
    }

    private fun observeViewModel() {
        viewModel.industries.observe(viewLifecycleOwner) { industries ->
            adapter.submitList(industries)
            binding.loadingIndicator.visibility = View.GONE
            binding.groupImageText.visibility = View.GONE
            binding.selectButton.visibility =
                if (adapter.getSelectedIndustry() != null) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.groupImageText.visibility = if (error != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.errorText.text = error ?: ""
            if (error != null) {
                binding.loadingIndicator.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
