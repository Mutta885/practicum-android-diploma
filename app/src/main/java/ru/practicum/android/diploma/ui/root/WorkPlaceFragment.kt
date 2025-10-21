package ru.practicum.android.diploma.ui.root

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentWorkPlaceBinding
import ru.practicum.android.diploma.presentation.vmodels.AreasViewModel
import ru.practicum.android.diploma.presentation.vmodels.filter.FiltrationViewModel
import ru.practicum.android.diploma.ui.models.FilterAreaState

class WorkPlaceFragment : Fragment() {

    private val viewModel: AreasViewModel by viewModel()
    private val filtrationViewModel: FiltrationViewModel by activityViewModel()

    private var _binding: FragmentWorkPlaceBinding? = null
    private var countryName: String? = null
    private var countryId: Int? = null
    private var regionName: String? = null
    private var regionId: Int? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_INVALID_ID = -1
        private const val DEBUG_TAG = "WorkPlaceFragment"
        private const val COUNTRY_ID_KEY = "country_id"
        private const val COUNTRY_NAME_KEY = "country_name"
        private const val REGION_PARENT_ID_KEY = "region_parentId"
        private const val REGION_NAME_KEY = "region_name"
        private const val REGION_ID_KEY = "region_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkPlaceBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("$DEBUG_TAG: onViewCreated")

        restoreStateFromArguments()
        setupClickListeners()
        observeViewModel()
        updateUI()
    }

    private fun restoreStateFromArguments() {
        val args = arguments
        countryId = args?.getInt(COUNTRY_ID_KEY, ARG_INVALID_ID)?.takeIf { it != ARG_INVALID_ID }
        countryName = args?.getString(COUNTRY_NAME_KEY)
        val regionParentId = args?.getInt(REGION_PARENT_ID_KEY, ARG_INVALID_ID)?.takeIf { it != ARG_INVALID_ID }
        regionName = args?.getString(REGION_NAME_KEY)
        regionId = args?.getInt(REGION_ID_KEY, ARG_INVALID_ID)?.takeIf { it != ARG_INVALID_ID }

        println(
            "$DEBUG_TAG: " +
                "countryId: $countryId, " +
                "countryName: $countryName, " +
                "regionParentId: $regionParentId, " +
                "regionName: $regionName, " +
                "regionId: $regionId"
        )

        if (regionParentId != null && countryName.isNullOrEmpty()) {
            println("$DEBUG_TAG: Getting country name by region parentId: $regionParentId")
            viewModel.getCountryNameByRegion(regionParentId)
        } else {
            updateUI()
        }
    }

    private fun setupClickListeners() {
        println("$DEBUG_TAG: Setting up click listeners")

        binding.countryContainer.setOnClickListener {
            println("$DEBUG_TAG: COUNTRY CONTAINER CLICKED")
            findNavController().navigate(R.id.action_workPlaceFragment_to_countryFragment)
        }

        binding.countryButton.setOnClickListener {
            println("$DEBUG_TAG: COUNTRY BUTTON CLICKED")
            findNavController().navigate(R.id.action_workPlaceFragment_to_countryFragment)
        }

        binding.regionContainer.setOnClickListener {
            println("$DEBUG_TAG: REGION CONTAINER CLICKED")
            navigateToRegionFragment()
        }

        binding.regionButton.setOnClickListener {
            println("$DEBUG_TAG: REGION BUTTON CLICKED")
            navigateToRegionFragment()
        }

        binding.chooseButton.setOnClickListener {
            handleChooseButtonClick()
        }

        binding.returnButton.setOnClickListener {
            println("$DEBUG_TAG: RETURN BUTTON CLICKED")
            findNavController().navigate(R.id.action_workPlaceFragment_to_filtrationFragment)
        }
    }

    private fun navigateToRegionFragment() {
        val bundle = Bundle()
        if (countryId != null) {
            bundle.putInt(COUNTRY_ID_KEY, countryId!!)
            println("$DEBUG_TAG: Passing countryId to RegionFragment: $countryId")
        }
        findNavController().navigate(R.id.action_workPlaceFragment_to_regionFragment, bundle)
    }

    private fun handleChooseButtonClick() {
        println("$DEBUG_TAG: CHOOSE BUTTON CLICKED - country: $countryName, region: $regionName")

        if (countryName == null && regionName == null) {
            println("$DEBUG_TAG: Nothing selected, not saving")
            return
        }

        filtrationViewModel.onWorkplaceSelected(
            countryName = countryName,
            countryId = countryId,
            regionName = regionName,
            regionId = regionId
        )

        println("$DEBUG_TAG: Workplace saved, navigating to filtration fragment")
        findNavController().navigate(R.id.action_workPlaceFragment_to_filtrationFragment)
    }

    private fun observeViewModel() {
        viewModel.filterAreaState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FilterAreaState.GetCountryNameState -> {
                    addCountryName(state.countryName, state.countryId)
                }
                is FilterAreaState.Loading -> {
                    // Можно показать загрузку если нужно
                }
                is FilterAreaState.Error -> {
                    // Можно показать ошибку если нужно
                }
                is FilterAreaState.CountriesState -> {
                    // Не используется в WorkPlaceFragment
                }
                is FilterAreaState.RegionsStateByCountry -> {
                    // Не используется в WorkPlaceFragment
                }
            }
        }
    }

    private fun addCountryName(name: String, id: Int) {
        println("$DEBUG_TAG: addCountryName called - name: $name, id: $id")
        countryName = name
        countryId = id
        updateUI()
    }

    private fun updateUI() {
        println("$DEBUG_TAG: updateUI - countryName: $countryName, regionName: $regionName")

        binding.countryText.text = countryName ?: getString(R.string.country_text)
        binding.regionText.text = regionName ?: getString(R.string.region_text)

        updateChooseButtonVisibility()
    }

    private fun updateChooseButtonVisibility() {
        val shouldShowButton = countryName?.isNotEmpty() == true || regionName?.isNotEmpty() == true
        binding.chooseButton.visibility = if (shouldShowButton) View.VISIBLE else View.INVISIBLE
        println("$DEBUG_TAG: Choose button visibility: $shouldShowButton")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
