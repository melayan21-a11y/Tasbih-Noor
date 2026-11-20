package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class DhikrPlaylistStep(
    val dhikrId: Long,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val targetCount: Int
)

@Entity(tableName = "dhikr_playlists")
data class DhikrPlaylist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val subtitle: String = "",
    val stepsJson: String,
    val isBuiltIn: Boolean = false,
    val orderIndex: Int = 0
) {
    fun steps(): List<DhikrPlaylistStep> = DhikrPlaylistSerializer.stepsFromJson(stepsJson)

    fun totalCount(): Int = steps().sumOf { it.targetCount.coerceAtLeast(1) }
}

object DhikrPlaylistSerializer {

    fun stepsToJson(steps: List<DhikrPlaylistStep>): String {
        val array = JSONArray()
        for (step in steps) {
            val obj = JSONObject()
            obj.put("dhikrId", step.dhikrId)
            obj.put("arabicText", step.arabicText)
            obj.put("transliteration", step.transliteration)
            obj.put("englishTranslation", step.englishTranslation)
            obj.put("targetCount", step.targetCount.coerceAtLeast(1))
            array.put(obj)
        }
        return array.toString()
    }

    fun stepsFromJson(json: String): List<DhikrPlaylistStep> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        DhikrPlaylistStep(
                            dhikrId = obj.optLong("dhikrId", 1L),
                            arabicText = obj.optString("arabicText", "سُبْحَانَ اللَّهِ"),
                            transliteration = obj.optString("transliteration", "SubhanAllah"),
                            englishTranslation = obj.optString("englishTranslation", "Glory be to Allah"),
                            targetCount = obj.optInt("targetCount", 33).coerceAtLeast(1)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
