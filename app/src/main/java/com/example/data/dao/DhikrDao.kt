package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DhikrItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikr_items GROUP BY transliteration ORDER BY isCustom ASC, orderIndex ASC, id ASC")
    fun getAllDhikrItems(): Flow<List<DhikrItem>>

    @Query("SELECT * FROM dhikr_items WHERE id = :id LIMIT 1")
    suspend fun getDhikrById(id: Long): DhikrItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikr(item: DhikrItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DhikrItem>)

    @Update
    suspend fun updateDhikr(item: DhikrItem)

    @Delete
    suspend fun deleteDhikr(item: DhikrItem)

    @Query("SELECT COUNT(*) FROM dhikr_items")
    suspend fun countDhikrItems(): Int

    @Query("DELETE FROM dhikr_items WHERE id NOT IN (SELECT MIN(id) FROM dhikr_items GROUP BY transliteration)")
    suspend fun removeDuplicates()
}
