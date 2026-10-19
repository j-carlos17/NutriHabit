package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FoodEntryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.NutritionTargetEntity
import com.example.data.local.entity.RoutineCompletionEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.WaterEntryEntity
import com.example.data.repository.FoodPreset
import com.example.data.repository.FoodRepository
import com.example.data.repository.HabitRepository
import com.example.data.repository.HabitWithStatus
import com.example.data.repository.RoutineRepository
import com.example.data.repository.RoutineWithExercises
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NutriHabitViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val foodRepository = FoodRepository(db.foodDao())
    val habitRepository = HabitRepository(db.habitDao())
    val routineRepository = RoutineRepository(db.routineDao())

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Current selected date
    private val _selectedDate = MutableStateFlow(sdf.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun stepDay(offset: Int) {
        try {
            val date = sdf.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, offset)
            _selectedDate.value = sdf.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = sdf.format(Date())
        }
    }

    fun setDateToToday() {
        _selectedDate.value = sdf.format(Date())
    }

    // Daily Nutrition Target
    val nutritionTarget: StateFlow<NutritionTargetEntity> = foodRepository.getDailyTarget()
        .combine(MutableStateFlow(Unit)) { target, _ ->
            target ?: NutritionTargetEntity()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NutritionTargetEntity()
        )

    // Food Entries for selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val foodEntries: StateFlow<List<FoodEntryEntity>> = _selectedDate
        .flatMapLatest { date -> foodRepository.getEntriesForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Water entries for selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val waterEntries: StateFlow<List<WaterEntryEntity>> = _selectedDate
        .flatMapLatest { date -> foodRepository.getWaterForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All active habits
    val allHabits: StateFlow<List<HabitEntity>> = habitRepository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Habit logs for selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDayHabitLogs: StateFlow<List<HabitLogEntity>> = _selectedDate
        .flatMapLatest { date -> habitRepository.getLogsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Habit status enriched with week map and streak
    val habitsWithStatus: StateFlow<List<HabitWithStatus>> = combine(
        allHabits,
        _selectedDate,
        currentDayHabitLogs
    ) { habits, dateStr, logs ->
        val completedIds = logs.filter { it.completed }.map { it.habitId }.toSet()
        val currentDayOfWeek = getDayOfWeekShort(dateStr)

        habits.map { habit ->
            HabitWithStatus(
                habit = habit,
                isCompletedToday = completedIds.contains(habit.id),
                currentStreak = 0, // calculated async or on demand
                weeklyCompletions = emptyMap()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Routines
    val allRoutines: StateFlow<List<RoutineEntity>> = routineRepository.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Routine completions for selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val routineCompletionsForDate: StateFlow<List<RoutineCompletionEntity>> = _selectedDate
        .flatMapLatest { date -> routineRepository.getCompletionsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentCompletions: StateFlow<List<RoutineCompletionEntity>> = routineRepository.getRecentCompletions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active Routine Running State
    private val _activeRoutine = MutableStateFlow<RoutineWithExercises?>(null)
    val activeRoutine: StateFlow<RoutineWithExercises?> = _activeRoutine.asStateFlow()

    fun startRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            val rWithEx = routineRepository.getRoutineWithExercises(routine.id)
            _activeRoutine.value = rWithEx
        }
    }

    fun exitRoutine() {
        _activeRoutine.value = null
    }

    fun completeRoutine(durationMins: Int, caloriesBurned: Int) {
        val routine = _activeRoutine.value?.routine ?: return
        viewModelScope.launch {
            routineRepository.logRoutineCompletion(
                routineId = routine.id,
                routineName = routine.name,
                date = _selectedDate.value,
                durationMinutes = durationMins,
                caloriesBurned = caloriesBurned
            )
            _activeRoutine.value = null
        }
    }

    // Food Operations
    fun addFood(
        mealType: String,
        name: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        servingSize: String,
        quantity: Float = 1.0f
    ) {
        viewModelScope.launch {
            foodRepository.insertFood(
                FoodEntryEntity(
                    date = _selectedDate.value,
                    mealType = mealType,
                    name = name,
                    calories = (calories * quantity).toInt(),
                    protein = protein * quantity,
                    carbs = carbs * quantity,
                    fat = fat * quantity,
                    servingSize = servingSize,
                    quantity = quantity
                )
            )
        }
    }

    fun logPreset(preset: FoodPreset, mealType: String = preset.defaultMealType, quantity: Float = 1.0f) {
        addFood(
            mealType = mealType,
            name = preset.name,
            calories = preset.calories,
            protein = preset.protein,
            carbs = preset.carbs,
            fat = preset.fat,
            servingSize = preset.servingSize,
            quantity = quantity
        )
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            foodRepository.deleteFood(id)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            foodRepository.addWater(_selectedDate.value, amountMl)
        }
    }

    fun deleteWater(id: Long) {
        viewModelScope.launch {
            foodRepository.deleteWater(id)
        }
    }

    fun updateNutritionTarget(calories: Int, protein: Float, carbs: Float, fat: Float, waterMl: Int) {
        viewModelScope.launch {
            foodRepository.updateDailyTarget(
                NutritionTargetEntity(
                    id = 1,
                    targetCalories = calories,
                    targetProtein = protein,
                    targetCarbs = carbs,
                    targetFat = fat,
                    targetWaterMl = waterMl
                )
            )
        }
    }

    // Habit Operations
    fun toggleHabit(habitId: Long, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            habitRepository.toggleHabit(habitId, _selectedDate.value, isCurrentlyCompleted)
        }
    }

    fun addHabit(
        name: String,
        category: String,
        icon: String,
        colorHex: String,
        repeatDays: String
    ) {
        viewModelScope.launch {
            habitRepository.createHabit(
                HabitEntity(
                    name = name,
                    category = category,
                    icon = icon,
                    colorHex = colorHex,
                    repeatDays = repeatDays
                )
            )
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    // Routine Operations
    fun createRoutine(
        name: String,
        description: String,
        category: String,
        estimatedMinutes: Int,
        targetDays: String,
        colorHex: String,
        exercises: List<RoutineExerciseEntity>
    ) {
        viewModelScope.launch {
            routineRepository.createRoutine(
                RoutineEntity(
                    name = name,
                    description = description,
                    category = category,
                    estimatedMinutes = estimatedMinutes,
                    targetDays = targetDays,
                    colorHex = colorHex
                ),
                exercises
            )
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }

    companion object {
        fun getDayOfWeekShort(dateStr: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(dateStr) ?: Date()
                val cal = Calendar.getInstance()
                cal.time = date
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "MON"
                    Calendar.TUESDAY -> "TUE"
                    Calendar.WEDNESDAY -> "WED"
                    Calendar.THURSDAY -> "THU"
                    Calendar.FRIDAY -> "FRI"
                    Calendar.SATURDAY -> "SAT"
                    Calendar.SUNDAY -> "SUN"
                    else -> "MON"
                }
            } catch (e: Exception) {
                "MON"
            }
        }
    }
}
