package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.WaterColor
import com.example.ui.viewmodel.NutriHabitViewModel

@Composable
fun GoalsScreen(
    viewModel: NutriHabitViewModel,
    modifier: Modifier = Modifier
) {
    val currentTarget by viewModel.nutritionTarget.collectAsStateWithLifecycle()

    var caloriesStr by remember(currentTarget) { mutableStateOf(currentTarget.targetCalories.toString()) }
    var proteinStr by remember(currentTarget) { mutableStateOf(currentTarget.targetProtein.toInt().toString()) }
    var carbsStr by remember(currentTarget) { mutableStateOf(currentTarget.targetCarbs.toInt().toString()) }
    var fatStr by remember(currentTarget) { mutableStateOf(currentTarget.targetFat.toInt().toString()) }
    var waterStr by remember(currentTarget) { mutableStateOf(currentTarget.targetWaterMl.toString()) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    // Calculator states
    var gender by remember { mutableStateOf("Male") }
    var weightKgStr by remember { mutableStateOf("75") }
    var heightCmStr by remember { mutableStateOf("178") }
    var ageStr by remember { mutableStateOf("28") }
    var activityLevel by remember { mutableStateOf("Moderate (3-5 workouts/wk)") }
    var goalType by remember { mutableStateOf("Maintain Weight") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("goals_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Direct Target Editor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Calorie & Macro Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = {
                            caloriesStr = it
                            saveSuccessMessage = false
                        },
                        label = { Text("Daily Calorie Target (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_calories_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = proteinStr,
                            onValueChange = {
                                proteinStr = it
                                saveSuccessMessage = false
                            },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = carbsStr,
                            onValueChange = {
                                carbsStr = it
                                saveSuccessMessage = false
                            },
                            label = { Text("Carbs (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fatStr,
                            onValueChange = {
                                fatStr = it
                                saveSuccessMessage = false
                            },
                            label = { Text("Fat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = waterStr,
                        onValueChange = {
                            waterStr = it
                            saveSuccessMessage = false
                        },
                        label = { Text("Target Water (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val cals = caloriesStr.toIntOrNull() ?: 2200
                            val p = proteinStr.toFloatOrNull() ?: 150f
                            val c = carbsStr.toFloatOrNull() ?: 230f
                            val f = fatStr.toFloatOrNull() ?: 65f
                            val w = waterStr.toIntOrNull() ?: 2500
                            viewModel.updateNutritionTarget(cals, p, c, f, w)
                            saveSuccessMessage = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_targets_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Nutrition Targets", fontWeight = FontWeight.Bold)
                    }

                    if (saveSuccessMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Daily targets updated successfully!",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // TDEE / Macro Calculator Tool Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Macro & TDEE Calculator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Calculate personalized calories & macros based on your body",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gender chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightKgStr,
                            onValueChange = { weightKgStr = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = heightCmStr,
                            onValueChange = { heightCmStr = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Activity Level", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Sedentary", "Moderate", "Very Active").forEach { lvl ->
                            FilterChip(
                                selected = activityLevel.startsWith(lvl),
                                onClick = { activityLevel = lvl },
                                label = { Text(lvl, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Goal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Lose Fat (-20%)", "Maintain Weight", "Build Muscle (+10%)").forEach { g ->
                            FilterChip(
                                selected = goalType == g,
                                onClick = { goalType = g },
                                label = { Text(g, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation
                    val weight = weightKgStr.toFloatOrNull() ?: 75f
                    val height = heightCmStr.toFloatOrNull() ?: 178f
                    val age = ageStr.toFloatOrNull() ?: 28f

                    // Mifflin-St Jeor formula
                    val bmr = if (gender == "Male") {
                        10 * weight + 6.25f * height - 5 * age + 5
                    } else {
                        10 * weight + 6.25f * height - 5 * age - 161
                    }

                    val activityMultiplier = when {
                        activityLevel.startsWith("Sedentary") -> 1.2f
                        activityLevel.startsWith("Very Active") -> 1.725f
                        else -> 1.55f
                    }

                    val tdee = bmr * activityMultiplier
                    val targetCalsCalc = when {
                        goalType.startsWith("Lose Fat") -> (tdee * 0.8f).toInt()
                        goalType.startsWith("Build Muscle") -> (tdee * 1.1f).toInt()
                        else -> tdee.toInt()
                    }

                    // Recommended macros: ~2.0g protein/kg, ~0.8g fat/kg, remainder carbs
                    val recProtein = (weight * 2.0f).toInt()
                    val recFat = (weight * 0.9f).toInt()
                    val recCarbs = ((targetCalsCalc - (recProtein * 4 + recFat * 9)) / 4).coerceAtLeast(50)

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated TDEE: ${tdee.toInt()} kcal",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "Recommended: $targetCalsCalc kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CalorieColor
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Split: ${recProtein}g Protein • ${recCarbs}g Carbs • ${recFat}g Fat",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    caloriesStr = targetCalsCalc.toString()
                                    proteinStr = recProtein.toString()
                                    carbsStr = recCarbs.toString()
                                    fatStr = recFat.toString()
                                    waterStr = (weight * 35).toInt().toString()
                                    viewModel.updateNutritionTarget(targetCalsCalc, recProtein.toFloat(), recCarbs.toFloat(), recFat.toFloat(), (weight * 35).toInt())
                                    saveSuccessMessage = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply Calculated Goals")
                            }
                        }
                    }
                }
            }
        }
    }
}
