package com.example

import com.example.ui.components.getCurrentWeekDays
import com.example.ui.viewmodel.NutriHabitViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutriHabitUnitTest {

    @Test
    fun testDayOfWeekCalculation() {
        // Monday test
        val monDay = NutriHabitViewModel.getDayOfWeekShort("2026-10-12")
        assertEquals("MON", monDay)

        // Wednesday test
        val wedDay = NutriHabitViewModel.getDayOfWeekShort("2026-10-14")
        assertEquals("WED", wedDay)

        // Sunday test
        val sunDay = NutriHabitViewModel.getDayOfWeekShort("2026-10-18")
        assertEquals("SUN", sunDay)
    }

    @Test
    fun testHabitWeeklyRepetitionMatch() {
        val habitRepeatDays = "MON,WED,FRI"
        val repeatDaysList = habitRepeatDays.split(",").map { it.trim().uppercase() }

        assertTrue(repeatDaysList.contains("MON"))
        assertTrue(repeatDaysList.contains("WED"))
        assertTrue(repeatDaysList.contains("FRI"))
        assertFalse(repeatDaysList.contains("TUE"))
        assertFalse(repeatDaysList.contains("THU"))
        assertFalse(repeatDaysList.contains("SAT"))
        assertFalse(repeatDaysList.contains("SUN"))
    }

    @Test
    fun testCurrentWeekDaysGeneration() {
        val weekDays = getCurrentWeekDays("2026-10-14")
        assertEquals(7, weekDays.size)
        assertEquals("MON", weekDays[0].dayShort)
        assertEquals("SUN", weekDays[6].dayShort)
    }

    @Test
    fun testMacroCaloriesCalculation() {
        // 1g Protein = 4 kcal, 1g Carbs = 4 kcal, 1g Fat = 9 kcal
        val proteinGrams = 30f
        val carbsGrams = 40f
        val fatGrams = 10f

        val calculatedCalories = (proteinGrams * 4) + (carbsGrams * 4) + (fatGrams * 9)
        assertEquals(370f, calculatedCalories, 0.1f)
    }
}
