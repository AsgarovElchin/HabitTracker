package com.plcoding.habittracker.feature.habits.data

import com.plcoding.habittracker.core.domain.DataError
import com.plcoding.habittracker.core.domain.EmptyResult
import com.plcoding.habittracker.core.domain.Result
import com.plcoding.habittracker.feature.habits.domain.CompletionRecord
import com.plcoding.habittracker.feature.habits.domain.Habit
import com.plcoding.habittracker.feature.habits.domain.HabitLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate

class RoomHabitDataSource(
    private val dao: HabitDao
) : HabitLocalDataSource {

    override fun getHabitsForDayOfWeek(dayOfWeek: DayOfWeek): Flow<List<Habit>> {
        val entityFlow = when (dayOfWeek) {
            DayOfWeek.MONDAY -> dao.getHabitsForMonday()
            DayOfWeek.TUESDAY -> dao.getHabitsForTuesday()
            DayOfWeek.WEDNESDAY -> dao.getHabitsForWednesday()
            DayOfWeek.THURSDAY -> dao.getHabitsForThursday()
            DayOfWeek.FRIDAY -> dao.getHabitsForFriday()
            DayOfWeek.SATURDAY -> dao.getHabitsForSaturday()
            DayOfWeek.SUNDAY -> dao.getHabitsForSunday()
        }
        return entityFlow.map { entities -> entities.map { it.toHabit() } }
    }

    override fun getCompletedHabitIdsForDate(date: LocalDate): Flow<Set<Long>> {
        return dao.getCompletedHabitIdsForDate(date.toEpochDay()).map { it.toSet() }
    }

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate) {
        val epochDay = date.toEpochDay()
        if (dao.isCompleted(habitId, epochDay)) {
            dao.deleteCompletion(habitId, epochDay)
        } else {
            dao.insertCompletion(HabitCompletionEntity(habitId = habitId, date = epochDay))
        }
    }

    override suspend fun upsertHabit(habit: Habit): EmptyResult<DataError.Local> {
        return try {
            dao.upsertHabit(habit.toHabitEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun deleteHabit(habitId: Long) {
        dao.deleteHabit(habitId)
    }

    override suspend fun getHabitById(habitId: Long): Habit? {
        return dao.getHabitById(habitId)?.toHabit()
    }

    override fun getActiveHabitCount(): Flow<Int> {
        return dao.getActiveHabitCount()
    }

    override suspend fun getAllHabits(): List<Habit> {
        return dao.getAllHabits().map { it.toHabit() }
    }

    override suspend fun getCompletionsInRange(start: LocalDate, end: LocalDate): List<CompletionRecord> {
        return dao.getCompletionsInRange(
            startDay = start.toEpochDay(),
            endDay = end.toEpochDay()
        ).map { it.toCompletionRecord() }
    }
}
