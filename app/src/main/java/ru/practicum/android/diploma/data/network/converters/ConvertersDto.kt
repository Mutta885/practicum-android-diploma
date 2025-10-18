package ru.practicum.android.diploma.data.network.converters

import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.domain.models.FilterArea

class ConvertersDto {
    fun map(filterAreaDto: FilterAreaDto): FilterArea {
        return with(filterAreaDto) {
            FilterArea(
                id = id,
                name = name,
                parentId = parentId,
                areas = areas?.map {
                    map(it)
                }?: listOf()
            )
        }
    }
}
