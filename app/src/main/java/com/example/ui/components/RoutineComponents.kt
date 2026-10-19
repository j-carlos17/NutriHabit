package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.repository.RoutineWithExercises
import com.example.ui.theme.StreakColor
import kotlinx.coroutines.delay

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onStartWorkout: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routineColor = parseHexColor(routine.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            .size(40.dp)
                            .background(routineColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = routineColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = routineColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = routine.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = routineColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "⏱ ~${routine.estimatedMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete routine",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (routine.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (routine.targetDays.isNotBlank()) {
                    Text(
                        text = "Days: ${routine.targetDays}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = onStartWorkout,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = routineColor),
                    modifier = Modifier.testTag("start_routine_${routine.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Workout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ActiveRoutineRunnerDialog(
    routineWithExercises: RoutineWithExercises,
    onDismiss: () -> Unit,
    onCompleteRoutine: (durationMins: Int, caloriesBurned: Int) -> Unit
) {
    val routine = routineWithExercises.routine
    val exercises = routineWithExercises.exercises

    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentSetIndex by remember { mutableIntStateOf(1) }
    var totalElapsedSeconds by remember { mutableIntStateOf(0) }
    var isResting by remember { mutableStateOf(false) }
    var restSecondsLeft by remember { mutableIntStateOf(0) }
    var showCelebration by remember { mutableStateOf(false) }

    // Overall timer ticker
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            totalElapsedSeconds++
        }
    }

    // Rest countdown ticker
    LaunchedEffect(isResting, restSecondsLeft) {
        if (isResting && restSecondsLeft > 0) {
            delay(1000L)
            restSecondsLeft--
            if (restSecondsLeft <= 0) {
                isResting = false
            }
        }
    }

    val currentEx = exercises.getOrNull(currentExerciseIndex)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("active_routine_runner"),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showCelebration) {
                WorkoutCelebrationView(
                    routineName = routine.name,
                    durationSeconds = totalElapsedSeconds,
                    onFinish = { cals ->
                        val durationMins = (totalElapsedSeconds / 60).coerceAtLeast(1)
                        onCompleteRoutine(durationMins, cals)
                    }
                )
            } else if (currentEx != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val mins = totalElapsedSeconds / 60
                            val secs = totalElapsedSeconds % 60
                            Text(
                                text = "Elapsed: %02d:%02d".format(mins, secs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Routine")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress through exercises
                    val progress = (currentExerciseIndex.toFloat() / exercises.size.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Exercise ${currentExerciseIndex + 1} of ${exercises.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Exercise Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Set $currentSetIndex of ${currentEx.sets}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentEx.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Target: ${currentEx.repsOrDuration}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Rest Timer Card if active
                            AnimatedVisibility(visible = isResting) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(top = 20.dp)
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = StreakColor.copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Timer,
                                                    contentDescription = null,
                                                    tint = StreakColor
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Rest Timer: ${restSecondsLeft}s",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StreakColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                FilledTonalButton(onClick = { restSecondsLeft += 30 }) {
                                                    Text("+30s")
                                                }
                                                FilledTonalButton(onClick = { isResting = false }) {
                                                    Text("Skip Rest")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentSetIndex < currentEx.sets) {
                                    currentSetIndex++
                                    // Trigger rest countdown
                                    if (currentEx.restSeconds > 0) {
                                        isResting = true
                                        restSecondsLeft = currentEx.restSeconds
                                    }
                                } else {
                                    // Move to next exercise
                                    if (currentExerciseIndex < exercises.size - 1) {
                                        currentExerciseIndex++
                                        currentSetIndex = 1
                                        if (currentEx.restSeconds > 0) {
                                            isResting = true
                                            restSecondsLeft = currentEx.restSeconds
                                        }
                                    } else {
                                        // Complete Routine!
                                        showCelebration = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("complete_set_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentSetIndex < currentEx.sets) "Complete Set $currentSetIndex" else if (currentExerciseIndex < exercises.size - 1) "Next Exercise" else "Finish Workout!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                // Empty routine fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No exercises in this routine.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onDismiss) { Text("Back") }
                }
            }
        }
    }
}

@Composable
fun WorkoutCelebrationView(
    routineName: String,
    durationSeconds: Int,
    onFinish: (caloriesBurned: Int) -> Unit
) {
    val durationMins = (durationSeconds / 60).coerceAtLeast(1)
    val estimatedCalories = durationMins * 7 // ~7 kcal/min moderate intensity workout

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Workout Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = routineName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$durationMins min",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "~$estimatedCalories kcal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StreakColor
                    )
                    Text(
                        text = "Calories Burned",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { onFinish(estimatedCalories) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp)
                .testTag("save_workout_completion_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Log Workout & Done", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onSaveRoutine: (
        name: String,
        description: String,
        category: String,
        estimatedMinutes: Int,
        targetDays: String,
        colorHex: String,
        exercises: List<RoutineExerciseEntity>
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Strength") }
    var durationMinutesStr by remember { mutableStateOf("30") }
    var colorHex by remember { mutableStateOf("#10B981") }

    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    var selectedDays by remember { mutableStateOf(setOf("MON", "WED", "FRI")) }

    // List of exercises in this routine
    val exercises = remember {
        mutableStateListOf(
            RoutineExerciseEntity(routineId = 0, name = "Squats", sets = 3, repsOrDuration = "10 reps", restSeconds = 60, orderIndex = 0),
            RoutineExerciseEntity(routineId = 0, name = "Push-ups", sets = 3, repsOrDuration = "12 reps", restSeconds = 60, orderIndex = 1)
        )
    }

    var newExName by remember { mutableStateOf("") }
    var newExSets by remember { mutableStateOf("3") }
    var newExReps by remember { mutableStateOf("10 reps") }
    var newExRest by remember { mutableStateOf("60") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("add_routine_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Routine",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Routine Name *") },
                            placeholder = { Text("e.g. Upper Body Hypertrophy") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("routine_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = durationMinutesStr,
                                onValueChange = { durationMinutesStr = it },
                                label = { Text("Duration (mins)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Category", style = MaterialTheme.typography.labelSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Strength", "Cardio", "Core").forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategory == cat,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Target Days
                    item {
                        Column {
                            Text(
                                text = "Target Days",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                allDays.forEach { day ->
                                    val isSelected = selectedDays.contains(day)
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.take(2),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Exercises Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exercises (${exercises.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Current Exercises List
                    itemsIndexed(exercises) { index, ex ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${index + 1}. ${ex.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${ex.sets} sets × ${ex.repsOrDuration} • ${ex.restSeconds}s rest",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { exercises.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove exercise",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Add Exercise Box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "+ Add Exercise",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = newExName,
                                    onValueChange = { newExName = it },
                                    placeholder = { Text("Exercise name (e.g. Bench Press)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = newExSets,
                                        onValueChange = { newExSets = it },
                                        label = { Text("Sets") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = newExReps,
                                        onValueChange = { newExReps = it },
                                        label = { Text("Reps / Duration") },
                                        modifier = Modifier.weight(1.5f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = newExRest,
                                        onValueChange = { newExRest = it },
                                        label = { Text("Rest (s)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (newExName.isNotBlank()) {
                                            val setsInt = newExSets.toIntOrNull() ?: 3
                                            val restInt = newExRest.toIntOrNull() ?: 60
                                            exercises.add(
                                                RoutineExerciseEntity(
                                                    routineId = 0,
                                                    name = newExName,
                                                    sets = setsInt,
                                                    repsOrDuration = newExReps.ifBlank { "10 reps" },
                                                    restSeconds = restInt,
                                                    orderIndex = exercises.size
                                                )
                                            )
                                            newExName = ""
                                        }
                                    },
                                    enabled = newExName.isNotBlank(),
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add Exercise")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val duration = durationMinutesStr.toIntOrNull() ?: 30
                        val daysStr = allDays.filter { selectedDays.contains(it) }.joinToString(",")
                        if (name.isNotBlank()) {
                            onSaveRoutine(name, description, selectedCategory, duration, daysStr, colorHex, exercises.toList())
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank() && exercises.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_routine_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Workout Routine", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
