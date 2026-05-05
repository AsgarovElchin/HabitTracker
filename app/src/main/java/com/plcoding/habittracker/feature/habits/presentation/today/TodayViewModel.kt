package com.plcoding.habittracker.feature.habits.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.habittracker.feature.habits.domain.HabitLocalDataSource
import com.plcoding.habittracker.feature.habits.domain.StreakCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val dataSource: HabitLocalDataSource,
) : ViewModel() {

    private val today = LocalDate.now()

    private val _state = MutableStateFlow(TodayState())
    val state = _state.asStateFlow()

    init {
        combine(
            dataSource.getHabitsForDayOfWeek(today.dayOfWeek),
            dataSource.getCompletedHabitIdsForDate(today)
        ) { habits, completedIds ->
            habits to completedIds
        }.mapLatest { (habits, completedIds) ->
            val earliestCreation = habits.minOfOrNull { it.createdAt } ?: return@mapLatest
            val completions = dataSource.getCompletionsInRange(earliestCreation, today)
            val completionsByHabit = completions.groupBy { it.habitId }
                .mapValues { (_, records) -> records.map { it.date }.toSet() }

            _state.update {
                TodayState(
                    habits = habits.map { habit ->
                        val dates = completionsByHabit[habit.id] ?: emptySet()
                        TodayHabitUi(
                            habitId = habit.id,
                            name = habit.name,
                            icon = habit.icon,
                            currentStreak = StreakCalculator.computeCurrentStreak(
                                habit, dates, today
                            ),
                            isCompleted = habit.id in completedIds,
                        )
                    },
                    completedCount = habits.count { it.id in completedIds },
                    totalCount = habits.size,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: TodayAction) {
        when (action) {
            is TodayAction.ToggleCompletion -> {
                viewModelScope.launch {
                    dataSource.toggleCompletion(action.habitId, today)
                }
            }
        }
    }
}
