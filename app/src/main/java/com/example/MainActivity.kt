package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.theme.NutriHabitTheme
import com.example.ui.viewmodel.NutriHabitViewModel

enum class AppNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    NUTRITION("nutrition", "Calories & Macros", Icons.Default.Restaurant, "nav_nutrition"),
    HABITS("habits", "Daily Habits", Icons.Default.TaskAlt, "nav_habits"),
    ROUTINES("routines", "Routines", Icons.Default.FitnessCenter, "nav_routines"),
    GOALS("goals", "Targets & Goals", Icons.Default.TrackChanges, "nav_goals")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriHabitTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: NutriHabitViewModel = viewModel()
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppNavDestination.NUTRITION) }

    // Handle back button: if not on Home/Nutrition, return to Nutrition first
    if (currentDestination != AppNavDestination.NUTRITION) {
        BackHandler {
            currentDestination = AppNavDestination.NUTRITION
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Tablet / Landscape layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AppNavDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) },
                            modifier = Modifier.testTag(destination.tag)
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentDestination.title,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentDestination) {
                            AppNavDestination.NUTRITION -> NutritionScreen(viewModel)
                            AppNavDestination.HABITS -> HabitsScreen(viewModel)
                            AppNavDestination.ROUTINES -> RoutinesScreen(viewModel)
                            AppNavDestination.GOALS -> GoalsScreen(viewModel)
                        }
                    }
                }
            }
        } else {
            // Standard Mobile Layout with bottom NavigationBar
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = currentDestination.title,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_nav_bar"),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppNavDestination.values().forEach { destination ->
                            NavigationBarItem(
                                selected = currentDestination == destination,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = { Text(destination.title) },
                                modifier = Modifier.testTag(destination.tag)
                            )
                        }
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentDestination) {
                        AppNavDestination.NUTRITION -> NutritionScreen(viewModel)
                        AppNavDestination.HABITS -> HabitsScreen(viewModel)
                        AppNavDestination.ROUTINES -> RoutinesScreen(viewModel)
                        AppNavDestination.GOALS -> GoalsScreen(viewModel)
                    }
                }
            }
        }
    }
}
