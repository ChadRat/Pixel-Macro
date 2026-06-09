package com.example

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DiaryEntry
import com.example.data.FoodItem
import com.example.data.TrackingRepository
import com.example.gemini.Content
import com.example.gemini.GenerateContentRequest
import com.example.gemini.InlineData
import com.example.gemini.Part
import com.example.gemini.RetrofitClient
import com.example.settings.SettingsRepository
import com.example.health.HealthConnectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val repository: TrackingRepository,
    val settingsRepository: SettingsRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    
    val caloriesGoal = settingsRepository.caloriesGoal
    val carbsGoal = settingsRepository.carbsGoal
    val proteinGoal = settingsRepository.proteinGoal
    val fatGoal = settingsRepository.fatGoal

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDateStr = MutableStateFlow(dateFormatter.format(Date()))

    val allFoods: StateFlow<List<FoodItem>> = repository.allFoods.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val diaryEntries: StateFlow<List<DiaryEntry>> = currentDateStr.flatMapLatest { date ->
        repository.getDiaryEntries(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val scanningState = MutableStateFlow<ScanningState>(ScanningState.Idle)

    fun addFood(food: FoodItem) {
        viewModelScope.launch {
            repository.insertFood(food)
        }
    }
    
    fun deleteFood(id: Int) {
        viewModelScope.launch { repository.deleteFood(id) }
    }

    fun addDiaryEntry(mealType: String, time: String, food: FoodItem) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                date = currentDateStr.value,
                time = time,
                mealType = mealType,
                foodName = food.name,
                calories = food.calories,
                carbs = food.carbs,
                protein = food.protein,
                fat = food.fat
            )
            repository.insertDiaryEntry(entry)

            if (settingsRepository.healthConnectEnabled.value && healthConnectManager.isAvailable.value) {
                // simple 1-minute window
                val now = Instant.now()
                val endTime = now.plusSeconds(60)
                healthConnectManager.writeNutrition(
                    energyKcal = food.calories.toDouble(),
                    carbsGrams = food.carbs.toDouble(),
                    proteinGrams = food.protein.toDouble(),
                    fatGrams = food.fat.toDouble(),
                    name = food.name,
                    startTime = now,
                    endTime = endTime
                )
            }
        }
    }
    
    fun removeDiaryEntry(id: Int) {
        viewModelScope.launch { repository.deleteDiaryEntry(id) }
    }

    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch { repository.updateDiaryEntry(entry) }
    }

    fun scanLabel(bitmap: Bitmap) {
        scanningState.value = ScanningState.Loading
        viewModelScope.launch {
            try {
                val configuredKey = settingsRepository.geminiApiKey.value
                val apiKey = if (configuredKey.isNotBlank()) configuredKey else BuildConfig.GEMINI_API_KEY
                val model = settingsRepository.aiModel.value
                val systemPrompt = settingsRepository.systemPrompt.value
                
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                
                val promptText = """
                    Analyze this nutrition label, product barcode, or product packaging and extract the nutrition information. If it's a barcode, identify the product and provide its typical nutrition.
                    CRITICAL: Calculate and provide the nutrition information PER 100 GRAMS.
                    Return ONLY a JSON object with the following structure exactly (no markdown formatting, no comments, raw JSON). Ensure all values are integers, name is string.
                    {
                      "name": "Product Name",
                      "calories": 100,
                      "carbs": 20,
                      "protein": 5,
                      "fat": 2,
                      "servingSize": "100g"
                    }
                """.trimIndent()
                
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = promptText),
                                Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                            )
                        )
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(model, apiKey, request)
                }

                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (textResponse != null) {
                    // Extract json if the response has markdown code blocks
                    val cleanedJson = textResponse.substringAfter("```json").substringBeforeLast("```").trim().ifEmpty { textResponse }
                    try {
                        val jsonObject = JSONObject(cleanedJson)
                        val name = jsonObject.optString("name", "Unknown Food")
                        val calories = jsonObject.optInt("calories", 0)
                        val carbs = jsonObject.optInt("carbs", 0)
                        val protein = jsonObject.optInt("protein", 0)
                        val fat = jsonObject.optInt("fat", 0)
                        val servingSize = jsonObject.optString("servingSize", "1 serving")
                        
                        val newFood = FoodItem(
                            name = name,
                            calories = calories,
                            carbs = carbs,
                            protein = protein,
                            fat = fat,
                            servingSize = servingSize
                        )
                        scanningState.value = ScanningState.Success(newFood)
                    } catch (e: Exception) {
                        scanningState.value = ScanningState.Error("Failed to parse AI response.")
                        Log.e("MainViewModel", "JSON parsing error", e)
                    }
                } else {
                    scanningState.value = ScanningState.Error("No response from AI.")
                }
            } catch (e: Exception) {
                scanningState.value = ScanningState.Error(e.message ?: "Unknown API error")
                Log.e("MainViewModel", "API error", e)
            }
        }
    }
    
    fun clearScanningState() {
        scanningState.value = ScanningState.Idle
    }
}

sealed class ScanningState {
    object Idle : ScanningState()
    object Loading : ScanningState()
    data class Success(val foodItem: FoodItem) : ScanningState()
    data class Error(val message: String) : ScanningState()
}

class MainViewModelFactory(
    private val repository: TrackingRepository,
    private val settingsRepository: SettingsRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, settingsRepository, healthConnectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
