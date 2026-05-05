package com.plcoding.habittracker.feature.habits.domain

import java.time.LocalDate

object StreakCalculator {

    fun computeCurrentStreak(
        habit: Habit,
        completedDates: Set<LocalDate>,
        today: LocalDate
    ): Int {
        val isTodayScheduled = habit.weekDays.isScheduledFor(today.dayOfWeek)
        val isTodayCompleted = today in completedDates
        var current = if (isTodayScheduled && !isTodayCompleted) today.minusDays(1) else today
        var streak = 0
        while (!current.isBefore(habit.createdAt)) {
            if (!habit.weekDays.isScheduledFor(current.dayOfWeek)) {
                current = current.minusDays(1)
                continue
            }
            if (current in completedDates) {
                streak++
                current = current.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    fun computeBestStreak(
        habit: Habit,
        completedDates: Set<LocalDate>,
        today: LocalDate
    ): Int {
        var current = habit.createdAt
        var currentRun = 0
        var bestRun = 0
        while (!current.isAfter(today)) {
            if (!habit.weekDays.isScheduledFor(current.dayOfWeek)) {
                current = current.plusDays(1)
                continue
            }
            if (current in completedDates) {
                currentRun++
                if (currentRun > bestRun) bestRun = currentRun
            } else {
                currentRun = 0
            }
            current = current.plusDays(1)
        }
        return bestRun
    }
}
