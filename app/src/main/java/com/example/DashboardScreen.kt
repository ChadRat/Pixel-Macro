package com.example

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.data.DiaryEntry

// Colors based on the screenshot
val BgDark = Color(0xFF1E1F22)
val TrackGray = Color(0xFF343434)
val TealAccent = Color(0xFF009688)

val Card1Bg = Color(0xFF065147)
val Card1Circle = Color(0xFF03362F)
val Card1Icon = Color(0xFF5DF2D6)

val Card2Bg = Color(0xFF404040)
val Card2Circle = Color(0xFF262626)
val Card2Text = Color(0xFFE0E0E0)
val Card2Icon = Color(0xFF82B1FF)

val Card3Bg = Color(0xFF5E3197)
val Card3Circle = Color(0xFF3E1C6F)
val Card3Text = Color(0xFFFFFFFF)

val ButtonBlue = Color(0xFF005881)
val ButtonGray = Color(0xFF404040)
val DotActive = Color(0xFFBDBDBD)
val DotInactive = Color(0xFF616161)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, navController: NavController) {
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    
    val caloriesGoal by viewModel.caloriesGoal.collectAsState()
    val carbsGoal by viewModel.carbsGoal.collectAsState()
    val proteinGoal by viewModel.proteinGoal.collectAsState()
    val fatGoal by viewModel.fatGoal.collectAsState()

    var timePickerEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    
    if (timePickerEntry != null) {
        val entry = timePickerEntry!!
        val parts = entry.time.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(initialHour, initialMinute, is24Hour = false)

        AlertDialog(
            onDismissRequest = { timePickerEntry = null },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        val updatedEntry = entry.copy(time = newTime)
                        viewModel.updateDiaryEntry(updatedEntry)
                        timePickerEntry = null
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { timePickerEntry = null }) { Text("Cancel") }
            }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            viewModel.scanLabel(it)
            navController.navigate("add_food")
        }
    }
    
    val totalCalories = diaryEntries.sumOf { it.calories }
    val totalCarbs = diaryEntries.sumOf { it.carbs }
    val totalProtein = diaryEntries.sumOf { it.protein }
    val totalFat = diaryEntries.sumOf { it.fat }
    
    val caloriesRemaining = (caloriesGoal - totalCalories).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Calories Ring (Replaces Steps Ring)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = TrackGray,
                    strokeWidth = 32.dp
                )
                // Progress
                CircularProgressIndicator(
                    progress = { (totalCalories.toFloat() / caloriesGoal.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = TealAccent,
                    trackColor = Color.Transparent,
                    strokeWidth = 32.dp
                )
                
                // Overlay text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Calories", color = Color(0xFFB0B0B0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("$totalCalories", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("of $caloriesGoal", color = TealAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                // Bottom dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TealAccent)
                )
            }

            // Right Side: 3 Cards
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1 (Carbs instead of Steps)
                MacroPillCard(
                    title = "Carbs",
                    value = "${totalCarbs}g",
                    icon = Icons.Default.Restaurant,
                    bgColor = Card1Bg,
                    circleColor = Card1Circle,
                    textColor = Card1Icon
                )
                
                // Card 2 (Protein instead of Bpm)
                MacroPillCard(
                    title = "Protein",
                    value = "${totalProtein}g",
                    icon = Icons.Default.Favorite,
                    bgColor = Card2Bg,
                    circleColor = Card2Circle,
                    textColor = Card2Icon,
                    valueColor = Card2Text
                )

                // Card 3 (Fat instead of Sleep)
                MacroPillCard(
                    title = "Fat",
                    value = "${totalFat}g",
                    icon = Icons.Default.NightlightRound, // Using night for aesthetics to match purple theme
                    bgColor = Card3Bg,
                    circleColor = Card3Circle,
                    textColor = Card3Text
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pager Dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(height = 8.dp, width = 16.dp).clip(RoundedCornerShape(4.dp)).background(DotActive))
            Box(Modifier.size(8.dp).clip(CircleShape).background(DotInactive))
            Box(Modifier.size(8.dp).clip(CircleShape).background(DotInactive))
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Bottom Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Log Button
            Button(
                onClick = { navController.navigate("add_food") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            
            // Scan Button
            Button(
                onClick = { cameraLauncher.launch() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            
            // Edit Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ButtonGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Logged Foods",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (diaryEntries.isEmpty()) {
            Text("No foods logged today", color = TrackGray, fontSize = 16.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                diaryEntries.forEach { entry ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ButtonGray)
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.foodName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${entry.calories} kcal • C:${entry.carbs} P:${entry.protein} F:${entry.fat}", color = DotActive, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.time, 
                                        color = TealAccent, 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { timePickerEntry = entry }
                                            .padding(6.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFF18381),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { viewModel.removeDiaryEntry(entry.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MacroPillCard(
    title: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    circleColor: Color,
    textColor: Color,
    valueColor: Color = textColor
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(bgColor)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
