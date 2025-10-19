package ru.practicum.android.diploma.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import ru.practicum.android.diploma.presentation.adapters.RegionAdapter
import ru.practicum.android.diploma.presentation.models.FilterAreaState
import ru.practicum.android.diploma.presentation.vmodels.RegionViewModel

class RegionFragment : Fragment(), RegionAdapter.RegionListener {

    private val viewModel: RegionViewModel by viewModel()
    private var _binding: FragmentRegionBinding? = null
    private val binding get() = _binding!!
    private var regionAdapter: RegionAdapter? = null
    private var allRegions: List<FilterArea> = emptyList()
    private var countryMap: Map<Int, FilterArea> = emptyMap()
    private var countriesLoaded = false

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
        loadInitialData()
    }

    private fun setupRecyclerView() {
        regionAdapter = RegionAdapter(emptyList(), this)
        binding.regionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.regionRecyclerView.adapter = regionAdapter
    }

    private fun setupSearchField() {
        val editText = binding.searchEditText
        val clearIcon = binding.clearIcon

        editText.addTextChangedListener(createTextWatcher(clearIcon))

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

    private fun createTextWatcher(clearIcon: View): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                updateClearIconVisibility(clearIcon, text)
                filterRegions(text)
            }
        }
    }

    private fun updateClearIconVisibility(clearIcon: View, text: String) {
        clearIcon.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
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
        updateResultsState(filtered, query)
    }

    private fun updateResultsState(filteredRegions: List<FilterArea>, query: String) {
        when {
            filteredRegions.isEmpty() && query.isNotEmpty() -> showNoResultsState()
            filteredRegions.isEmpty() -> showNoResultsState()
            else -> showSuccessState()
        }
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            handleFilterAreaState(state)
        }
    }

    private fun handleFilterAreaState(state: FilterAreaState) {
        when (state) {
            is FilterAreaState.RegionsStateByCountry -> handleRegionsState(state)
            is FilterAreaState.CountriesState -> handleCountriesState(state)
            is FilterAreaState.Loading -> showLoadingState()
            is FilterAreaState.Error -> showErrorState(state.message)
            is FilterAreaState.GetCountryNameState -> Unit // Не используется в этом фрагменте
        }
    }

    private fun handleRegionsState(state: FilterAreaState.RegionsStateByCountry) {
        if (state.regions.isNotEmpty()) {
            showRegions(state.regions)
        } else {
            showNoResultsState()
        }
    }

    private fun handleCountriesState(state: FilterAreaState.CountriesState) {
        countryMap = state.countries.associateBy { it.id }
        countriesLoaded = true

        val countryId = getCountryIdFromArguments()
        if (countryId == null) {
            viewModel.getRegions(countryId)
        }
    }

    private fun loadInitialData() {
        val countryId = getCountryIdFromArguments()
        if (countryId == null) {
            viewModel.getCountries()
        } else {
            viewModel.getRegions(countryId)
        }
    }

    private fun getCountryIdFromArguments(): Int? {
        val countryId = arguments?.getInt(COUNTRY_ID_KEY, ARG_INVALID_ID)
        return if (countryId == ARG_INVALID_ID) null else countryId
    }

    private fun showRegions(regions: List<FilterArea>) {
        allRegions = regions
        regionAdapter?.updateRegions(regions)
        showSuccessState()
    }

    private fun showLoadingState() {
        with(binding) {
            loadingContainer.visibility = View.VISIBLE
            errorContainer.visibility = View.GONE
            noResultsContainer.visibility = View.GONE
            successContainer.visibility = View.GONE
        }
    }

    private fun showErrorState(message: String) {
        with(binding) {
            errorText.text = message
            loadingContainer.visibility = View.GONE
            errorContainer.visibility = View.VISIBLE
            noResultsContainer.visibility = View.GONE
            successContainer.visibility = View.GONE
        }
    }

    private fun showNoResultsState() {
        with(binding) {
            loadingContainer.visibility = View.GONE
            errorContainer.visibility = View.GONE
            noResultsContainer.visibility = View.VISIBLE
            successContainer.visibility = View.GONE
        }
    }

    private fun showSuccessState() {
        with(binding) {
            loadingContainer.visibility = View.GONE
            errorContainer.visibility = View.GONE
            noResultsContainer.visibility = View.GONE
            successContainer.visibility = View.VISIBLE
        }
    }

    override fun onRegionClick(region: FilterArea) {
        val countryInfo = prepareCountryInfoForRegion(region)
        navigateToWorkPlaceFragment(region, countryInfo)
    }

    private fun prepareCountryInfoForRegion(region: FilterArea): CountryInfo {
        val countryIdFromArgs = getCountryIdFromArguments()

        var countryIdToPass = countryIdFromArgs ?: region.parentId
        var countryName: String? = null

        if (countryIdToPass == null) {
            val foundCountry = findCountryForRegion(region)
            countryIdToPass = foundCountry?.id ?: region.parentId
            countryName = foundCountry?.name
        } else {
            countryName = countryMap[countryIdToPass]?.name
        }

        return CountryInfo(countryIdToPass, countryName)
    }

    private fun findCountryForRegion(region: FilterArea): FilterArea? {
        for (country in countryMap.values) {
            if (country.areas.any { it.id == region.id }) {
                return country
            }
        }
        return null
    }

    private fun navigateToWorkPlaceFragment(region: FilterArea, countryInfo: CountryInfo) {
        if (countryInfo.id == null) return

        val bundle = createNavigationBundle(region, countryInfo)
        findNavController().navigate(R.id.action_regionFragment_to_workPlaceFragment, bundle)
    }

    private fun createNavigationBundle(region: FilterArea, countryInfo: CountryInfo): Bundle {
        return Bundle().apply {
            putString(REGION_NAME_KEY, region.name)
            putInt(REGION_ID_KEY, region.id)
            region.parentId?.let { putInt(REGION_PARENT_ID_KEY, it) }
            countryInfo.id?.let { putInt(COUNTRY_ID_KEY, it) }
            countryInfo.name?.let { putString(COUNTRY_NAME_KEY, it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        regionAdapter = null
    }

    private data class CountryInfo(val id: Int?, val name: String?)

    companion object {
        private const val DEBUG_TAG = "RegionFragment"
        private const val COUNTRY_ID_KEY = "country_id"
        private const val REGION_NAME_KEY = "region_name"
        private const val REGION_ID_KEY = "region_id"
        private const val REGION_PARENT_ID_KEY = "region_parentId"
        private const val COUNTRY_NAME_KEY = "country_name"
        private const val ARG_INVALID_ID = -1
    }
}
