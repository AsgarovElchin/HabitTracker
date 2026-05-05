package com.plcoding.habittracker.feature.habits.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.habittracker.feature.habits.domain.Habit
import com.plcoding.habittracker.feature.habits.domain.HabitLocalDataSource
import com.plcoding.habittracker.feature.habits.domain.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class StatisticsViewModel(
    private val dataSource: HabitLocalDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val allHabits = dataSource.getAllHabits()

            if (allHabits.isEmpty()) return@launch

            val earliestCreation = allHabits.minOf { it.createdAt }
            val completions = dataSource.getCompletionsInRange(earliestCreation, today)
            val completionsByHabit = completions.groupBy { it.habitId }
                .mapValues { (_, records) -> records.map { it.date }.toSet() }

            val habitStreaks = allHabits.map { habit ->
                val dates = completionsByHabit[habit.id] ?: emptySet()
                HabitStreakUi(
                    habitId = habit.id,
                    name = habit.name,
                    icon = habit.icon,
                    currentStreak = StreakCalculator.computeCurrentStreak(habit, dates, today),
                    bestStreak = StreakCalculator.computeBestStreak(habit, dates, today),
                )
            }

            _state.update {
                StatisticsState(
                    thisWeekPercentage = computeThisWeekPercentage(allHabits, completionsByHabit, today),
                    bestStreak = habitStreaks.maxOfOrNull { it.bestStreak } ?: 0,
                    activeHabitCount = allHabits.size,
                    heatmapData = computeHeatmap(allHabits, completionsByHabit, today),
                    habitStreaks = habitStreaks.sortedByDescending { it.bestStreak },
                )
            }
        }
    }

    private fun computeThisWeekPercentage(
        habits: List<Habit>,
        completionsByHabit: Map<Long, Set<LocalDate>>,
        today: LocalDate,
    ): Int {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        var totalScheduled = 0
        var totalCompleted = 0

        var day = monday
        while (!day.isAfter(today)) {
            for (habit in habits) {
                if (habit.weekDays.isScheduledFor(day.dayOfWeek) && !day.isBefore(habit.createdAt)) {
                    totalScheduled++
                    if (day in (completionsByHabit[habit.id] ?: emptySet())) totalCompleted++
                }
            }
            day = day.plusDays(1)
        }

        return if (totalScheduled > 0) (totalCompleted * 100 / totalScheduled) else 0
    }

    private fun computeHeatmap(
        habits: List<Habit>,
        completionsByHabit: Map<Long, Set<LocalDate>>,
        today: LocalDate,
    ): List<HeatmapDay> {
        val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val start = sunday.minusDays(27)

        return (0 until 28).map { offset ->
            val day = start.plusDays(offset.toLong())
            var scheduled = 0
            var completed = 0

            for (habit in habits) {
                if (habit.weekDays.isScheduledFor(day.dayOfWeek) && !day.isBefore(habit.createdAt)) {
                    scheduled++
                    if (day in (completionsByHabit[habit.id] ?: emptySet())) completed++
                }
            }

            HeatmapDay(
                date = day,
                completionRatio = if (scheduled > 0) completed.toFloat() / scheduled else 0f,
                isToday = day == today,
                isFuture = day.isAfter(today),
            )
        }
    }
}
