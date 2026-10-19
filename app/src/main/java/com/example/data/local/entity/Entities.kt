package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_entries",
    indices = [Index(value = ["date"]), Index(value = ["mealType"])]
)
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACK
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: String,
    val quantity: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "nutrition_targets")
data class NutritionTargetEntity(
    @PrimaryKey val id: Int = 1,
    val targetCalories: Int = 2200,
    val targetProtein: Float = 140f,
    val targetCarbs: Float = 240f,
    val targetFat: Float = 65f,
    val targetWaterMl: Int = 2500
)

@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["date"])]
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Health, Fitness, Nutrition, Mindset
    val icon: String, // water, run, gym, apple, book, sleep, check
    val colorHex: String,
    val repeatDays: String, // comma separated: MON,TUE,WED,THU,FRI,SAT,SUN
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // YYYY-MM-DD
    val completed: Boolean = true,
    val completedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: String, // Strength, Cardio, Core, Mobility
    val estimatedMinutes: Int,
    val targetDays: String, // e.g. MON,WED,FRI
    val colorHex: String = "#10B981",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_exercises",
    indices = [Index(value = ["routineId"])]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val name: String,
    val sets: Int,
    val repsOrDuration: String,
    val restSeconds: Int,
    val orderIndex: Int
)

@Entity(
    tableName = "routine_completions",
    indices = [Index(value = ["date"])]
)
data class RoutineCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val routineName: String,
    val date: String, // YYYY-MM-DD
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val timestamp: Long = System.currentTimeMillis()
)
