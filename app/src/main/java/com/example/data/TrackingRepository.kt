package com.example.data

import kotlinx.coroutines.flow.Flow

class TrackingRepository(private val dao: TrackingDao) {
    val allFoods: Flow<List<FoodItem>> = dao.getAllFoods()

    fun getDiaryEntries(date: String): Flow<List<DiaryEntry>> = dao.getDiaryEntries(date)

    suspend fun insertFood(food: FoodItem) = dao.insertFood(food)
    
    suspend fun deleteFood(id: Int) = dao.deleteFood(id)

    suspend fun insertDiaryEntry(entry: DiaryEntry) = dao.insertDiaryEntry(entry)
    
    suspend fun updateDiaryEntry(entry: DiaryEntry) = dao.updateDiaryEntry(entry)

    suspend fun deleteDiaryEntry(id: Int) = dao.deleteDiaryEntry(id)
}
