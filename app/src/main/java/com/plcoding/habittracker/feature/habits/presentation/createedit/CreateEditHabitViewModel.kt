package com.plcoding.habittracker.feature.habits.presentation.createedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.habittracker.core.domain.onSuccess
import com.plcoding.habittracker.feature.habits.domain.Habit
import com.plcoding.habittracker.feature.habits.domain.HabitLocalDataSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreateEditHabitViewModel(
    private val dataSource: HabitLocalDataSource,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val habitId: Long? = savedStateHandle.get<Long>("habitId")

    private val _state = MutableStateFlow(CreateEditHabitState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateEditHabitEvent>()
    val events = _events.receiveAsFlow()

    init {
        if (habitId != null) {
            viewModelScope.launch {
                val habit = dataSource.getHabitById(habitId) ?: return@launch
                _state.update {
                    it.copy(
                        isEditMode = true,
                        habitName = habit.name,
                        selectedIcon = habit.icon,
                        weekDays = habit.weekDays,
                        canSave = computeCanSave(habit.name, habit.weekDays),
                    )
                }
            }
        }
    }

    fun onAction(action: CreateEditHabitAction) {
        when (action) {
            is CreateEditHabitAction.OnNameChange -> {
                val name = action.name
                _state.update {
                    it.copy(
                        habitName = name,
                        canSave = computeCanSave(name, it.weekDays),
                    )
                }
            }
            is CreateEditHabitAction.OnIconSelect -> {
                _state.update {
                    it.copy(
                        selectedIcon = action.icon,
                        isIconPickerExpanded = false,
                    )
                }
            }
            is CreateEditHabitAction.OnToggleDay -> {
                _state.update {
                    val newWeekDays = it.weekDays.toggle(action.dayOfWeek)
                    it.copy(
                        weekDays = newWeekDays,
                        canSave = computeCanSave(it.habitName, newWeekDays),
                    )
                }
            }
            CreateEditHabitAction.OnToggleIconPicker -> {
                _state.update { it.copy(isIconPickerExpanded = !it.isIconPickerExpanded) }
            }
            CreateEditHabitAction.OnSaveClick -> saveHabit()
            CreateEditHabitAction.OnDeleteClick -> {
                _state.update { it.copy(isDeleteDialogVisible = true) }
            }
            CreateEditHabitAction.OnConfirmDelete -> deleteHabit()
            CreateEditHabitAction.OnDismissDeleteDialog -> {
                _state.update { it.copy(isDeleteDialogVisible = false) }
            }
        }
    }

    private fun saveHabit() {
        val currentState = _state.value
        if (!currentState.canSave || currentState.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val habit = Habit(
                id = habitId ?: 0L,
                name = currentState.habitName.trim(),
                icon = currentState.selectedIcon,
                weekDays = currentState.weekDays,
                createdAt = if (habitId != null) {
                    dataSource.getHabitById(habitId)?.createdAt ?: LocalDate.now()
                } else {
                    LocalDate.now()
                },
            )
            dataSource.upsertHabit(habit).onSuccess {
                _events.send(CreateEditHabitEvent.HabitSaved)
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun deleteHabit() {
        val id = habitId ?: return
        viewModelScope.launch {
            dataSource.deleteHabit(id)
            _events.send(CreateEditHabitEvent.HabitDeleted)
        }
    }

    private fun computeCanSave(name: String, weekDays: com.plcoding.habittracker.feature.habits.domain.WeekDays): Boolean {
        val trimmed = name.trim()
        return trimmed.isNotEmpty() && trimmed.length <= 200 && weekDays.hasAtLeastOneDay()
    }
}
