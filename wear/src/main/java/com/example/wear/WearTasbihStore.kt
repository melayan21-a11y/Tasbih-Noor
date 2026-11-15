package com.example.wear

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val AmiriWearFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Bold)
)

data class WearDhikrPreset(
    val id: Long,
    val arabicText: String,
    val transliteration: String,
    val defaultTarget: Int
)

data class WearTasbihState(
    val dhikrId: Long = 1L,
    val arabicText: String = "سُبْحَانَ اللَّهِ",
    val transliteration: String = "SubhanAllah",
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val todayTotal: Int = 0,
    val streakDays: Int = 0,
    val isNightMode: Boolean = true,
    val salahModeEnabled: Boolean = false,
    val salahStageIndex: Int = 0,
    val lastSyncedAtMillis: Long = 0L
)

object WearTasbihStore {

    private const val PREFS_NAME = "tasbih_wear_prefs"
    private const val KEY_DHIKR_ID = "dhikr_id"
    private const val KEY_ARABIC = "arabic_text"
    private const val KEY_TRANSLIT = "transliteration"
    private const val KEY_COUNT = "current_count"
    private const val KEY_TARGET = "target_count"
    private const val KEY_TODAY = "today_total"
    private const val KEY_TODAY_DATE = "today_date"
    private const val KEY_STREAK = "streak_days"
    private const val KEY_NIGHT = "is_night_mode"
    private const val KEY_SALAH_MODE = "salah_mode"
    private const val KEY_SALAH_STAGE = "salah_stage"
    private const val KEY_LAST_SYNC = "last_sync_at"

    val defaultPresets = listOf(
        WearDhikrPreset(1L, "سُبْحَانَ اللَّهِ", "SubhanAllah", 33),
        WearDhikrPreset(2L, "الْحَمْدُ لِلَّهِ", "Alhamdulillah", 33),
        WearDhikrPreset(3L, "اللَّهُ أَكْبَرُ", "Allahu Akbar", 33),
        WearDhikrPreset(4L, "أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", 100),
        WearDhikrPreset(5L, "لَا إِلٰهَ إِلَّا اللَّهُ", "La ilaha illallah", 100),
        WearDhikrPreset(6L, "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Salawat", 33)
    )

    val salahStages = listOf(
        WearDhikrPreset(1L, "سُبْحَانَ اللَّهِ", "SubhanAllah", 33),
        WearDhikrPreset(2L, "الْحَمْدُ لِلَّهِ", "Alhamdulillah", 33),
        WearDhikrPreset(3L, "اللَّهُ أَكْبَرُ", "Allahu Akbar", 33),
        WearDhikrPreset(7L, "لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ...", "Tahlil Seal", 1)
    )

    private val _state = MutableStateFlow(WearTasbihState())
    val state: StateFlow<WearTasbihState> = _state.asStateFlow()

    @Volatile
    private var initialized = false

    private fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    @Synchronized
    fun ensureLoaded(context: Context): WearTasbihState {
        if (!initialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedDate = prefs.getString(KEY_TODAY_DATE, "")
            val isSameDay = savedDate == todayDateKey()
            val loaded = WearTasbihState(
                dhikrId = prefs.getLong(KEY_DHIKR_ID, 1L),
                arabicText = prefs.getString(KEY_ARABIC, "سُبْحَانَ اللَّهِ") ?: "سُبْحَانَ اللَّهِ",
                transliteration = prefs.getString(KEY_TRANSLIT, "SubhanAllah") ?: "SubhanAllah",
                currentCount = prefs.getInt(KEY_COUNT, 0),
                targetCount = prefs.getInt(KEY_TARGET, 33).coerceAtLeast(1),
                todayTotal = if (isSameDay) prefs.getInt(KEY_TODAY, 0) else 0,
                streakDays = prefs.getInt(KEY_STREAK, 0),
                isNightMode = prefs.getBoolean(KEY_NIGHT, true),
                salahModeEnabled = prefs.getBoolean(KEY_SALAH_MODE, false),
                salahStageIndex = prefs.getInt(KEY_SALAH_STAGE, 0).coerceIn(0, salahStages.lastIndex),
                lastSyncedAtMillis = prefs.getLong(KEY_LAST_SYNC, 0L)
            )
            _state.value = loaded
            initialized = true
        }
        return _state.value
    }

