package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HabitEntity
import com.example.ui.theme.StreakColor

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF10B981)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0x00000000FF000000)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        defaultColor
    }
}

fun getHabitIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "water" -> Icons.Default.LocalDrink
        "run", "gym", "fitness" -> Icons.Default.FitnessCenter
        "apple", "nutrition" -> Icons.Default.Restaurant
        "sleep" -> Icons.Default.NightlightRound
        "book" -> Icons.Default.MenuBook
        "mind" -> Icons.Default.SelfImprovement
        else -> Icons.Default.TaskAlt
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    currentDayOfWeek: String, // e.g. "MON"
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = parseHexColor(habit.colorHex)
    val checkAnimScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.0f else 0.85f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "checkScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        label = "bgColor"
    )

    val weekDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val repeatDaySet = habit.repeatDays.split(",").map { it.trim().uppercase() }.toSet()
    val isScheduledForCurrentDay = repeatDaySet.contains(currentDayOfWeek.uppercase())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp),
        border = if (isCompleted) null else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark Toggle Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) habitColor else habitColor.copy(alpha = 0.15f)
                    )
                    .clickable { onToggle() }
                    .testTag("toggle_habit_${habit.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(checkAnimScale)
                    )
                } else {
                    Icon(
                        imageVector = getHabitIcon(habit.icon),
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Habit Details & Weekly Repetition Badges
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = habitColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = habitColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isScheduledForCurrentDay) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Scheduled Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Weekly Repeating Days Pills (Mon - Sun)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEach { day ->
                        val isRepeatDay = repeatDaySet.contains(day)
                        val isToday = day == currentDayOfWeek.uppercase()

                        val dayColor = when {
                            isRepeatDay && isToday -> habitColor
                            isRepeatDay -> habitColor.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        }

                        val textColor = when {
                            isRepeatDay && isToday -> Color.White
                            isRepeatDay -> habitColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isRepeatDay && isToday) habitColor else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = dayColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.take(1),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = if (isRepeatDay) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Habit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
