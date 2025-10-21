package ru.practicum.android.diploma.data.dto

import com.google.gson.annotations.SerializedName

data class FilterAreaDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("parent_id")
    val parentId: Int?,
    @SerializedName("areas")
    val areas: List<FilterAreaDto>?
)
