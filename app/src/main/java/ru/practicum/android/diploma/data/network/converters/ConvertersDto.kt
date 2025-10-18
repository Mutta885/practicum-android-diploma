package ru.practicum.android.diploma.data.network.converters

import ru.practicum.android.diploma.data.dto.FilterAreaDto
import ru.practicum.android.diploma.data.dto.IndustryDto
import ru.practicum.android.diploma.domain.models.FilterArea
import ru.practicum.android.diploma.domain.models.Industry

class ConvertersDto {
    fun map(filterAreaDto: FilterAreaDto): FilterArea {
        return with(filterAreaDto) {
            FilterArea(
                id = id,
                name = name,
                parentId = parentId,
                areas = areas?.map {
                    map(it)
                } ?: listOf()
            )
        }
    }

    fun map(industryDto: IndustryDto): Industry {
        return with(industryDto) {
            Industry(
                id = id,
                name = name
            )
        }
    }
}
