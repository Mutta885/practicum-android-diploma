package ru.practicum.android.diploma.domain.models

data class FilterParameters(
    val onlyWithSalary: Boolean = false,
    val salary: String = "",
    val industries: List<Industry> = emptyList()
)
