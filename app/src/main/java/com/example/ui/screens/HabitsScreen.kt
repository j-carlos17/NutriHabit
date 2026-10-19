package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitConsistencySummaryCard
import com.example.ui.components.HabitWeeklyMatrixItem
import com.example.ui.components.getCurrentWeekDays
import com.example.ui.viewmodel.NutriHabitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HabitsScreen(
    viewModel: NutriHabitViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val logs by viewModel.currentDayHabitLogs.collectAsStateWithLifecycle()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var viewModeTab by remember { mutableIntStateOf(0) } // 0: Daily Checklist, 1: Weekly Consistency Matrix
    var filterTab by remember { mutableIntStateOf(0) } // 0: Scheduled for this day, 1: All Habits

    val currentDayOfWeek = NutriHabitViewModel.getDayOfWeekShort(selectedDate)
    val completedHabitIds = logs.filter { it.completed }.map { it.habitId }.toSet()

    val weekDays = remember(selectedDate) { getCurrentWeekDays(selectedDate) }
    val todayActualStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Filter habits based on tab
    val displayedHabits = if (filterTab == 0) {
        habits.filter { habit ->
            val repeatDays = habit.repeatDays.split(",").map { it.trim().uppercase() }
            repeatDays.contains(currentDayOfWeek.uppercase())
        }
    } else {
        habits
    }

    val scheduledCount = habits.count { habit ->
        val repeatDays = habit.repeatDays.split(",").map { it.trim().uppercase() }
        repeatDays.contains(currentDayOfWeek.uppercase())
    }
    val completedCount = habits.count { habit ->
        val repeatDays = habit.repeatDays.split(",").map { it.trim().uppercase() }
        repeatDays.contains(currentDayOfWeek.uppercase()) && completedHabitIds.contains(habit.id)
    }

    val completionRate = if (scheduledCount > 0) {
        completedCount.toFloat() / scheduledCount.toFloat()
    } else 0f

    val animatedRate by animateFloatAsState(targetValue = completionRate, label = "rate")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true },
                modifier = Modifier.testTag("fab_add_habit"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("habits_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Selector Bar
            item {
                DateSelectorBar(
                    currentDateStr = selectedDate,
                    onPreviousDay = { viewModel.stepDay(-1) },
                    onNextDay = { viewModel.stepDay(1) },
                    onToday = { viewModel.setDateToToday() }
                )
            }

            // View Mode Tab: Daily Checklist vs Weekly Consistency Overview
            item {
                TabRow(selectedTabIndex = viewModeTab) {
                    Tab(
                        selected = viewModeTab == 0,
                        onClick = { viewModeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ListAlt, contentDescription = null)
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text("Daily Checklist")
                            }
                        }
                    )
                    Tab(
                        selected = viewModeTab == 1,
                        onClick = { viewModeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.GridView, contentDescription = null)
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text("Consistency Matrix")
                            }
                        }
                    )
                }
            }

            if (viewModeTab == 0) {
                // DAILY CHECKLIST VIEW
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Habit Target",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$completedCount of $scheduledCount completed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${(completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { animatedRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterTab == 0,
                            onClick = { filterTab = 0 },
                            label = { Text("Scheduled for $currentDayOfWeek ($scheduledCount)") }
                        )
                        FilterChip(
                            selected = filterTab == 1,
                            onClick = { filterTab = 1 },
                            label = { Text("All Habits (${habits.size})") }
                        )
                    }
                }

                // Habits List
                if (displayedHabits.isNotEmpty()) {
                    items(displayedHabits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            isCompleted = completedHabitIds.contains(habit.id),
                            currentDayOfWeek = currentDayOfWeek,
                            onToggle = {
                                viewModel.toggleHabit(habit.id, completedHabitIds.contains(habit.id))
                            },
                            onDelete = {
                                viewModel.deleteHabit(habit)
                            }
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = if (filterTab == 0) "No habits scheduled for $currentDayOfWeek" else "No habits created yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap the + button below to add your weekly habits!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // VISUAL CONSISTENCY MATRIX VIEW
                item {
                    HabitConsistencySummaryCard(
                        habits = habits,
                        weekLogs = logs,
                        weekDays = weekDays,
                        todayStr = selectedDate
                    )
                }

                item {
                    Text(
                        text = "Weekly Repeating Habit Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(habits, key = { "matrix_${it.id}" }) { habit ->
                    HabitWeeklyMatrixItem(
                        habit = habit,
                        weekDays = weekDays,
                        weekLogs = logs,
                        todayStr = selectedDate
                    )
                }
            }
        }
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onSaveHabit = { name, category, icon, colorHex, repeatDays ->
                viewModel.addHabit(name, category, icon, colorHex, repeatDays)
            }
        )
    }
}
