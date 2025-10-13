package ru.practicum.android.diploma.ui.root

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentWorkPlaceBinding
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel
import ru.practicum.android.diploma.ui.models.FilterAreaState

class WorkPlaceFragment : Fragment() {

    private val viewModel: AreasViewModel by viewModel()
    private var _binding: FragmentWorkPlaceBinding? = null
    private var countryName: String? = null
    private var countryId: Int? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWorkPlaceBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        countryId = arguments?.getInt("country_id")
        countryName = arguments?.getString("country_name")
        val regionParentId = arguments?.getInt("region_parentId")
        val regionName = arguments?.getString("region_name")

        if (countryName?.isNotEmpty() == true) {
            binding.countryText.text = countryName
            binding.chooseButton.visibility = View.VISIBLE
        } else if (regionParentId != null) {
            viewModel.getCountryNameByRegion(regionParentId)
        }

        if (regionName?.isNotBlank() == true) {
            binding.regionText.text = regionName
        }

        observeViewModel()

        binding.countryButton.setOnClickListener {
            findNavController().navigate(R.id.action_workPlaceFragment_to_countryFragment)
        }

        binding.regionButton.setOnClickListener {
            val bundle = Bundle()
            if (countryId != null) {
                bundle.putInt("country_id", countryId!!)
                findNavController().navigate(R.id.action_workPlaceFragment_to_regionFragment, bundle)
            }
        }

        binding.chooseButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("country_name", countryName)
            bundle.putString("region_name", regionName)
            findNavController().navigate(R.id.action_workPlaceFragment_to_filtrationFragment, bundle)
        }

        binding.returnButton.setOnClickListener {
            findNavController().navigate(R.id.action_workPlaceFragment_to_filtrationFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            if (state is FilterAreaState.GetCountryNameState) {
                addCountryName(state.countryName, state.countryId)
            }
        }
    }

    private fun addCountryName(name: String, id: Int) {
        countryName = name
        countryId = id
        binding.countryText.text = countryName
        binding.chooseButton.visibility = View.VISIBLE
    }
}
