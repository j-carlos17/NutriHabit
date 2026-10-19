package com.example.data.repository

import com.example.data.local.dao.FoodDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.RoutineDao
import com.example.data.local.entity.FoodEntryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.NutritionTargetEntity
import com.example.data.local.entity.RoutineCompletionEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.WaterEntryEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FoodPreset(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: String,
    val defaultMealType: String
)

data class RoutineWithExercises(
    val routine: RoutineEntity,
    val exercises: List<RoutineExerciseEntity>
)

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val weeklyCompletions: Map<String, Boolean> // dayOfWeek (e.g. "MON") -> Boolean
)

class FoodRepository(private val foodDao: FoodDao) {
    fun getEntriesForDate(date: String): Flow<List<FoodEntryEntity>> =
        foodDao.getFoodEntriesByDate(date)

    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<FoodEntryEntity>> =
        foodDao.getFoodEntriesForDateRange(startDate, endDate)

    suspend fun insertFood(entry: FoodEntryEntity): Long =
        foodDao.insertFoodEntry(entry)

    suspend fun updateFood(entry: FoodEntryEntity) =
        foodDao.updateFoodEntry(entry)

    suspend fun deleteFood(id: Long) =
        foodDao.deleteFoodEntryById(id)

    fun getDailyTarget(): Flow<NutritionTargetEntity?> =
        foodDao.getDailyTarget()

    suspend fun updateDailyTarget(target: NutritionTargetEntity) =
        foodDao.insertOrUpdateTarget(target)

    fun getWaterForDate(date: String): Flow<List<WaterEntryEntity>> =
        foodDao.getWaterEntriesByDate(date)

    suspend fun addWater(date: String, amountMl: Int): Long =
        foodDao.insertWaterEntry(WaterEntryEntity(date = date, amountMl = amountMl))

    suspend fun deleteWater(id: Long) =
        foodDao.deleteWaterEntryById(id)

    val commonFoodPresets: List<FoodPreset> = listOf(
        FoodPreset("Large Whole Egg (Boiled/Fried)", 74, 6.3f, 0.4f, 5.0f, "1 large (50g)", "BREAKFAST"),
        FoodPreset("Rolled Oats with Milk", 300, 12f, 48f, 6.5f, "1 bowl (75g)", "BREAKFAST"),
        FoodPreset("Whey Protein Shake (1 scoop)", 130, 25f, 3f, 1.5f, "1 scoop (30g)", "SNACK"),
        FoodPreset("Greek Yogurt (Plain 0%)", 100, 17f, 6f, 0.5f, "1 cup (170g)", "SNACK"),
        FoodPreset("Grilled Chicken Breast", 230, 43f, 0f, 5.0f, "1 fillet (165g)", "LUNCH"),
        FoodPreset("Steamed Jasmine Rice", 205, 4.2f, 45f, 0.4f, "1 cup (158g)", "LUNCH"),
        FoodPreset("Fresh Atlantic Salmon", 280, 34f, 0f, 15f, "1 fillet (170g)", "DINNER"),
        FoodPreset("Roasted Sweet Potato", 112, 2.0f, 26f, 0.1f, "1 medium (130g)", "DINNER"),
        FoodPreset("Mixed Green Salad w/ Olive Oil", 140, 2f, 5f, 12f, "1 plate", "LUNCH"),
        FoodPreset("Avocado Toast", 240, 6f, 24f, 14f, "1 slice", "BREAKFAST"),
        FoodPreset("Apple with Peanut Butter", 195, 4f, 25f, 9.5f, "1 apple + 1 tbsp", "SNACK"),
        FoodPreset("Almonds (Handful)", 164, 6f, 6f, 14f, "1 oz (28g)", "SNACK"),
        FoodPreset("Lean Ground Beef 90/10", 290, 30f, 0f, 18f, "1 patty (150g)", "DINNER"),
        FoodPreset("Banana (Medium)", 105, 1.3f, 27f, 0.4f, "1 medium (118g)", "SNACK"),
        FoodPreset("Whole Wheat Pasta Bolognese", 480, 28f, 62f, 14f, "1 plate", "DINNER")
    )
}

class HabitRepository(private val habitDao: HabitDao) {
    fun getAllHabits(): Flow<List<HabitEntity>> =
        habitDao.getAllActiveHabits()

    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>> =
        habitDao.getHabitLogsForDate(date)

    fun getLogsInRange(startDate: String, endDate: String): Flow<List<HabitLogEntity>> =
        habitDao.getHabitLogsInRange(startDate, endDate)

    suspend fun createHabit(habit: HabitEntity): Long =
        habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) =
        habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) =
        habitDao.deleteHabit(habit)

    suspend fun toggleHabit(habitId: Long, date: String, currentlyCompleted: Boolean) {
        if (currentlyCompleted) {
            habitDao.deleteLog(habitId, date)
        } else {
            habitDao.insertOrReplaceLog(
                HabitLogEntity(
                    habitId = habitId,
                    date = date,
                    completed = true
                )
            )
        }
    }

    suspend fun calculateStreak(habitId: Long): Int {
        val logs = habitDao.getAllLogsForHabitDirect(habitId)
        if (logs.isEmpty()) return 0
        val completedDates = logs.map { it.date }.toSet()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        var streak = 0

        // Check if completed today, or if yesterday was completed
        val todayStr = sdf.format(cal.time)
        val completedToday = completedDates.contains(todayStr)
        if (!completedToday) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)
            if (!completedDates.contains(yesterdayStr)) {
                return 0
            }
        }

        // Count consecutive days backward
        while (true) {
            val dStr = sdf.format(cal.time)
            if (completedDates.contains(dStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}

class RoutineRepository(private val routineDao: RoutineDao) {
    fun getAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>> =
        routineDao.getExercisesForRoutine(routineId)

    suspend fun getRoutineWithExercises(routineId: Long): RoutineWithExercises? {
        val routine = routineDao.getRoutineById(routineId) ?: return null
        val exercises = routineDao.getExercisesForRoutineDirect(routineId)
        return RoutineWithExercises(routine, exercises)
    }

    suspend fun createRoutine(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>): Long {
        val id = routineDao.insertRoutine(routine)
        val ordered = exercises.mapIndexed { index, ex ->
            ex.copy(routineId = id, orderIndex = index)
        }
        routineDao.insertExercises(ordered)
        return id
    }

    suspend fun updateRoutine(routine: RoutineEntity, exercises: List<RoutineExerciseEntity>) {
        routineDao.updateRoutine(routine)
        routineDao.deleteExercisesForRoutine(routine.id)
        val ordered = exercises.mapIndexed { index, ex ->
            ex.copy(routineId = routine.id, orderIndex = index)
        }
        routineDao.insertExercises(ordered)
    }

    suspend fun deleteRoutine(routine: RoutineEntity) {
        routineDao.deleteExercisesForRoutine(routine.id)
        routineDao.deleteRoutine(routine)
    }

    suspend fun logRoutineCompletion(
        routineId: Long,
        routineName: String,
        date: String,
        durationMinutes: Int,
        caloriesBurned: Int
    ): Long {
        return routineDao.insertCompletion(
            RoutineCompletionEntity(
                routineId = routineId,
                routineName = routineName,
                date = date,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned
            )
        )
    }

    fun getCompletionsForDate(date: String): Flow<List<RoutineCompletionEntity>> =
        routineDao.getCompletionsForDate(date)

    fun getRecentCompletions(): Flow<List<RoutineCompletionEntity>> =
        routineDao.getRecentCompletions()
}
