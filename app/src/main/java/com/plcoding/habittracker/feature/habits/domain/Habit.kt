package com.plcoding.habittracker.feature.habits.domain

import java.time.LocalDate

data class Habit(
    val id: Long,
    val name: String,
    val icon: HabitIcon,
    val weekDays: WeekDays,
    val createdAt: LocalDate
)
