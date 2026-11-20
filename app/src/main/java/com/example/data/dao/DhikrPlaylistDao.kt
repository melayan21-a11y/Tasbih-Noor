package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DhikrPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrPlaylistDao {

    @Query("SELECT * FROM dhikr_playlists ORDER BY isBuiltIn DESC, orderIndex ASC, id ASC")
    fun getAllPlaylists(): Flow<List<DhikrPlaylist>>

    @Query("SELECT * FROM dhikr_playlists ORDER BY isBuiltIn DESC, orderIndex ASC, id ASC")
    suspend fun getAllPlaylistsList(): List<DhikrPlaylist>

    @Query("SELECT COUNT(*) FROM dhikr_playlists")
    suspend fun countPlaylists(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: DhikrPlaylist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playlists: List<DhikrPlaylist>)

    @Delete
    suspend fun deletePlaylist(playlist: DhikrPlaylist)
}
