package ru.practicum.android.diploma.domain.models

data class FilterParameters(
    var onlyWithSalary: Boolean = false,
    var salary: String = "",
    var industries: List<Industry> = emptyList()
)
