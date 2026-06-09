package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TrackingRepository
import com.example.settings.SettingsRepository
import com.example.health.HealthConnectManager

class MyApplication : Application() {
    val database by lazy { 
        Room.databaseBuilder(this, AppDatabase::class.java, "macro_database")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    val repository by lazy { TrackingRepository(database.trackingDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
    val healthConnectManager by lazy { HealthConnectManager(this) }
}
