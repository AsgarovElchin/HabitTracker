package com.plcoding.habittracker.feature.habits.presentation.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plcoding.habittracker.R
import com.plcoding.habittracker.core.presentation.designsystem.HabitIconContainer
import com.plcoding.habittracker.core.presentation.designsystem.HabitTrackerIconButton
import com.plcoding.habittracker.core.presentation.designsystem.HabitTrackerTopBar
import com.plcoding.habittracker.feature.habits.domain.HabitIcon
import com.plcoding.habittracker.ui.theme.HabitTrackerTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreenRoot(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: TodayViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TodayScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToStats = onNavigateToStats,
    )
}

@Composable
private fun TodayScreen(
    state: TodayState,
    onAction: (TodayAction) -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    val now = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
        ) {
            HabitTrackerTopBar(
                title = stringResource(R.string.today),
                subtitle = now.format(dateFormatter),
                trailingContent = {
                    HabitTrackerIconButton(
                        icon = rememberVectorPainter(Icons.Filled.BarChart),
                        contentDescription = stringResource(R.string.statistics),
                        onClick = onNavigateToStats,
                        size = 44.dp,
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProgressSection(
                completedCount = state.completedCount,
                totalCount = state.totalCount,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.habits.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.habits,
                        key = { it.habitId }
                    ) { habit ->
                        HabitCard(
                            habit = habit,
                            onToggleCompletion = {
                                onAction(TodayAction.ToggleCompletion(habit.habitId))
                            },
                            onClick = { onNavigateToEdit(habit.habitId) },
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_habit),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ProgressSection(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.daily_progress),
                style = MaterialTheme.typography.bodyMedium,
                color = HabitTrackerTheme.colors.textTertiary,
            )
            Text(
                text = "$completedCount / $totalCount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val fraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun HabitCard(
    habit: TodayHabitUi,
    onToggleCompletion: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HabitIconContainer(
            icon = habit.icon,
            size = 42.dp,
            iconSize = 22.dp,
            cornerRadius = 12.dp,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (habit.currentStreak > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = HabitTrackerTheme.colors.streak,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.day_streak, habit.currentStreak),
                        style = MaterialTheme.typography.labelSmall,
                        color = HabitTrackerTheme.colors.streak,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.no_streak_yet),
                    style = MaterialTheme.typography.labelSmall,
                    color = HabitTrackerTheme.colors.textTertiary,
                )
            }
        }

        CompletionCheckbox(
            isChecked = habit.isCompleted,
            onClick = onToggleCompletion,
        )
    }
}

@Composable
private fun CompletionCheckbox(
    isChecked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isChecked) HabitTrackerTheme.colors.success else Color.Transparent,
        label = "checkbox_bg"
    )
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .then(
                if (isChecked) {
                    Modifier.background(backgroundColor)
                } else {
                    Modifier.border(2.dp, HabitTrackerTheme.colors.border, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.no_habits_yet),
            style = MaterialTheme.typography.titleMedium,
            color = HabitTrackerTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.tap_plus_to_add),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 17.sp),
            color = HabitTrackerTheme.colors.textTertiary,
        )
    }
}

@Preview
@Composable
private fun TodayScreenPreview() {
    HabitTrackerTheme {
        TodayScreen(
            state = TodayState(
                habits = listOf(
                    TodayHabitUi(1, "Morning Run", HabitIcon.RUN, 5, true),
                    TodayHabitUi(2, "Read", HabitIcon.READ, 0, false),
                    TodayHabitUi(3, "Meditate", HabitIcon.MEDITATE, 12, true),
                ),
                completedCount = 2,
                totalCount = 3,
            ),
            onAction = {},
            onNavigateToEdit = {},
            onNavigateToCreate = {},
            onNavigateToStats = {},
        )
    }
}

@Preview
@Composable
private fun TodayScreenEmptyPreview() {
    HabitTrackerTheme {
        TodayScreen(
            state = TodayState(),
            onAction = {},
            onNavigateToEdit = {},
            onNavigateToCreate = {},
            onNavigateToStats = {},
        )
    }
}
