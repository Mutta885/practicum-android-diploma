package ru.practicum.android.diploma.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentIndustryBinding
import ru.practicum.android.diploma.presentation.adapters.IndustryAdapter
import ru.practicum.android.diploma.presentation.vmodels.IndustryViewModel
import ru.practicum.android.diploma.presentation.vmodels.FiltrationViewModel

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        setupSearch()
        setupErrorHandling()
        observeViewModel()
        viewModel.loadIndustries()
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
            adapter.filter("") // Применяем фильтр с пустым запросом
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
                val presence = adapter.filter(text)
                if (!presence) {
                    renderGroupImageText(true, getString(R.string.this_not_industry))
                }
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
            if (industries.isNotEmpty()) {
                adapter.submitList(industries)
                binding.loadingIndicator.visibility = View.GONE
                binding.groupImageText.visibility = View.GONE
                binding.selectButton.visibility =
                    if (adapter.getSelectedIndustry() != null) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                binding.searchEditText.isEnabled = true
            } else {
                binding.searchEditText.isEnabled = false
                renderGroupImageText(value = true, string = getString(R.string.no_results))
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.searchEditText.isEnabled = false
            renderGroupImageText(false)
            binding.loadingIndicator.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.searchEditText.isEnabled = false
            if (error != null) {
                renderGroupImageText(value = true, string = error)
            }
        }
    }

    private fun renderGroupImageText(value: Boolean, string: String? = null) {
        binding.groupImageText.isVisible = value
        if (value) {
            when (string) {
                HTTP_NOT_INTERNET -> {
                    binding.errorText.text = getString(R.string.not_internet)
                    binding.imageError.setImageResource(R.drawable.image_yorik)
                }

                HTTP_SERVER_ERROR -> {
                    binding.errorText.text = getString(R.string.error_server)
                    binding.imageError.setImageResource(R.drawable.error_server)
                }

                HTTP_UNAUTHORIZED, HTTP_FORBIDDEN, HTTP_NOT_FOUND -> {
                    binding.errorText.text = getString(R.string.no_results)
                    binding.imageError.setImageResource(R.drawable.cover_samolet)
                }

                getString(R.string.this_not_industry) -> {
                    binding.errorText.text = string
                    binding.imageError.setImageResource(R.drawable.cat_with_the_plate)
                }

                else -> {
                    binding.errorText.text = string ?: getString(R.string.error_unknown)
                    binding.imageError.setImageResource(R.drawable.image_yorik)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val HTTP_NOT_INTERNET = "-1"
        private const val HTTP_UNAUTHORIZED = "401"
        private const val HTTP_FORBIDDEN = "403"
        private const val HTTP_NOT_FOUND = "404"
        private const val HTTP_SERVER_ERROR = "500"
    }
}
