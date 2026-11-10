package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_logs")
data class DhikrLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // Format: "yyyy-MM-dd" for calendar & consistency tracking
    val dhikrId: Long,
    val dhikrTitle: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)
