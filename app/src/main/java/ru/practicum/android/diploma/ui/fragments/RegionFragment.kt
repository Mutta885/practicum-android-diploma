package ru.practicum.android.diploma.ui.fragments

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
import ru.practicum.android.diploma.presentation.vmodels.RegionViewModel
import ru.practicum.android.diploma.presentation.models.FilterAreaState
import ru.practicum.android.diploma.presentation.adapters.RegionAdapter

class RegionFragment : Fragment(), RegionAdapter.RegionListener {

    private val viewModel: RegionViewModel by viewModel()
    private var _binding: FragmentRegionBinding? = null
    private val binding get() = _binding!!
    private var regionAdapter: RegionAdapter? = null
    private var allRegions: List<FilterArea> = emptyList()
    // Карта для хранения стран по ID
    private var countryMap: Map<Int, FilterArea> = emptyMap()
    // Флаг, указывающий, что список стран загружен
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

        // ОБНОВЛЕНИЕ: обрабатываем ARG_INVALID_ID как null
        val countryId = arguments?.getInt(COUNTRY_ID_KEY, ARG_INVALID_ID)
        val actualCountryId = if (countryId == ARG_INVALID_ID) null else countryId

        println("$DEBUG_TAG: received countryId: $countryId, actualCountryId: $actualCountryId")

        // Если страна не передана или передан недопустимый ID - загружаем список всех стран
        if (actualCountryId == null) {
            viewModel.getCountries()
        } else {
            viewModel.getRegions(actualCountryId)
        }
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
                is FilterAreaState.CountriesState -> {
                    // Сохраняем страны в карту для быстрого поиска
                    countryMap = state.countries.associateBy { it.id }
                    countriesLoaded = true

                    // Если мы загружали страны для отображения всех регионов, загружаем регионы
                    val countryId = arguments?.getInt(COUNTRY_ID_KEY, ARG_INVALID_ID)
                    val actualCountryId = if (countryId == ARG_INVALID_ID) null else countryId
                    if (actualCountryId == null) {
                        viewModel.getRegions(actualCountryId)
                    }
                }
                is FilterAreaState.Loading -> {
                    showLoadingState()
                }
                is FilterAreaState.Error -> {
                    showErrorState(state.message)
                }
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
        println("$DEBUG_TAG: Region clicked: ${region.name}, id: ${region.id}, parentId: ${region.parentId}")

        // ОБНОВЛЕНИЕ: обрабатываем случай, когда countryIdFromArgs равен ARG_INVALID_ID
        val countryIdFromArgs = arguments?.getInt(COUNTRY_ID_KEY, ARG_INVALID_ID)
        val actualCountryIdFromArgs = if (countryIdFromArgs == ARG_INVALID_ID) null else countryIdFromArgs
        println("$DEBUG_TAG: countryIdFromArgs: $countryIdFromArgs, actualCountryIdFromArgs: $actualCountryIdFromArgs")

        // Если страна была выбрана, используем ее ID
        var countryIdToPass = actualCountryIdFromArgs ?: region.parentId
        println("$DEBUG_TAG: countryIdToPass (initial): $countryIdToPass")

        var countryName: String? = null

        // Если countryIdToPass все еще null, пытаемся определить страну
        if (countryIdToPass == null) {
            println("$DEBUG_TAG: Trying to determine country from countryMap")
            // Попробуем найти страну, которая содержит этот регион
            for (country in countryMap.values) {
                if (country.areas.any { it.id == region.id }) {
                    countryIdToPass = country.id
                    countryName = country.name
                    println("$DEBUG_TAG: Found country: $countryName (id: $countryIdToPass)")
                    break
                }
            }

            // Если не удалось найти страну по региону, используем родительский регион (если есть)
            if (countryIdToPass == null && region.parentId != null) {
                countryIdToPass = region.parentId
                countryName = countryMap[countryIdToPass]?.name
                println("$DEBUG_TAG: Using region.parentId: $countryIdToPass, countryName: $countryName")
            }
        } else {
            // Получаем название страны из карты
            countryName = countryMap[countryIdToPass]?.name
            println("$DEBUG_TAG: countryName from countryMap: $countryName")
        }

        // Если все еще не удалось определить страну, выходим
        if (countryIdToPass == null) {
            println("$DEBUG_TAG: Cannot determine country for region: ${region.name}")
            return
        }

        println("$DEBUG_TAG: Final countryIdToPass: $countryIdToPass, countryName: $countryName")

        val bundle = Bundle().apply {
            putString(REGION_NAME_KEY, region.name)
            putInt(REGION_ID_KEY, region.id)
            if (region.parentId != null) {
                putInt(REGION_PARENT_ID_KEY, region.parentId)
            }
            putInt(COUNTRY_ID_KEY, countryIdToPass)
            // Передаем название страны, если оно известно
            if (countryName != null) {
                putString(COUNTRY_NAME_KEY, countryName)
            }
        }

        findNavController().navigate(R.id.action_regionFragment_to_workPlaceFragment, bundle)
        println("$DEBUG_TAG: Navigated to WorkPlaceFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        regionAdapter = null
    }

    companion object {
        private const val DEBUG_TAG = "RegionFragment"
        private const val COUNTRY_ID_KEY = "country_id"
        private const val REGION_NAME_KEY = "region_name"
        private const val REGION_ID_KEY = "region_id"
        private const val REGION_PARENT_ID_KEY = "region_parentId"
        private const val COUNTRY_NAME_KEY = "country_name"
        private const val ARG_INVALID_ID = -1 // Добавляем константу для обработки недопустимых ID
    }
}