    @Synchronized
    private fun persistAndPublish(context: Context, updated: WearTasbihState) {
        _state.value = updated
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong(KEY_DHIKR_ID, updated.dhikrId)
            .putString(KEY_ARABIC, updated.arabicText)
            .putString(KEY_TRANSLIT, updated.transliteration)
            .putInt(KEY_COUNT, updated.currentCount)
            .putInt(KEY_TARGET, updated.targetCount)
            .putInt(KEY_TODAY, updated.todayTotal)
            .putString(KEY_TODAY_DATE, todayDateKey())
            .putInt(KEY_STREAK, updated.streakDays)
            .putBoolean(KEY_NIGHT, updated.isNightMode)
            .putBoolean(KEY_SALAH_MODE, updated.salahModeEnabled)
            .putInt(KEY_SALAH_STAGE, updated.salahStageIndex)
            .putLong(KEY_LAST_SYNC, updated.lastSyncedAtMillis)
            .apply()

        requestComplicationRefresh(context)
    }

    fun requestComplicationRefresh(context: Context) {
        try {
            ComplicationDataSourceUpdateRequester
                .create(context, ComponentName(context, TasbihComplicationService::class.java))
                .requestUpdateAll()
        } catch (_: Exception) {
        }
    }

    /**
     * Increments the bead counter on the watch and returns true if targetCount was just reached.
     */
    @Synchronized
    fun incrementBead(context: Context): Pair<WearTasbihState, Boolean> {
        val current = ensureLoaded(context)
        val nextCount = current.currentCount + 1
        val nextToday = current.todayTotal + 1
        val reachedTarget = nextCount >= current.targetCount

        val nextState = if (current.salahModeEnabled && reachedTarget) {
            val nextStageIdx = (current.salahStageIndex + 1) % salahStages.size
            val stage = salahStages[nextStageIdx]
            current.copy(
                dhikrId = stage.id,
                arabicText = stage.arabicText,
                transliteration = stage.transliteration,
                currentCount = 0,
                targetCount = stage.targetCountForSalah(),
                todayTotal = nextToday,
                salahStageIndex = nextStageIdx
            )
        } else {
            val wrappedCount = if (nextCount > current.targetCount) 1 else nextCount
            current.copy(
                currentCount = wrappedCount,
                todayTotal = nextToday
            )
        }

        persistAndPublish(context, nextState)
        return nextState to reachedTarget
    }

    private fun WearDhikrPreset.targetCountForSalah(): Int = defaultTarget

    @Synchronized
    fun undoBead(context: Context) {
        val current = ensureLoaded(context)
        if (current.currentCount <= 0) return
        persistAndPublish(
            context,
            current.copy(
                currentCount = current.currentCount - 1,
                todayTotal = (current.todayTotal - 1).coerceAtLeast(0)
            )
        )
    }

    @Synchronized
    fun resetCount(context: Context) {
        val current = ensureLoaded(context)
        persistAndPublish(context, current.copy(currentCount = 0))
    }

    @Synchronized
    fun selectPreset(context: Context, preset: WearDhikrPreset) {
        val current = ensureLoaded(context)
        persistAndPublish(
            context,
            current.copy(
                dhikrId = preset.id,
                arabicText = preset.arabicText,
                transliteration = preset.transliteration,
                currentCount = 0,
                targetCount = preset.defaultTarget,
                salahModeEnabled = false
            )
        )
    }

    @Synchronized
    fun toggleSalahMode(context: Context) {
        val current = ensureLoaded(context)
        val enable = !current.salahModeEnabled
        val next = if (enable) {
            val first = salahStages.first()
            current.copy(
                salahModeEnabled = true,
                salahStageIndex = 0,
                dhikrId = first.id,
                arabicText = first.arabicText,
                transliteration = first.transliteration,
                currentCount = 0,
                targetCount = first.defaultTarget
            )
        } else {
            current.copy(salahModeEnabled = false)
        }
        persistAndPublish(context, next)
    }

    @Synchronized
    fun applyPhoneSync(
        context: Context,
        dhikrId: Long,
        arabicText: String,
        transliteration: String,
        targetCount: Int,
        todayTotal: Int,
        streakDays: Int,
        isNightMode: Boolean
    ) {
        val current = ensureLoaded(context)
        val switchedDhikr = current.dhikrId != dhikrId
        val next = current.copy(
            dhikrId = dhikrId,
            arabicText = arabicText.ifBlank { current.arabicText },
            transliteration = transliteration.ifBlank { current.transliteration },
            currentCount = if (switchedDhikr) 0 else current.currentCount,
            targetCount = targetCount.coerceAtLeast(1),
            todayTotal = maxOf(todayTotal, if (switchedDhikr) todayTotal else current.todayTotal),
            streakDays = streakDays,
            isNightMode = isNightMode,
            lastSyncedAtMillis = System.currentTimeMillis()
        )
        persistAndPublish(context, next)
    }
}
