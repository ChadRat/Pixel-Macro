package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DiaryScreen(viewModel: MainViewModel) {
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val currentDate by viewModel.currentDateStr.collectAsState()

    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snacks")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Implement date minus one day */ }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
            }
            Text("Today", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { /* Implement date plus one day */ }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
            }
        }
        
        HorizontalDivider()
        
        meals.forEach { mealType ->
            val mealEntries = diaryEntries.filter { it.mealType == mealType }
            val mealCals = mealEntries.sumOf { it.calories }
            
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(mealType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$mealCals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                mealEntries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.time, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 8.dp), color = MaterialTheme.colorScheme.primary)
                                Text(entry.foodName, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text("C: ${entry.carbs}g | F: ${entry.fat}g | P: ${entry.protein}g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${entry.calories}", style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { viewModel.removeDiaryEntry(entry.id) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                }
                
                TextButton(
                    onClick = { /* Handle navigate to add food for specific meal */ },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("ADD FOOD")
                }
                HorizontalDivider()
            }
        }
    }
}
