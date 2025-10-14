package ru.practicum.android.diploma.data.dto

import ru.practicum.android.diploma.domain.models.FilterArea

fun FilterAreaDto.toDomain(): FilterArea {
    return FilterArea(
        id = this.id,
        name = this.name,
        parentId = this.parentId,
        areas = this.areas?.map { it.toDomain() } ?: emptyList()
    )
}
