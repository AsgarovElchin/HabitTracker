package com.plcoding.habittracker.feature.habits.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object SampleDataSeeder {

    suspend fun seed(dao: HabitDao) {
        val today = LocalDate.now()
        val sixWeeksAgo = today.minusWeeks(6)

        data class HabitDef(
            val name: String,
            val icon: String,
            val days: Set<DayOfWeek>,
            val createdAt: LocalDate,
            val completionPattern: (LocalDate) -> Boolean,
        )

        val habits = listOf(
            HabitDef(
                name = "Morning Run",
                icon = "RUN",
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                createdAt = sixWeeksAgo,
                completionPattern = { date -> ChronoUnit.DAYS.between(date, today) < 14 },
            ),
            HabitDef(
                name = "Read 30 min",
                icon = "READ",
                days = DayOfWeek.entries.toSet(),
                createdAt = sixWeeksAgo.plusWeeks(1),
                completionPattern = { date -> date.dayOfYear % 2 == 0 },
            ),
            HabitDef(
                name = "Meditate",
                icon = "MEDITATE",
                days = setOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                ),
                createdAt = sixWeeksAgo,
                completionPattern = { true },
            ),
            HabitDef(
                name = "Drink 2L Water",
                icon = "WATER",
                days = DayOfWeek.entries.toSet(),
                createdAt = sixWeeksAgo.plusWeeks(2),
                completionPattern = { false },
            ),
            HabitDef(
                name = "Code 1 hour",
                icon = "CODE",
                days = setOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                ),
                createdAt = sixWeeksAgo,
                completionPattern = { date -> date.dayOfYear % 3 == 0 },
            ),
            HabitDef(
                name = "Go to Gym",
                icon = "GYM",
                days = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                createdAt = sixWeeksAgo.plusWeeks(3),
                completionPattern = { true },
            ),
            HabitDef(
                name = "Journal",
                icon = "JOURNAL",
                days = DayOfWeek.entries.toSet(),
                createdAt = sixWeeksAgo,
                completionPattern = { date -> ChronoUnit.DAYS.between(sixWeeksAgo, date) < 14 },
            ),
        )

        for (def in habits) {
            val habitId = dao.upsertHabit(
                HabitEntity(
                    name = def.name,
                    icon = def.icon,
                    monday = DayOfWeek.MONDAY in def.days,
                    tuesday = DayOfWeek.TUESDAY in def.days,
                    wednesday = DayOfWeek.WEDNESDAY in def.days,
                    thursday = DayOfWeek.THURSDAY in def.days,
                    friday = DayOfWeek.FRIDAY in def.days,
                    saturday = DayOfWeek.SATURDAY in def.days,
                    sunday = DayOfWeek.SUNDAY in def.days,
                    createdAt = def.createdAt.toEpochDay(),
                )
            )

            var day = def.createdAt
            while (!day.isAfter(today)) {
                if (day.dayOfWeek in def.days && def.completionPattern(day)) {
                    dao.insertCompletion(
                        HabitCompletionEntity(habitId = habitId, date = day.toEpochDay())
                    )
                }
                day = day.plusDays(1)
            }
        }
    }
}
