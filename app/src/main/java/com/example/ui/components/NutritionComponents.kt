package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FoodEntryEntity
import com.example.data.local.entity.NutritionTargetEntity
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.WaterColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@Composable
fun DateSelectorBar(
    currentDateStr: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDate = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(currentDateStr) ?: Date()
        val todayStr = sdf.format(Date())
        if (currentDateStr == todayStr) {
            "Today, " + SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        } else {
            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        currentDateStr
    }

    val isToday = try {
        currentDateStr == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    } catch (e: Exception) {
        true
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_selector_bar"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.testTag("prev_day_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day"
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToday() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isToday) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Reset",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextDay,
                modifier = Modifier.testTag("next_day_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day"
                )
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(
    entries: List<FoodEntryEntity>,
    target: NutritionTargetEntity,
    waterAmountMl: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.map { it.protein }.sum()
    val totalCarbs = entries.map { it.carbs }.sum()
    val totalFat = entries.map { it.fat }.sum()

    val caloriesRemaining = (target.targetCalories - totalCalories).coerceAtLeast(0)
    val calProgress = if (target.targetCalories > 0) {
        min(1f, totalCalories.toFloat() / target.targetCalories.toFloat())
    } else 0f

    val animatedCalProgress by animateFloatAsState(
        targetValue = calProgress,
        animationSpec = tween(durationMillis = 600),
        label = "calProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nutrition_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Calorie Visual
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = CalorieColor,
                            startAngle = -90f,
                            sweepAngle = animatedCalProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$caloriesRemaining",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "kcal left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Numerical breakdown
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Daily Calorie Budget",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$totalCalories",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = CalorieColor
                        )
                        Text(
                            text = "/ ${target.targetCalories} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    val pct = (calProgress * 100).toInt()
                    Text(
                        text = "$pct% consumed",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (totalCalories > target.targetCalories) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macro Progress Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroPill(
                    label = "Protein",
                    current = totalProtein,
                    target = target.targetProtein,
                    unit = "g",
                    color = ProteinColor,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Carbs",
                    current = totalCarbs,
                    target = target.targetCarbs,
                    unit = "g",
                    color = CarbsColor,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Fat",
                    current = totalFat,
                    target = target.targetFat,
                    unit = "g",
                    color = FatColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Water Tracker Bar
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = WaterColor.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Water Intake",
                            tint = WaterColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Water",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$waterAmountMl / ${target.targetWaterMl} ml",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { onAddWater(250) },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WaterColor.copy(alpha = 0.25f)
                            )
                        ) {
                            Text("+250ml", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { onAddWater(500) },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WaterColor.copy(alpha = 0.25f)
                            )
                        ) {
                            Text("+500ml", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroPill(
    label: String,
    current: Float,
    target: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) min(1f, current / target) else 0f

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${current.toInt()} / ${target.toInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MealSectionCard(
    mealType: String,
    title: String,
    icon: @Composable () -> Unit,
    entries: List<FoodEntryEntity>,
    onAddFood: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val mealCalories = entries.sumOf { it.calories }
    val mealProtein = entries.map { it.protein }.sum().toInt()
    val mealCarbs = entries.map { it.carbs }.sum().toInt()
    val mealFat = entries.map { it.fat }.sum().toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meal_section_${mealType.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (entries.isNotEmpty()) {
                            Text(
                                text = "P: ${mealProtein}g • C: ${mealCarbs}g • F: ${mealFat}g",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$mealCalories kcal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CalorieColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onAddFood,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("add_food_${mealType.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Food to $title",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (entries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entries.forEach { entry ->
                        FoodEntryRow(
                            entry = entry,
                            onDelete = { onDeleteEntry(entry.id) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No food logged yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 46.dp)
                )
            }
        }
    }
}

@Composable
fun FoodEntryRow(
    entry: FoodEntryEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${entry.servingSize} • P: ${entry.protein.toInt()}g  C: ${entry.carbs.toInt()}g  F: ${entry.fat.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${entry.calories} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
