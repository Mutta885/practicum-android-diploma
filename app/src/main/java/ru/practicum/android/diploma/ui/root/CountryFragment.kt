package ru.practicum.android.diploma.ui.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentCountryBinding
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel
import ru.practicum.android.diploma.ui.models.FilterAreaState

class CountryFragment : Fragment(), CountryAdapter.CountryListener {

    private val viewModel: AreasViewModel by viewModel()
    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCountryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.returnButton.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.getCountries()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            if (state is FilterAreaState.CountriesState) {
                getCountries(state.countries)
            }
        }
    }

    private fun getCountries(countries: List<FilterArea>) {
        val recyclerView = binding.countriesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val countryAdapter = CountryAdapter(countries, this)
        recyclerView.adapter = countryAdapter
    }

    override fun onCountryClick(country: FilterArea) {
        val bundle = Bundle()
        bundle.putString("country_name", country.name)
        bundle.putInt("country_id", country.id!!)
        findNavController().navigate(R.id.workPlaceFragment, bundle)
    }
}
