package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentRegionBinding
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel
import ru.practicum.android.diploma.ui.models.FilterAreaState

class RegionFragment : Fragment(), RegionAdapter.RegionListener {

    private val viewModel: AreasViewModel by viewModel()
    private var _binding: FragmentRegionBinding? = null
    private val binding get() = _binding!!
    private var regionAdapter: RegionAdapter? = null
    private var allRegions: List<FilterArea> = emptyList()

    companion object {
        private const val DEBUG_TAG = "RegionFragment"
        private const val COUNTRY_ID_KEY = "country_id"
        private const val REGION_NAME_KEY = "region_name"
        private const val REGION_ID_KEY = "region_id"
        private const val REGION_PARENT_ID_KEY = "region_parentId"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchField()
        setupClickListeners()
        observeViewModel()

        val countryId = arguments?.getInt(COUNTRY_ID_KEY)
        println("$DEBUG_TAG: received countryId: $countryId")
        viewModel.getRegions(countryId)
    }

    private fun setupRecyclerView() {
        regionAdapter = RegionAdapter(emptyList(), this)
        binding.regionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.regionRecyclerView.adapter = regionAdapter
    }

    private fun setupSearchField() {
        val editText = binding.searchEditText
        val clearIcon = binding.clearIcon

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()

                if (text.isNotEmpty()) {
                    clearIcon.visibility = View.VISIBLE
                } else {
                    clearIcon.visibility = View.GONE
                }

                filterRegions(text)
            }
        })

        clearIcon.setOnClickListener {
            editText.text.clear()
            filterRegions("")
            clearIcon.visibility = View.GONE
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editText.text.toString().trim()
                filterRegions(query)
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        binding.returnButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun filterRegions(query: String) {
        val filtered = if (query.isEmpty()) {
            allRegions
        } else {
            allRegions.filter { it.name.contains(query, ignoreCase = true) }
        }

        regionAdapter?.updateRegions(filtered)

        // Показываем состояние "ничего не найдено" если после фильтрации список пуст
        if (filtered.isEmpty() && query.isNotEmpty()) {
            showNoResultsState()
        } else if (filtered.isEmpty()) {
            // Если изначально список пуст (нет регионов для этой страны)
            showNoResultsState()
        } else {
            showSuccessState()
        }
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            Log.v("my", "RegionFragment state = $state")
            when (state) {
                is FilterAreaState.RegionsStateByCountry -> {
                    if (state.regions.isNotEmpty()) {
                        showRegions(state.regions)
                    } else {
                        showNoResultsState()
                    }
                }
                is FilterAreaState.Loading -> {
                    showLoadingState()
                }
                is FilterAreaState.Error -> {
                    showErrorState(state.message)
                }
                is FilterAreaState.CountriesState,
                is FilterAreaState.GetCountryNameState -> {
                    // Не используется в этом фрагменте
                }
            }
        }
    }

    private fun showRegions(regions: List<FilterArea>) {
        regions.forEach { region ->
            println("$DEBUG_TAG: Region: ${region.name}, parentId: ${region.parentId}")
        }

        allRegions = regions
        regionAdapter?.updateRegions(regions)
        showSuccessState()
    }

    private fun showLoadingState() {
        binding.loadingContainer.visibility = View.VISIBLE
        binding.errorContainer.visibility = View.GONE
        binding.noResultsContainer.visibility = View.GONE
        binding.successContainer.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.errorText.text = message
        binding.loadingContainer.visibility = View.GONE
        binding.errorContainer.visibility = View.VISIBLE
        binding.noResultsContainer.visibility = View.GONE
        binding.successContainer.visibility = View.GONE
    }

    private fun showNoResultsState() {
        binding.loadingContainer.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE
        binding.noResultsContainer.visibility = View.VISIBLE
        binding.successContainer.visibility = View.GONE
    }

    private fun showSuccessState() {
        binding.loadingContainer.visibility = View.GONE
        binding.errorContainer.visibility = View.GONE
        binding.noResultsContainer.visibility = View.GONE
        binding.successContainer.visibility = View.VISIBLE
    }

    override fun onRegionClick(region: FilterArea) {
        val countryIdFromArgs = arguments?.getInt(COUNTRY_ID_KEY)

        val bundle = Bundle().apply {
            putString(REGION_NAME_KEY, region.name)
            putInt(REGION_ID_KEY, region.id)
            if (region.parentId != null) {
                putInt(REGION_PARENT_ID_KEY, region.parentId)
            }
            if (countryIdFromArgs != null) {
                putInt(COUNTRY_ID_KEY, countryIdFromArgs)
                println("$DEBUG_TAG: Passing back countryId to WorkPlaceFragment: $countryIdFromArgs")
            }
        }

        findNavController().navigate(R.id.action_regionFragment_to_workPlaceFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        regionAdapter = null
    }
}
