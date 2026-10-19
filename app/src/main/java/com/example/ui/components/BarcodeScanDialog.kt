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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.FoodDatabaseService
import com.example.data.remote.FoodSearchResult
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import kotlinx.coroutines.launch

@Composable
fun BarcodeScanDialog(
    initialMealType: String,
    onDismiss: () -> Unit,
    onAddFood: (mealType: String, name: String, calories: Int, protein: Float, carbs: Float, fat: Float, servingSize: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val foodDbService = remember { FoodDatabaseService() }

    var barcodeInput by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf(initialMealType) }
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
                errorMessage = "Product not found (${err.localizedMessage}). You can enter details manually or try another barcode."
            }
        }
    }

    // Laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 110f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("barcode_scan_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Barcode Nutrition Scanner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Meal Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK").forEach { type ->
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

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Scanner Viewfinder Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Viewfinder frame
                                Box(
                                    modifier = Modifier
                                        .size(width = 200.dp, height = 90.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                ) {
                                    // Animated Laser Line
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .offset(y = (laserOffset * 0.7f).dp)
                                            .background(Color(0xFFEF4444))
                                    )
                                }

                                Text(
                                    text = "Ready to scan barcode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }

                    // Barcode Search Input
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = barcodeInput,
                                onValueChange = { barcodeInput = it },
                                placeholder = { Text("Enter or paste barcode digits...") },
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
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Lookup")
                                }
                            }
                        }
                    }

                    // Quick Test Sample Barcodes
                    item {
                        Column {
                            Text(
                                text = "Or Tap a Verified Sample Barcode:",
                                style = MaterialTheme.typography.labelMedium,
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
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Error Message
                    errorMessage?.let { err ->
                        item {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Scanned Product Result Card
                    scannedProduct?.let { prod ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scanned_product_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
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

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Macro pills
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = ProteinColor.copy(alpha = 0.15f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Protein", style = MaterialTheme.typography.labelSmall, color = ProteinColor, fontWeight = FontWeight.Bold)
                                                Text("${(prod.protein * quantityMultiplier).toInt()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = CarbsColor.copy(alpha = 0.15f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Carbs", style = MaterialTheme.typography.labelSmall, color = CarbsColor, fontWeight = FontWeight.Bold)
                                                Text("${(prod.carbs * quantityMultiplier).toInt()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            color = FatColor.copy(alpha = 0.15f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Fat", style = MaterialTheme.typography.labelSmall, color = FatColor, fontWeight = FontWeight.Bold)
                                                Text("${(prod.fat * quantityMultiplier).toInt()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Servings quantity multiplier
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Servings:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { q ->
                                                FilterChip(
                                                    selected = quantityMultiplier == q,
                                                    onClick = { quantityMultiplier = q },
                                                    label = { Text("${q}x") }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            val cals = (prod.calories * quantityMultiplier).toInt()
                                            val p = prod.protein * quantityMultiplier
                                            val c = prod.carbs * quantityMultiplier
                                            val f = prod.fat * quantityMultiplier
                                            val serving = if (quantityMultiplier == 1.0f) prod.servingSize else "${quantityMultiplier}x (${prod.servingSize})"
                                            onAddFood(selectedMealType, prod.name, cals, p, c, f, serving)
                                            onDismiss()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("log_scanned_food_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Log to ${selectedMealType.lowercase().replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
