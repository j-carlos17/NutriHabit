package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FoodEntryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.NutritionTargetEntity
import com.example.data.local.entity.RoutineCompletionEntity
import com.example.data.local.entity.RoutineEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.WaterEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getFoodEntriesByDate(date: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun getFoodEntriesForDateRange(startDate: String, endDate: String): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(entry: FoodEntryEntity): Long

    @Update
    suspend fun updateFoodEntry(entry: FoodEntryEntity)

    @Delete
    suspend fun deleteFoodEntry(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteFoodEntryById(id: Long)

    @Query("SELECT * FROM nutrition_targets WHERE id = 1 LIMIT 1")
    fun getDailyTarget(): Flow<NutritionTargetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTarget(target: NutritionTargetEntity)

    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getWaterEntriesByDate(date: String): Flow<List<WaterEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntryEntity): Long

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteWaterEntryById(id: Long)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY id ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getHabitLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    fun getHabitLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date BETWEEN :startDate AND :endDate")
    fun getHabitLogsInRange(startDate: String, endDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getAllLogsForHabitDirect(habitId: Long): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Long, date: String)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesForRoutineDirect(routineId: Long): List<RoutineExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesForRoutine(routineId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: RoutineCompletionEntity): Long

    @Query("SELECT * FROM routine_completions WHERE date = :date ORDER BY timestamp DESC")
    fun getCompletionsForDate(date: String): Flow<List<RoutineCompletionEntity>>

    @Query("SELECT * FROM routine_completions ORDER BY timestamp DESC LIMIT 30")
    fun getRecentCompletions(): Flow<List<RoutineCompletionEntity>>
}
