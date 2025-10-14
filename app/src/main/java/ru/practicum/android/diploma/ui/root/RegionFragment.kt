package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
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
    private lateinit var regionAdapter: RegionAdapter
    private var allRegions: List<FilterArea> = emptyList()

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
        setupSearchView()
        setupClickListeners()
        observeViewModel()

        val countryId = arguments?.getInt("country_id")
        println("DEBUG: RegionFragment - received countryId: $countryId")
        viewModel.getRegions(countryId)
    }

    private fun setupRecyclerView() {
        regionAdapter = RegionAdapter(emptyList(), this)
        binding.regionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.regionRecyclerView.adapter = regionAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterRegions(newText.orEmpty())
                return true
            }
        })
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
        regionAdapter.updateRegions(filtered)
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FilterAreaState.RegionsStateByCountry -> {
                    showRegions(state.regions)
                }
                is FilterAreaState.Loading -> {
                    showLoading()
                }
                is FilterAreaState.Error -> {
                    showError(state.message)
                }
                is FilterAreaState.CountriesState -> {
                    // Не используется в этом фрагменте
                }
                is FilterAreaState.GetCountryNameState -> {
                    // Не используется в этом фрагменте
                }
            }
        }
    }

    private fun showRegions(regions: List<FilterArea>) {
        // Логируем для отладки
        regions.forEach { region ->
            println("DEBUG: Region: ${region.name}, parentId: ${region.parentId}")
        }

        allRegions = regions
        regionAdapter.updateRegions(regions)
        binding.regionRecyclerView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        // Показать индикатор загрузки
        binding.regionRecyclerView.visibility = View.GONE
    }

    private fun showError(message: String) {
        // Показать состояние ошибки
        binding.regionRecyclerView.visibility = View.GONE
    }

    override fun onRegionClick(region: FilterArea) {
        // Получаем countryId из аргументов (переданный из WorkPlaceFragment)
        val countryIdFromArgs = arguments?.getInt("country_id")

        val bundle = Bundle().apply {
            putString("region_name", region.name)
            // Добавляем parentId региона
            if (region.parentId != null) {
                putInt("region_parentId", region.parentId)
            }
            // ВАЖНО: передаем countryId обратно в WorkPlaceFragment
            if (countryIdFromArgs != null) {
                putInt("country_id", countryIdFromArgs)
                println("DEBUG: Passing back countryId to WorkPlaceFragment: $countryIdFromArgs")
            }
        }

        findNavController().navigate(R.id.action_regionFragment_to_workPlaceFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
