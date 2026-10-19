package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.FoodDatabaseService
import com.example.data.remote.FoodSearchResult
import com.example.data.repository.FoodPreset
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import kotlinx.coroutines.launch

@Composable
fun AddFoodDialog(
    initialMealType: String,
    presets: List<FoodPreset>,
    onDismiss: () -> Unit,
    onAddFood: (mealType: String, name: String, calories: Int, protein: Float, carbs: Float, fat: Float, servingSize: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Database & Search, 1: Scan Barcode, 2: Custom
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("add_food_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Food Database & Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Meal Type Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mealTypes.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = {
                                Text(
                                    text = type.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3 Tab selector: Food Database, Barcode Scanner, Custom Food
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Search Database") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan Barcode")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Custom") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> FoodDatabaseSearchView(
                        presets = presets,
                        onSelectFood = { name, cals, p, c, f, serving ->
                            onAddFood(selectedMealType, name, cals, p, c, f, serving)
                            onDismiss()
                        }
                    )
                    1 -> BarcodeScannerTabView(
                        onSelectFood = { name, cals, p, c, f, serving ->
                            onAddFood(selectedMealType, name, cals, p, c, f, serving)
                            onDismiss()
                        }
                    )
                    2 -> CustomFoodView(
                        onAddCustomFood = { name, cals, p, c, f, serving ->
                            onAddFood(selectedMealType, name, cals, p, c, f, serving)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FoodDatabaseSearchView(
    presets: List<FoodPreset>,
    onSelectFood: (name: String, calories: Int, protein: Float, carbs: Float, fat: Float, serving: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val foodDbService = remember { FoodDatabaseService() }

    var searchQuery by remember { mutableStateOf("") }
    var onlineResults by remember { mutableStateOf<List<FoodSearchResult>>(emptyList()) }
    var isSearchingOnline by remember { mutableStateOf(false) }

    val localFiltered = presets.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    fun triggerSearch() {
        if (searchQuery.isBlank()) return
        isSearchingOnline = true
        coroutineScope.launch {
            val results = foodDbService.searchFoodsOnline(searchQuery)
            onlineResults = results
            isSearchingOnline = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onlineResults = emptyList()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("food_search_input"),
                placeholder = { Text("Search millions of foods online...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { triggerSearch() }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = { triggerSearch() },
                enabled = searchQuery.isNotBlank() && !isSearchingOnline,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                if (isSearchingOnline) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Search")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Online Results Section
            if (onlineResults.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Online Food Database (${onlineResults.size} results)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                items(onlineResults) { food ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectFood(
                                    food.name,
                                    food.calories,
                                    food.protein,
                                    food.carbs,
                                    food.fat,
                                    food.servingSize
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                food.brand?.let { b ->
                                    Text(
                                        text = b.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${food.servingSize} • P: ${food.protein.toInt()}g  C: ${food.carbs.toInt()}g  F: ${food.fat.toInt()}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${food.calories} kcal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CalorieColor
                            )
                        }
                    }
                }
            }

            // Verified Staple Food Library
            item {
                Text(
                    text = if (onlineResults.isNotEmpty()) "Verified Food Staples" else "Verified Foods & Presets",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(localFiltered) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectFood(
                                preset.name,
                                preset.calories,
                                preset.protein,
                                preset.carbs,
                                preset.fat,
                                preset.servingSize
                            )
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.servingSize} • P: ${preset.protein.toInt()}g  C: ${preset.carbs.toInt()}g  F: ${preset.fat.toInt()}g",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${preset.calories} kcal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CalorieColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarcodeScannerTabView(
    onSelectFood: (name: String, calories: Int, protein: Float, carbs: Float, fat: Float, serving: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val foodDbService = remember { FoodDatabaseService() }

    var barcodeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var scannedProduct by remember { mutableStateOf<FoodSearchResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var quantityMultiplier by remember { mutableFloatStateOf(1.0f) }

    val sampleBarcodes = listOf(
        "052159701007" to "Greek Yogurt (Fage)",
        "030000041100" to "Rolled Oats (Quaker)",
        "049000028904" to "Coca Cola",
        "602652171917" to "Kind Bar",
        "3017620422003" to "Nutella",
        "044000032029" to "Oreo Cookies"
    )

    fun performLookup(code: String) {
        if (code.isBlank()) return
        isLoading = true
        errorMessage = null
        scannedProduct = null
        coroutineScope.launch {
            val result = foodDbService.getProductByBarcode(code)
            isLoading = false
            result.onSuccess { product ->
                scannedProduct = product
            }.onFailure { err ->
                errorMessage = "Product not found (${err.localizedMessage}). You can enter details manually in Custom tab."
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scanner Viewfinder Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 190.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = (laserOffset * 0.7f).dp)
                                .background(Color(0xFFEF4444))
                        )
                    }

                    Text(
                        text = "Point camera at food barcode",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Barcode input
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    placeholder = { Text("Enter barcode digits...") },
                    label = { Text("Barcode Number") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { performLookup(barcodeInput) }),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("barcode_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { performLookup(barcodeInput) },
                    enabled = barcodeInput.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("barcode_lookup_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Lookup")
                    }
                }
            }
        }

        // Tap Sample Barcodes
        item {
            Column {
                Text(
                    text = "Tap a Sample Barcode to Test:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleBarcodes) { (code, label) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    barcodeInput = code
                                    performLookup(code)
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let { err ->
            item {
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        scannedProduct?.let { prod ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanned_product_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                prod.brand?.let { b ->
                                    Text(
                                        text = b.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Serving: ${prod.servingSize}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(prod.calories * quantityMultiplier).toInt()} kcal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = CalorieColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = ProteinColor.copy(alpha = 0.15f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("Protein", style = MaterialTheme.typography.labelSmall, color = ProteinColor, fontWeight = FontWeight.Bold)
                                    Text("${(prod.protein * quantityMultiplier).toInt()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = CarbsColor.copy(alpha = 0.15f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("Carbs", style = MaterialTheme.typography.labelSmall, color = CarbsColor, fontWeight = FontWeight.Bold)
                                    Text("${(prod.carbs * quantityMultiplier).toInt()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = FatColor.copy(alpha = 0.15f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("Fat", style = MaterialTheme.typography.labelSmall, color = FatColor, fontWeight = FontWeight.Bold)
                                    Text("${(prod.fat * quantityMultiplier).toInt()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Portion:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { q ->
                                    FilterChip(
                                        selected = quantityMultiplier == q,
                                        onClick = { quantityMultiplier = q },
                                        label = { Text("${q}x") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val cals = (prod.calories * quantityMultiplier).toInt()
                                val p = prod.protein * quantityMultiplier
                                val c = prod.carbs * quantityMultiplier
                                val f = prod.fat * quantityMultiplier
                                val serving = if (quantityMultiplier == 1.0f) prod.servingSize else "${quantityMultiplier}x (${prod.servingSize})"
                                onSelectFood(prod.name, cals, p, c, f, serving)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log This Scanned Food", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFoodView(
    onAddCustomFood: (name: String, calories: Int, protein: Float, carbs: Float, fat: Float, serving: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("1 serving") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_food_name"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = caloriesStr,
                onValueChange = { caloriesStr = it },
                label = { Text("Calories (kcal) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_food_calories"),
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
                    value = proteinStr,
                    onValueChange = { proteinStr = it },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = carbsStr,
                    onValueChange = { carbsStr = it },
                    label = { Text("Carbs (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = fatStr,
                    onValueChange = { fatStr = it },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            OutlinedTextField(
                value = servingSize,
                onValueChange = { servingSize = it },
                label = { Text("Serving Size") },
                placeholder = { Text("e.g. 100g, 1 bowl, 2 slices") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val cals = caloriesStr.toIntOrNull() ?: 0
                    val p = proteinStr.toFloatOrNull() ?: 0f
                    val c = carbsStr.toFloatOrNull() ?: 0f
                    val f = fatStr.toFloatOrNull() ?: 0f
                    if (name.isNotBlank() && cals > 0) {
                        onAddCustomFood(name, cals, p, c, f, servingSize)
                    }
                },
                enabled = name.isNotBlank() && (caloriesStr.toIntOrNull() ?: 0) > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_custom_food_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Food Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}
