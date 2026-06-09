package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _aiProvider = MutableStateFlow(prefs.getString("ai_provider", "Google Gemini") ?: "Google Gemini")
    val aiProvider: StateFlow<String> = _aiProvider.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _aiModel = MutableStateFlow(prefs.getString("ai_model", "gemini-3.1-flash-lite") ?: "gemini-3.1-flash-lite")
    val aiModel: StateFlow<String> = _aiModel.asStateFlow()

    private val _systemPrompt = MutableStateFlow(prefs.getString("system_prompt", "You are a clinical, concise hydration coach...") ?: "You are a clinical, concise hydration coach...")
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

    private val _healthConnectEnabled = MutableStateFlow(prefs.getBoolean("health_connect", false))
    val healthConnectEnabled: StateFlow<Boolean> = _healthConnectEnabled.asStateFlow()

    private val _caloriesGoal = MutableStateFlow(prefs.getInt("calories_goal", 2000))
    val caloriesGoal: StateFlow<Int> = _caloriesGoal.asStateFlow()

    private val _carbsGoal = MutableStateFlow(prefs.getInt("carbs_goal", 195))
    val carbsGoal: StateFlow<Int> = _carbsGoal.asStateFlow()

    private val _proteinGoal = MutableStateFlow(prefs.getInt("protein_goal", 170))
    val proteinGoal: StateFlow<Int> = _proteinGoal.asStateFlow()

    private val _fatGoal = MutableStateFlow(prefs.getInt("fat_goal", 60))
    val fatGoal: StateFlow<Int> = _fatGoal.asStateFlow()

    fun setAiProvider(provider: String) {
        prefs.edit().putString("ai_provider", provider).apply()
        _aiProvider.value = provider
    }

    fun setGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
        _geminiApiKey.value = key
    }

    fun setAiModel(model: String) {
        prefs.edit().putString("ai_model", model).apply()
        _aiModel.value = model
    }

    fun setSystemPrompt(prompt: String) {
        prefs.edit().putString("system_prompt", prompt).apply()
        _systemPrompt.value = prompt
    }

    fun setHealthConnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("health_connect", enabled).apply()
        _healthConnectEnabled.value = enabled
    }

    fun setCaloriesGoal(goal: Int) {
        prefs.edit().putInt("calories_goal", goal).apply()
        _caloriesGoal.value = goal
    }

    fun setCarbsGoal(goal: Int) {
        prefs.edit().putInt("carbs_goal", goal).apply()
        _carbsGoal.value = goal
    }

    fun setProteinGoal(goal: Int) {
        prefs.edit().putInt("protein_goal", goal).apply()
        _proteinGoal.value = goal
    }

    fun setFatGoal(goal: Int) {
        prefs.edit().putInt("fat_goal", goal).apply()
        _fatGoal.value = goal
    }
}
