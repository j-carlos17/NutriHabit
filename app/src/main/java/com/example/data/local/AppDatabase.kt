package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        FoodEntryEntity::class,
        NutritionTargetEntity::class,
        WaterEntryEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        RoutineCompletionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun habitDao(): HabitDao
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutri_habit_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial realistic data in background
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Seed Target
            db.foodDao().insertOrUpdateTarget(
                NutritionTargetEntity(
                    id = 1,
                    targetCalories = 2200,
                    targetProtein = 150f,
                    targetCarbs = 230f,
                    targetFat = 65f,
                    targetWaterMl = 2500
                )
            )

            // Seed some food entries for today
            db.foodDao().insertFoodEntry(
                FoodEntryEntity(
                    date = todayStr,
                    mealType = "BREAKFAST",
                    name = "Oatmeal with Whey & Berries",
                    calories = 420,
                    protein = 32f,
                    carbs = 54f,
                    fat = 7f,
                    servingSize = "1 bowl",
                    quantity = 1f
                )
            )
            db.foodDao().insertFoodEntry(
                FoodEntryEntity(
                    date = todayStr,
                    mealType = "LUNCH",
                    name = "Grilled Chicken Breast, Jasmine Rice & Broccoli",
                    calories = 580,
                    protein = 48f,
                    carbs = 62f,
                    fat = 12f,
                    servingSize = "1 plate",
                    quantity = 1f
                )
            )
            db.foodDao().insertWaterEntry(
                WaterEntryEntity(
                    date = todayStr,
                    amountMl = 750
                )
            )

            // Seed Habits repeating weekly
            val habit1 = db.habitDao().insertHabit(
                HabitEntity(
                    name = "Drink 2.5L Water",
                    category = "Health",
                    icon = "water",
                    colorHex = "#06B6D4",
                    repeatDays = "MON,TUE,WED,THU,FRI,SAT,SUN"
                )
            )
            val habit2 = db.habitDao().insertHabit(
                HabitEntity(
                    name = "Hit 140g+ Protein Goal",
                    category = "Nutrition",
                    icon = "apple",
                    colorHex = "#3B82F6",
                    repeatDays = "MON,TUE,WED,THU,FRI,SAT,SUN"
                )
            )
            val habit3 = db.habitDao().insertHabit(
                HabitEntity(
                    name = "Daily Workout or 8k Steps",
                    category = "Fitness",
                    icon = "run",
                    colorHex = "#10B981",
                    repeatDays = "MON,TUE,WED,THU,FRI,SAT"
                )
            )
            val habit4 = db.habitDao().insertHabit(
                HabitEntity(
                    name = "Morning Mobility & Stretch",
                    category = "Fitness",
                    icon = "gym",
                    colorHex = "#8B5CF6",
                    repeatDays = "MON,TUE,WED,THU,FRI,SAT,SUN"
                )
            )
            val habit5 = db.habitDao().insertHabit(
                HabitEntity(
                    name = "Sleep 7.5+ Hours",
                    category = "Health",
                    icon = "sleep",
                    colorHex = "#F59E0B",
                    repeatDays = "MON,TUE,WED,THU,FRI,SAT,SUN"
                )
            )

            // Mark habit1 as checked for today as a demo
            db.habitDao().insertOrReplaceLog(
                HabitLogEntity(
                    habitId = habit1,
                    date = todayStr,
                    completed = true
                )
            )

            // Seed Workout Routines
            val routine1Id = db.routineDao().insertRoutine(
                RoutineEntity(
                    name = "Full Body Strength",
                    description = "Compound strength training routine targeting major muscle groups.",
                    category = "Strength",
                    estimatedMinutes = 45,
                    targetDays = "MON,WED,FRI",
                    colorHex = "#10B981"
                )
            )
            db.routineDao().insertExercises(
                listOf(
                    RoutineExerciseEntity(routineId = routine1Id, name = "Barbell Squats", sets = 4, repsOrDuration = "8-10 reps", restSeconds = 90, orderIndex = 0),
                    RoutineExerciseEntity(routineId = routine1Id, name = "Flat Bench Press", sets = 4, repsOrDuration = "8-10 reps", restSeconds = 90, orderIndex = 1),
                    RoutineExerciseEntity(routineId = routine1Id, name = "Bent-Over Barbell Rows", sets = 3, repsOrDuration = "10-12 reps", restSeconds = 75, orderIndex = 2),
                    RoutineExerciseEntity(routineId = routine1Id, name = "Overhead Dumbbell Press", sets = 3, repsOrDuration = "10 reps", restSeconds = 60, orderIndex = 3),
                    RoutineExerciseEntity(routineId = routine1Id, name = "Forearm Plank", sets = 3, repsOrDuration = "45 sec", restSeconds = 45, orderIndex = 4)
                )
            )

            val routine2Id = db.routineDao().insertRoutine(
                RoutineEntity(
                    name = "HIIT Cardio & Core Burner",
                    description = "High intensity interval workout to elevate heart rate and incinerate calories.",
                    category = "Cardio",
                    estimatedMinutes = 25,
                    targetDays = "TUE,THU,SAT",
                    colorHex = "#F97316"
                )
            )
            db.routineDao().insertExercises(
                listOf(
                    RoutineExerciseEntity(routineId = routine2Id, name = "Jumping Jacks", sets = 3, repsOrDuration = "45 sec", restSeconds = 20, orderIndex = 0),
                    RoutineExerciseEntity(routineId = routine2Id, name = "Mountain Climbers", sets = 3, repsOrDuration = "40 sec", restSeconds = 20, orderIndex = 1),
                    RoutineExerciseEntity(routineId = routine2Id, name = "Burpees", sets = 3, repsOrDuration = "30 sec", restSeconds = 30, orderIndex = 2),
                    RoutineExerciseEntity(routineId = routine2Id, name = "Bicycle Crunches", sets = 3, repsOrDuration = "45 sec", restSeconds = 20, orderIndex = 3),
                    RoutineExerciseEntity(routineId = routine2Id, name = "High Knees Sprint", sets = 3, repsOrDuration = "30 sec", restSeconds = 30, orderIndex = 4)
                )
            )

            val routine3Id = db.routineDao().insertRoutine(
                RoutineEntity(
                    name = "Morning Mobility & Spine Flow",
                    description = "Gentle joint mobility and stretching to awaken the body and improve posture.",
                    category = "Mobility",
                    estimatedMinutes = 15,
                    targetDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    colorHex = "#06B6D4"
                )
            )
            db.routineDao().insertExercises(
                listOf(
                    RoutineExerciseEntity(routineId = routine3Id, name = "Cat-Cow Stretch", sets = 2, repsOrDuration = "60 sec", restSeconds = 15, orderIndex = 0),
                    RoutineExerciseEntity(routineId = routine3Id, name = "World's Greatest Stretch", sets = 2, repsOrDuration = "5 reps/side", restSeconds = 15, orderIndex = 1),
                    RoutineExerciseEntity(routineId = routine3Id, name = "90/90 Hip Swivels", sets = 2, repsOrDuration = "8 reps/side", restSeconds = 15, orderIndex = 2),
                    RoutineExerciseEntity(routineId = routine3Id, name = "Bird Dog Core Holds", sets = 2, repsOrDuration = "10 reps/side", restSeconds = 20, orderIndex = 3)
                )
            )
        }
    }
}
