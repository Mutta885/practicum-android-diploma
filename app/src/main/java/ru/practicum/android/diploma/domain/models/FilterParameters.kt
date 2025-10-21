package ru.practicum.android.diploma.domain.models

data class FilterParameters(
    val onlyWithSalary: Boolean = false,
    val salary: String = "",
    val industries: List<Industry> = emptyList(),
    val country: String? = null,
    val countryId: String? = null,
    val region: String? = null,
    val regionId: String? = null
)
