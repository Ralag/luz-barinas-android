package com.example.data.model

import androidx.annotation.Keep

import com.squareup.moshi.JsonClass

@androidx.annotation.Keep
@JsonClass(generateAdapter = true)
data class MonthlyWeeksResponse(
    val weeks: List<PacWeekPlan>
)
