package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_items")
data class DhikrItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val category: String, // "Daily", "After Salah", "Forgiveness", "Quranic", "Custom"
    val defaultTarget: Int = 33, // 33, 99, 100, etc.
    val virtues: String = "",
    val reference: String = "",
    val isCustom: Boolean = false,
    val orderIndex: Int = 0
)
