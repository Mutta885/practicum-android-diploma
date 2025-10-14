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

        println("DEBUG: WorkPlaceFragment onViewCreated")

        // Восстановление состояния из аргументов
        restoreStateFromArguments()
        setupClickListeners()
        observeViewModel()
        updateUI()
    }

    private fun restoreStateFromArguments() {
        val args = arguments
        countryId = args?.getInt("country_id", -1)?.takeIf { it != -1 }
        countryName = args?.getString("country_name")
        val regionParentId = args?.getInt("region_parentId", -1)?.takeIf { it != -1 }
        regionName = args?.getString("region_name")
        regionId = args?.getInt("region_id", -1)?.takeIf { it != -1 }

        // Логируем полученные данные
        println("DEBUG: WorkPlaceFragment - countryId: $countryId, countryName: $countryName, regionParentId: $regionParentId, regionName: $regionName, regionId: $regionId")

        // Если пришел регион, но нет страны - получаем страну по parentId региона
        if (regionParentId != null && countryName.isNullOrEmpty()) {
            println("DEBUG: Getting country name by region parentId: $regionParentId")
            viewModel.getCountryNameByRegion(regionParentId)
        } else {
            updateUI()
        }
    }

    private fun setupClickListeners() {
        println("DEBUG: Setting up click listeners")

        // Контейнер страны
        binding.countryContainer.setOnClickListener {
            println("DEBUG: COUNTRY CONTAINER CLICKED")
            findNavController().navigate(R.id.action_workPlaceFragment_to_countryFragment)
        }

        // Кнопка страны (стрелка)
        binding.countryButton.setOnClickListener {
            println("DEBUG: COUNTRY BUTTON CLICKED")
            findNavController().navigate(R.id.action_workPlaceFragment_to_countryFragment)
        }

        // Контейнер региона
        binding.regionContainer.setOnClickListener {
            println("DEBUG: REGION CONTAINER CLICKED")
            val bundle = Bundle()
            // Всегда передаем countryId если он есть
            if (countryId != null) {
                bundle.putInt("country_id", countryId!!)
                println("DEBUG: Passing countryId to RegionFragment: $countryId")
            }
            findNavController().navigate(R.id.action_workPlaceFragment_to_regionFragment, bundle)
        }

        // Кнопка региона (стрелка)
        binding.regionButton.setOnClickListener {
            println("DEBUG: REGION BUTTON CLICKED")
            val bundle = Bundle()
            if (countryId != null) {
                bundle.putInt("country_id", countryId!!)
                println("DEBUG: Passing countryId to RegionFragment: $countryId")
            }
            findNavController().navigate(R.id.action_workPlaceFragment_to_regionFragment, bundle)
        }

        // Кнопка "Выбрать" - ИСПРАВЛЕНО: используем явное действие навигации
        binding.chooseButton.setOnClickListener {
            println("DEBUG: CHOOSE BUTTON CLICKED - country: $countryName, region: $regionName")

            // Проверяем, что что-то выбрано
            if (countryName == null && regionName == null) {
                println("DEBUG: Nothing selected, not saving")
                return@setOnClickListener
            }

            // Сохраняем выбранное место работы в FiltrationViewModel
            filtrationViewModel.onWorkplaceSelected(
                countryName = countryName,
                countryId = countryId,
                regionName = regionName,
                regionId = regionId
            )

            println("DEBUG: Workplace saved, navigating to filtration fragment")
            // ИСПРАВЛЕНО: Явный переход в FiltrationFragment
            findNavController().navigate(R.id.action_workPlaceFragment_to_filtrationFragment)
        }

        // Кнопка возврата - ИСПРАВЛЕНО: используем явное действие навигации
        binding.returnButton.setOnClickListener {
            println("DEBUG: RETURN BUTTON CLICKED")
            // ИСПРАВЛЕНО: Явный переход в FiltrationFragment
            findNavController().navigate(R.id.action_workPlaceFragment_to_filtrationFragment)
        }
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
        println("DEBUG: addCountryName called - name: $name, id: $id")
        countryName = name
        countryId = id
        updateUI()
    }

    private fun updateUI() {
        println("DEBUG: updateUI - countryName: $countryName, regionName: $regionName")

        // Обновляем тексты
        countryName?.let {
            binding.countryText.text = it
        } ?: run {
            binding.countryText.text = getString(R.string.country_text)
        }

        regionName?.let {
            binding.regionText.text = it
        } ?: run {
            binding.regionText.text = getString(R.string.region_text)
        }

        // Обновляем видимость кнопки "Выбрать"
        updateChooseButtonVisibility()
    }

    private fun updateChooseButtonVisibility() {
        val shouldShowButton = countryName?.isNotEmpty() == true || regionName?.isNotEmpty() == true
        binding.chooseButton.visibility = if (shouldShowButton) View.VISIBLE else View.INVISIBLE
        println("DEBUG: Choose button visibility: $shouldShowButton")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
