package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onSaveHabit: (name: String, category: String, icon: String, colorHex: String, repeatDays: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    var selectedIcon by remember { mutableStateOf("water") }
    var selectedColorHex by remember { mutableStateOf("#10B981") }

    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    var selectedDays by remember { mutableStateOf(allDays.toSet()) }

    val categories = listOf("Health", "Fitness", "Nutrition", "Mindset", "Productivity")
    val icons = listOf(
        "water" to "Water",
        "gym" to "Workout",
        "run" to "Cardio",
        "apple" to "Nutrition",
        "sleep" to "Sleep",
        "book" to "Reading",
        "mind" to "Mindset",
        "check" to "General"
    )
    val colors = listOf(
        "#10B981", "#3B82F6", "#F59E0B", "#EC4899", "#8B5CF6", "#06B6D4", "#F97316"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("add_habit_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Weekly Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Habit Name
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Habit Title *") },
                            placeholder = { Text("e.g. Drink 2.5L Water, Walk 10k Steps") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("habit_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Weekly Repetition Picker
                    item {
                        Column {
                            Text(
                                text = "Repeats Weekly On *",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick preset chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = selectedDays.size == 7,
                                    onClick = { selectedDays = allDays.toSet() },
                                    label = { Text("Everyday") }
                                )
                                FilterChip(
                                    selected = selectedDays == setOf("MON", "TUE", "WED", "THU", "FRI"),
                                    onClick = {
                                        selectedDays = setOf("MON", "TUE", "WED", "THU", "FRI")
                                    },
                                    label = { Text("Weekdays") }
                                )
                                FilterChip(
                                    selected = selectedDays == setOf("SAT", "SUN"),
                                    onClick = { selectedDays = setOf("SAT", "SUN") },
                                    label = { Text("Weekends") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day selector circles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                allDays.forEach { day ->
                                    val isSelected = selectedDays.contains(day)
                                    val activeColor = parseHexColor(selectedColorHex)

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                selectedDays = if (isSelected) {
                                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                                } else {
                                                    selectedDays + day
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.take(2),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category
                    item {
                        Column {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat) }
                                    )
                                }
                            }
                        }
                    }

                    // Icon Picker
                    item {
                        Column {
                            Text(
                                text = "Icon",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                icons.forEach { (iconKey, label) ->
                                    val isSelected = selectedIcon == iconKey
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { selectedIcon = iconKey },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getHabitIcon(iconKey),
                                            contentDescription = label,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Color Picker
                    item {
                        Column {
                            Text(
                                text = "Color Accent",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                colors.forEach { hex ->
                                    val col = parseHexColor(hex)
                                    val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColorHex = hex }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                            val daysStr = allDays.filter { selectedDays.contains(it) }.joinToString(",")
                            onSaveHabit(name, selectedCategory, selectedIcon, selectedColorHex, daysStr)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank() && selectedDays.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_habit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Habit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
