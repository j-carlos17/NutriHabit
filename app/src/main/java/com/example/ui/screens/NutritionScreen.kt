package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddFoodDialog
import com.example.ui.components.BarcodeScanDialog
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.MealSectionCard
import com.example.ui.components.NutritionSummaryCard
import com.example.ui.viewmodel.NutriHabitViewModel

@Composable
fun NutritionScreen(
    viewModel: NutriHabitViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val target by viewModel.nutritionTarget.collectAsStateWithLifecycle()
    val foodEntries by viewModel.foodEntries.collectAsStateWithLifecycle()
    val waterEntries by viewModel.waterEntries.collectAsStateWithLifecycle()
    val totalWater = waterEntries.sumOf { it.amountMl }

    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var selectedMealTypeForDialog by remember { mutableStateOf("BREAKFAST") }

    val breakfastEntries = foodEntries.filter { it.mealType.equals("BREAKFAST", ignoreCase = true) }
    val lunchEntries = foodEntries.filter { it.mealType.equals("LUNCH", ignoreCase = true) }
    val dinnerEntries = foodEntries.filter { it.mealType.equals("DINNER", ignoreCase = true) }
    val snackEntries = foodEntries.filter { it.mealType.equals("SNACK", ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.SmallFloatingActionButton(
                    onClick = {
                        selectedMealTypeForDialog = "BREAKFAST"
                        showBarcodeDialog = true
                    },
                    modifier = Modifier.testTag("fab_scan_barcode"),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Barcode"
                    )
                }
                FloatingActionButton(
                    onClick = {
                        selectedMealTypeForDialog = "BREAKFAST"
                        showAddFoodDialog = true
                    },
                    modifier = Modifier.testTag("fab_add_food"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Food")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("nutrition_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Bar
            item {
                DateSelectorBar(
                    currentDateStr = selectedDate,
                    onPreviousDay = { viewModel.stepDay(-1) },
                    onNextDay = { viewModel.stepDay(1) },
                    onToday = { viewModel.setDateToToday() }
                )
            }

            // Hero Summary Card
            item {
                NutritionSummaryCard(
                    entries = foodEntries,
                    target = target,
                    waterAmountMl = totalWater,
                    onAddWater = { ml -> viewModel.addWater(ml) }
                )
            }

            // Breakfast
            item {
                MealSectionCard(
                    mealType = "BREAKFAST",
                    title = "Breakfast",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    entries = breakfastEntries,
                    onAddFood = {
                        selectedMealTypeForDialog = "BREAKFAST"
                        showAddFoodDialog = true
                    },
                    onDeleteEntry = { id -> viewModel.deleteFood(id) }
                )
            }

            // Lunch
            item {
                MealSectionCard(
                    mealType = "LUNCH",
                    title = "Lunch",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LunchDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    entries = lunchEntries,
                    onAddFood = {
                        selectedMealTypeForDialog = "LUNCH"
                        showAddFoodDialog = true
                    },
                    onDeleteEntry = { id -> viewModel.deleteFood(id) }
                )
            }

            // Dinner
            item {
                MealSectionCard(
                    mealType = "DINNER",
                    title = "Dinner",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DinnerDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    entries = dinnerEntries,
                    onAddFood = {
                        selectedMealTypeForDialog = "DINNER"
                        showAddFoodDialog = true
                    },
                    onDeleteEntry = { id -> viewModel.deleteFood(id) }
                )
            }

            // Snacks
            item {
                MealSectionCard(
                    mealType = "SNACK",
                    title = "Snacks & Drinks",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.RamenDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    entries = snackEntries,
                    onAddFood = {
                        selectedMealTypeForDialog = "SNACK"
                        showAddFoodDialog = true
                    },
                    onDeleteEntry = { id -> viewModel.deleteFood(id) }
                )
            }
        }
    }

    if (showAddFoodDialog) {
        AddFoodDialog(
            initialMealType = selectedMealTypeForDialog,
            presets = viewModel.foodRepository.commonFoodPresets,
            onDismiss = { showAddFoodDialog = false },
            onAddFood = { mealType, name, cals, p, c, f, serving ->
                viewModel.addFood(mealType, name, cals, p, c, f, serving)
            }
        )
    }

    if (showBarcodeDialog) {
        BarcodeScanDialog(
            initialMealType = selectedMealTypeForDialog,
            onDismiss = { showBarcodeDialog = false },
            onAddFood = { mealType, name, cals, p, c, f, serving ->
                viewModel.addFood(mealType, name, cals, p, c, f, serving)
            }
        )
    }
}
