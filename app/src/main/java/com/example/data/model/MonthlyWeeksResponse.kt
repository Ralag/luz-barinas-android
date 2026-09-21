package com.example.data.model

import androidx.annotation.Keep

@Keep
data class MonthlyWeeksResponse(
    val weeks: List<PacWeekPlan>
)
