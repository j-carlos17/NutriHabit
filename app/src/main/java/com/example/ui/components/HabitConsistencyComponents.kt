package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.ui.theme.StreakColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekDayInfo(
    val dayShort: String, // "MON", "TUE", etc.
    val dateStr: String,  // "YYYY-MM-DD"
    val dayNumber: String // "14", "15", etc.
)

fun getCurrentWeekDays(referenceDateStr: String): List<WeekDayInfo> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = try { sdf.parse(referenceDateStr) ?: Date() } catch (e: Exception) { Date() }
    val cal = Calendar.getInstance()
    cal.time = date

    // Move to Monday of current week
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val list = mutableListOf<WeekDayInfo>()
    val shortSdf = SimpleDateFormat("EEE", Locale.getDefault())
    val numSdf = SimpleDateFormat("d", Locale.getDefault())

    for (i in 0 until 7) {
        val dStr = sdf.format(cal.time)
        val shortName = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> "MON"
        }
        val dayNum = numSdf.format(cal.time)
        list.add(WeekDayInfo(shortName, dStr, dayNum))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
}

@Composable
fun HabitConsistencySummaryCard(
    habits: List<HabitEntity>,
    weekLogs: List<HabitLogEntity>,
    weekDays: List<WeekDayInfo>,
    todayStr: String,
    modifier: Modifier = Modifier
) {
    val completedSet = weekLogs.filter { it.completed }.map { "${it.habitId}_${it.date}" }.toSet()

    var totalScheduledSlots = 0
    var totalCompletedSlots = 0

    // Day-by-day counts
    val dayCompletionCounts = weekDays.map { dayInfo ->
        var scheduledForDay = 0
        var completedForDay = 0
        habits.forEach { habit ->
            val repeatDays = habit.repeatDays.split(",").map { it.trim().uppercase() }
            if (repeatDays.contains(dayInfo.dayShort)) {
                scheduledForDay++
                totalScheduledSlots++
                if (completedSet.contains("${habit.id}_${dayInfo.dateStr}")) {
                    completedForDay++
                    totalCompletedSlots++
                }
            }
        }
        Pair(dayInfo, if (scheduledForDay > 0) completedForDay.toFloat() / scheduledForDay.toFloat() else 0f)
    }

    val overallScorePct = if (totalScheduledSlots > 0) {
        ((totalCompletedSlots.toFloat() / totalScheduledSlots.toFloat()) * 100).toInt()
    } else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_consistency_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Weekly Habit Consistency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$totalCompletedSlots of $totalScheduledSlots habit check-ins completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$overallScorePct%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7-day mini bar graph
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dayCompletionCounts.forEach { (dayInfo, ratio) ->
                    val isToday = dayInfo.dateStr == todayStr

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((ratio * 56).coerceAtLeast(0f).dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (ratio >= 0.8f) MaterialTheme.colorScheme.primary else if (ratio > 0f) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = dayInfo.dayShort.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dayInfo.dayNumber,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitWeeklyMatrixItem(
    habit: HabitEntity,
    weekDays: List<WeekDayInfo>,
    weekLogs: List<HabitLogEntity>,
    todayStr: String,
    modifier: Modifier = Modifier
) {
    val habitColor = parseHexColor(habit.colorHex)
    val repeatDays = habit.repeatDays.split(",").map { it.trim().uppercase() }.toSet()
    val completedDates = weekLogs
        .filter { it.habitId == habit.id && it.completed }
        .map { it.date }
        .toSet()

    val scheduledDaysInWeek = weekDays.count { repeatDays.contains(it.dayShort) }
    val completedDaysInWeek = weekDays.count { repeatDays.contains(it.dayShort) && completedDates.contains(it.dateStr) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(habitColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getHabitIcon(habit.icon),
                            contentDescription = null,
                            tint = habitColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = habitColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$completedDaysInWeek / $scheduledDaysInWeek this week",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = habitColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7 Days Circles Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { dayInfo ->
                    val isScheduled = repeatDays.contains(dayInfo.dayShort)
                    val isCompleted = completedDates.contains(dayInfo.dateStr)
                    val isToday = dayInfo.dateStr == todayStr

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> habitColor
                                        isScheduled && isToday -> habitColor.copy(alpha = 0.15f)
                                        isScheduled -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isToday) 2.dp else if (isScheduled) 1.dp else 0.dp,
                                    color = if (isToday) habitColor else if (isScheduled) habitColor.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (isScheduled) {
                                Text(
                                    text = dayInfo.dayShort.take(1),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) habitColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            } else {
                                Text(
                                    text = "–",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = dayInfo.dayShort.take(1),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isToday) habitColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
