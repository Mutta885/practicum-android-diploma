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
import ru.practicum.android.diploma.databinding.FragmentRegionBinding
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel
import ru.practicum.android.diploma.ui.models.FilterAreaState

class RegionFragment : Fragment(), RegionAdapter.RegionListener {

    private val viewModel: AreasViewModel by viewModel()
    private var _binding: FragmentRegionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRegionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val countryId = arguments?.getInt("country_id")
        viewModel.getRegions(countryId)
        observeViewModel()
        binding.returnButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            if (state is FilterAreaState.RegionsStateByCountry) {
                getRegionsByCountry(state.regions)
            }
        }
    }

    private fun getRegionsByCountry(regions: List<FilterArea>) {
        val recyclerView = binding.regionRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val regionAdapter = RegionAdapter(regions, this)
        recyclerView.adapter = regionAdapter
    }

    override fun onRegionClick(region: FilterArea) {
        val bundle = Bundle()
        bundle.putInt("region_parentId", region.parentId!!)
        bundle.putString("region_name", region.name)
        findNavController().navigate(R.id.workPlaceFragment, bundle)
    }
}
