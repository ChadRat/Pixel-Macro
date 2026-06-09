package com.example.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneOffset

class HealthConnectManager(private val context: Context) {
    private val client: HealthConnectClient? by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    private val _isAvailable = MutableStateFlow(client != null)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    suspend fun writeNutrition(
        energyKcal: Double,
        carbsGrams: Double,
        proteinGrams: Double,
        fatGrams: Double,
        name: String,
        startTime: Instant,
        endTime: Instant
    ): Result<Unit> {
        if (client == null) return Result.failure(Exception("Health Connect not available"))

        return try {
            val record = NutritionRecord(
                energy = Energy.kilocalories(energyKcal),
                totalCarbohydrate = Mass.grams(carbsGrams),
                protein = Mass.grams(proteinGrams),
                totalFat = Mass.grams(fatGrams),
                startTime = startTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime = endTime,
                endZoneOffset = ZoneOffset.UTC,
                name = name
            )
            client?.insertRecords(listOf(record))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
