package com.plcoding.habittracker.feature.habits.data

import com.plcoding.habittracker.feature.habits.domain.CompletionRecord
import com.plcoding.habittracker.feature.habits.domain.Habit
import com.plcoding.habittracker.feature.habits.domain.HabitIcon
import com.plcoding.habittracker.feature.habits.domain.WeekDays
import java.time.LocalDate

fun HabitEntity.toHabit(): Habit {
    return Habit(
        id = id,
        name = name,
        icon = HabitIcon.valueOf(icon),
        weekDays = WeekDays(
            monday = monday,
            tuesday = tuesday,
            wednesday = wednesday,
            thursday = thursday,
            friday = friday,
            saturday = saturday,
            sunday = sunday
        ),
        createdAt = LocalDate.ofEpochDay(createdAt)
    )
}

fun Habit.toHabitEntity(): HabitEntity {
    return HabitEntity(
        id = id,
        name = name,
        icon = icon.name,
        monday = weekDays.monday,
        tuesday = weekDays.tuesday,
        wednesday = weekDays.wednesday,
        thursday = weekDays.thursday,
        friday = weekDays.friday,
        saturday = weekDays.saturday,
        sunday = weekDays.sunday,
        createdAt = createdAt.toEpochDay()
    )
}

fun HabitCompletionRaw.toCompletionRecord(): CompletionRecord {
    return CompletionRecord(
        habitId = habitId,
        date = LocalDate.ofEpochDay(date)
    )
}
