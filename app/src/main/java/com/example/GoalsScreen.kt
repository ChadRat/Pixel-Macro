package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: MainViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Goals") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Calorie & Macro Goals", style = MaterialTheme.typography.titleLarge)
            
            ListItem(
                headlineContent = { Text("Calories") },
                trailingContent = { Text(viewModel.caloriesGoal.toString(), style = MaterialTheme.typography.titleMedium) }
            )
            ListItem(
                headlineContent = { Text("Carbohydrates") },
                trailingContent = { Text("${viewModel.carbsGoal}g", style = MaterialTheme.typography.titleMedium) }
            )
            ListItem(
                headlineContent = { Text("Protein") },
                trailingContent = { Text("${viewModel.proteinGoal}g", style = MaterialTheme.typography.titleMedium) }
            )
            ListItem(
                headlineContent = { Text("Fat") },
                trailingContent = { Text("${viewModel.fatGoal}g", style = MaterialTheme.typography.titleMedium) }
            )
            
            Text("In this prototype, goals are fixed. In a full version, you would be able to edit these values.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
