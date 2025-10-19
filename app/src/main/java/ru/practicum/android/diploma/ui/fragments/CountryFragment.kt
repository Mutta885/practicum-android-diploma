package ru.practicum.android.diploma.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentCountryBinding
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.presentation.adapters.CountryAdapter
import ru.practicum.android.diploma.presentation.models.FilterAreaState
import ru.practicum.android.diploma.presentation.vmodels.RegionViewModel

class CountryFragment : Fragment(), CountryAdapter.CountryListener {

    private val viewModel: RegionViewModel by viewModel()
    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!
    private var countryAdapter: CountryAdapter? = null

    companion object {
        private const val COUNTRY_NAME_KEY = "country_name"
        private const val COUNTRY_ID_KEY = "country_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCountryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.getCountries()
    }

    private fun setupRecyclerView() {
        countryAdapter = CountryAdapter(emptyList(), this)
        binding.countriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.countriesRecyclerView.adapter = countryAdapter
    }

    private fun setupClickListeners() {
        binding.returnButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FilterAreaState.CountriesState -> {
                    showCountries(state.countries)
                }

                is FilterAreaState.Loading -> {
                    showLoading()
                }

                is FilterAreaState.Error -> {
                    showError(state.message)
                }

                is FilterAreaState.GetCountryNameState,
                is FilterAreaState.RegionsStateByCountry -> {
                    // Не используется в CountryFragment
                }
            }
        }
    }

    private fun showCountries(countries: List<FilterArea>) {
        countryAdapter?.updateCountries(countries)
        binding.countriesRecyclerView.visibility = View.VISIBLE
        binding.noResultsContainer.isVisible = false
        binding.loadingContainer.isVisible = false
    }

    private fun showLoading() {
        binding.countriesRecyclerView.visibility = View.GONE
        binding.noResultsContainer.isVisible = false
        binding.loadingContainer.isVisible = true
    }

    private fun showError(message: String) {
        binding.countriesRecyclerView.visibility = View.GONE
        binding.noResultsContainer.isVisible = true
        binding.loadingContainer.isVisible = false
        binding.noResultsContainer.text = message
    }

    override fun onCountryClick(country: FilterArea) {
        val bundle = Bundle().apply {
            putString(COUNTRY_NAME_KEY, country.name)
            putInt(COUNTRY_ID_KEY, country.id)
        }
        findNavController().navigate(R.id.action_countryFragment_to_workPlaceFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        countryAdapter = null
    }
}
