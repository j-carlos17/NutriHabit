package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FoodSearchResult(
    val barcode: String? = null,
    val name: String,
    val brand: String? = null,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: String = "100g",
    val source: String = "Open Food Facts"
)

class FoodDatabaseService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun getProductByBarcode(barcode: String): Result<FoodSearchResult> = withContext(Dispatchers.IO) {
        try {
            val cleanBarcode = barcode.trim()
            val url = "https://world.openfoodfacts.org/api/v2/product/$cleanBarcode.json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "NutriHabitAndroidApp/1.0 (contact@nutrihabit.app)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error code: ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val json = JSONObject(body)
            val status = json.optInt("status", 0)
            if (status != 1) {
                return@withContext Result.failure(Exception("Product not found for barcode: $cleanBarcode"))
            }

            val product = json.getJSONObject("product")
            val name = product.optString("product_name", product.optString("product_name_en", "Unknown Product"))
            val brand = product.optString("brands", "").ifBlank { null }
            val serving = product.optString("serving_size", "100g")

            val nutriments = product.optJSONObject("nutriments")
            var calories = 0
            var protein = 0f
            var carbs = 0f
            var fat = 0f

            if (nutriments != null) {
                // Try serving-based first, otherwise 100g
                calories = nutriments.optDouble("energy-kcal_serving", -1.0).let {
                    if (it >= 0) it.toInt() else nutriments.optDouble("energy-kcal_100g", 0.0).toInt()
                }
                if (calories == 0) {
                    val energyKj = nutriments.optDouble("energy_100g", 0.0)
                    if (energyKj > 0) calories = (energyKj / 4.184).toInt()
                }

                protein = nutriments.optDouble("proteins_serving", -1.0).let {
                    if (it >= 0) it.toFloat() else nutriments.optDouble("proteins_100g", 0.0).toFloat()
                }

                carbs = nutriments.optDouble("carbohydrates_serving", -1.0).let {
                    if (it >= 0) it.toFloat() else nutriments.optDouble("carbohydrates_100g", 0.0).toFloat()
                }

                fat = nutriments.optDouble("fat_serving", -1.0).let {
                    if (it >= 0) it.toFloat() else nutriments.optDouble("fat_100g", 0.0).toFloat()
                }
            }

            val result = FoodSearchResult(
                barcode = cleanBarcode,
                name = name,
                brand = brand,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                servingSize = serving,
                source = "Open Food Facts"
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e("FoodDatabaseService", "Barcode lookup failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun searchFoodsOnline(query: String): List<FoodSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encodedQuery&search_simple=1&action=process&json=1&page_size=20"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "NutriHabitAndroidApp/1.0 (contact@nutrihabit.app)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val products = json.optJSONArray("products") ?: return@withContext emptyList()

            val results = mutableListOf<FoodSearchResult>()
            for (i in 0 until products.length()) {
                val prod = products.getJSONObject(i)
                val name = prod.optString("product_name", prod.optString("product_name_en", ""))
                if (name.isBlank()) continue

                val brand = prod.optString("brands", "").ifBlank { null }
                val serving = prod.optString("serving_size", "100g")
                val barcode = prod.optString("code", "")

                val nutriments = prod.optJSONObject("nutriments")
                var cals = 0
                var p = 0f
                var c = 0f
                var f = 0f

                if (nutriments != null) {
                    cals = nutriments.optDouble("energy-kcal_100g", 0.0).toInt()
                    if (cals == 0) {
                        val kj = nutriments.optDouble("energy_100g", 0.0)
                        if (kj > 0) cals = (kj / 4.184).toInt()
                    }
                    p = nutriments.optDouble("proteins_100g", 0.0).toFloat()
                    c = nutriments.optDouble("carbohydrates_100g", 0.0).toFloat()
                    f = nutriments.optDouble("fat_100g", 0.0).toFloat()
                }

                results.add(
                    FoodSearchResult(
                        barcode = barcode.ifBlank { null },
                        name = name,
                        brand = brand,
                        calories = cals,
                        protein = p,
                        carbs = c,
                        fat = f,
                        servingSize = serving,
                        source = "Open Food Facts"
                    )
                )
            }
            results
        } catch (e: Exception) {
            Log.e("FoodDatabaseService", "Online search failed: ${e.message}", e)
            emptyList()
        }
    }
}
