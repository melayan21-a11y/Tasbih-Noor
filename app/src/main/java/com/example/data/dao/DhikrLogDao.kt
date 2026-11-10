package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DhikrLog
import kotlinx.coroutines.flow.Flow

data class DhikrDailyStat(
    val dateString: String,
    val totalCount: Int
)

data class DhikrTypeStat(
    val dhikrTitle: String,
    val totalCount: Int
)

@Dao
interface DhikrLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DhikrLog): Long

    @Query("SELECT * FROM dhikr_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DhikrLog>>

    @Query("SELECT dateString, SUM(count) as totalCount FROM dhikr_logs GROUP BY dateString ORDER BY dateString DESC")
    fun getDailyTotals(): Flow<List<DhikrDailyStat>>

    @Query("SELECT dhikrTitle, SUM(count) as totalCount FROM dhikr_logs GROUP BY dhikrTitle ORDER BY totalCount DESC")
    fun getStatsByDhikrType(): Flow<List<DhikrTypeStat>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM dhikr_logs WHERE dateString = :dateString")
    fun getTodayTotal(dateString: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(count), 0) FROM dhikr_logs")
    fun getLifetimeTotal(): Flow<Int>

    @Query("SELECT DISTINCT dateString FROM dhikr_logs ORDER BY dateString DESC")
    fun getActiveDates(): Flow<List<String>>

    @Query("DELETE FROM dhikr_logs")
    suspend fun clearAllLogs()
}
