package com.example

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FoodItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val DarkBg = Color(0xFF09090A)
val SurfaceDark = Color(0xFF161618)
val SurfaceLightDark = Color(0xFF262626)
val GreenAccent = Color(0xFF5EFAA3)
val TextGray = Color(0xFFA0A0A0)
val RedClose = Color(0xFFF18381)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(viewModel: MainViewModel) {
    val allFoods by viewModel.allFoods.collectAsState()
    val scanningState by viewModel.scanningState.collectAsState()
    val context = LocalContext.current

    var amountString by remember { mutableStateOf("") }
    var selectedMeal by remember { mutableStateOf("Breakfast") }
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    var isCustomExpanded by remember { mutableStateOf(false) }
    var foodName by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Handle AI success
    LaunchedEffect(scanningState) {
        if (scanningState is ScanningState.Success) {
            val item = (scanningState as ScanningState.Success).foodItem
            foodName = item.name
            caloriesStr = item.calories.toString()
            carbsStr = item.carbs.toString()
            proteinStr = item.protein.toString()
            fatStr = item.fat.toString()
            isCustomExpanded = true
            viewModel.clearScanningState()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bmp = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            viewModel.scanLabel(bmp)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.scanLabel(it) }
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            Column(Modifier.background(DarkBg)) {
                Button(
                    onClick = {
                        val factor = (amountString.toFloatOrNull() ?: 100f) / 100f
                        val finalCals = ((caloriesStr.toFloatOrNull() ?: 0f) * factor).toInt()
                        val finalCarbs = ((carbsStr.toFloatOrNull() ?: 0f) * factor).toInt()
                        val finalProtein = ((proteinStr.toFloatOrNull() ?: 0f) * factor).toInt()
                        val finalFat = ((fatStr.toFloatOrNull() ?: 0f) * factor).toInt()

                        val foodToLog = FoodItem(
                            name = foodName.ifBlank { "Custom Food" },
                            calories = finalCals,
                            carbs = finalCarbs,
                            protein = finalProtein,
                            fat = finalFat,
                            servingSize = "${amountString.ifBlank { "100" }}g"
                        )
                        // Also save it to custom foods
                        viewModel.addFood(foodToLog)
                        
                        val timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        viewModel.addDiaryEntry(selectedMeal, timeStr, foodToLog)
                        
                        // Reset forms
                        amountString = ""
                        foodName = ""
                        caloriesStr = ""
                        carbsStr = ""
                        proteinStr = ""
                        fatStr = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent, contentColor = Color.Black)
                ) {
                    Text("Lock in food", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = { /* Could dismiss but since it's a screen, maybe just clear */ },
                    modifier = Modifier.align(Alignment.End).padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Text("Discard", color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Log Food", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Log customized food amount and tracking details.",
                color = TextGray, fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Amount Input
            Text("Amount (grams)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amountString,
                onValueChange = { amountString = it },
                placeholder = { Text("e.g. 100, 250", color = TextGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = GreenAccent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Select Meal
            Text("Select Meal Preset", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            
            // Preset Grid
            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MealToggleButton(meals[0], selectedMeal == meals[0], Modifier.weight(1f)) { selectedMeal = meals[0] }
                MealToggleButton(meals[1], selectedMeal == meals[1], Modifier.weight(1f)) { selectedMeal = meals[1] }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MealToggleButton(meals[2], selectedMeal == meals[2], Modifier.weight(1f)) { selectedMeal = meals[2] }
                MealToggleButton(meals[3], selectedMeal == meals[3], Modifier.weight(1f)) { selectedMeal = meals[3] }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = SurfaceDark)
            Spacer(modifier = Modifier.height(24.dp))

            // Custom Saved Foods
            Text("Custom Saved Foods", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(allFoods) { food ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .clickable {
                                foodName = food.name
                                caloriesStr = food.calories.toString()
                                carbsStr = food.carbs.toString()
                                proteinStr = food.protein.toString()
                                fatStr = food.fat.toString()
                                isCustomExpanded = true
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(food.name, color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Delete", 
                                tint = RedClose, 
                                modifier = Modifier.size(16.dp).clickable { viewModel.deleteFood(food.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Food Expandable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isCustomExpanded) SurfaceDark else Color.Transparent)
                    .border(1.dp, SurfaceLightDark, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCustomExpanded = !isCustomExpanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Custom Food ", color = GreenAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    }
                    Icon(
                        if (isCustomExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = GreenAccent
                    )
                }

                if (isCustomExpanded) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Food Name", color = GreenAccent, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenAccent,
                                unfocusedBorderColor = SurfaceLightDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Macros per 100g", color = GreenAccent, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MacroField("Cals", caloriesStr, Modifier.weight(1f)) { caloriesStr = it }
                            MacroField("Carbs", carbsStr, Modifier.weight(1f)) { carbsStr = it }
                            MacroField("Prot", proteinStr, Modifier.weight(1f)) { proteinStr = it }
                            MacroField("Fat", fatStr, Modifier.weight(1f)) { fatStr = it }
                        }
                        
                        Spacer(Modifier.height(16.dp))

                        if (scanningState is ScanningState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GreenAccent)
                            }
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { cameraLauncher.launch() },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark, contentColor = TextGray)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Camera")
                            }
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightDark, contentColor = TextGray)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Gallery")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MacroField(label: String, value: String, modifier: Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenAccent,
            unfocusedBorderColor = SurfaceLightDark,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = DarkBg,
            unfocusedContainerColor = DarkBg
        )
    )
}

@Composable
fun MealToggleButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color.White else SurfaceDark)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Restaurant, 
                contentDescription = null, 
                tint = if (isSelected) Color.Black else TextGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
