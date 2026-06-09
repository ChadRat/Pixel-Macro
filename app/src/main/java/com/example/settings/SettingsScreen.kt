package com.example.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsRepository: SettingsRepository) {
    val aiProvider by settingsRepository.aiProvider.collectAsState()
    val geminiApiKey by settingsRepository.geminiApiKey.collectAsState()
    val aiModel by settingsRepository.aiModel.collectAsState()
    val systemPrompt by settingsRepository.systemPrompt.collectAsState()
    val healthConnectEnabled by settingsRepository.healthConnectEnabled.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(geminiApiKey) }

    var showPromptDialog by remember { mutableStateOf(false) }
    var promptInput by remember { mutableStateOf(systemPrompt) }

    val caloriesGoal by settingsRepository.caloriesGoal.collectAsState()
    val carbsGoal by settingsRepository.carbsGoal.collectAsState()
    val proteinGoal by settingsRepository.proteinGoal.collectAsState()
    val fatGoal by settingsRepository.fatGoal.collectAsState()

    var showGoalsDialog by remember { mutableStateOf(false) }
    var caloriesInput by remember { mutableStateOf(caloriesGoal.toString()) }
    var carbsInput by remember { mutableStateOf(carbsGoal.toString()) }
    var proteinInput by remember { mutableStateOf(proteinGoal.toString()) }
    var fatInput by remember { mutableStateOf(fatGoal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AI Integrations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = aiProvider,
                    onValueChange = { settingsRepository.setAiProvider(it) },
                    label = { Text("AI Provider") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = aiModel,
                    onValueChange = { settingsRepository.setAiModel(it) },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("API Key", style = MaterialTheme.typography.bodyLarge)
                        Text(if (geminiApiKey.isBlank()) "Default (Built-in)" else "Custom Key Set", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { 
                        apiKeyInput = geminiApiKey
                        showApiKeyDialog = true 
                    }) {
                        Text("Edit")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Prompt", style = MaterialTheme.typography.bodyLarge)
                        Text("Custom instructions for the AI", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { 
                        promptInput = systemPrompt
                        showPromptDialog = true 
                    }) {
                        Text("Edit")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nutrition Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Goals", style = MaterialTheme.typography.bodyLarge)
                        Text("$caloriesGoal kcal • ${carbsGoal}g C • ${proteinGoal}g P • ${fatGoal}g F", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { 
                        caloriesInput = caloriesGoal.toString()
                        carbsInput = carbsGoal.toString()
                        proteinInput = proteinGoal.toString()
                        fatInput = fatGoal.toString()
                        showGoalsDialog = true 
                    }) {
                        Text("Edit")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Integrations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Health Connect", style = MaterialTheme.typography.bodyLarge)
                        Text("Sync with Google Health Connect", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = healthConnectEnabled,
                        onCheckedChange = { settingsRepository.setHealthConnectEnabled(it) }
                    )
                }
            }
        }
    }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Edit API Key") },
            text = {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Google Gemini API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsRepository.setGeminiApiKey(apiKeyInput)
                    showApiKeyDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPromptDialog) {
        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            title = { Text("Edit System Prompt") },
            text = {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    label = { Text("System Prompt") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsRepository.setSystemPrompt(promptInput)
                    showPromptDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromptDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGoalsDialog) {
        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            title = { Text("Edit Nutrition Goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caloriesInput,
                        onValueChange = { caloriesInput = it },
                        label = { Text("Calories (kcal)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = carbsInput,
                        onValueChange = { carbsInput = it },
                        label = { Text("Carbohydrates (g)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = proteinInput,
                        onValueChange = { proteinInput = it },
                        label = { Text("Protein (g)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fatInput,
                        onValueChange = { fatInput = it },
                        label = { Text("Fat (g)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    caloriesInput.toIntOrNull()?.let { settingsRepository.setCaloriesGoal(it) }
                    carbsInput.toIntOrNull()?.let { settingsRepository.setCarbsGoal(it) }
                    proteinInput.toIntOrNull()?.let { settingsRepository.setProteinGoal(it) }
                    fatInput.toIntOrNull()?.let { settingsRepository.setFatGoal(it) }
                    showGoalsDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
