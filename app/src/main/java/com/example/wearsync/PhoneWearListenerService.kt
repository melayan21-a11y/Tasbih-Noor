package com.example.wearsync

import android.util.Log
import com.example.data.TasbihDatabase
import com.example.data.repository.TasbihRepository
import com.example.util.TasbihPreferences
import com.example.widget.TasbihAppWidgetProvider
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PhoneWearListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        when (messageEvent.path) {
            WearSyncManager.PATH_WRIST_INCREMENT -> {
                val payload = String(messageEvent.data, Charsets.UTF_8)
                val parts = payload.split("|")
                val delta = parts.getOrNull(0)?.toIntOrNull() ?: 1
                val dhikrId = parts.getOrNull(1)?.toLongOrNull() ?: 1L
                val translitHint = parts.getOrNull(2).orEmpty()

                serviceScope.launch {
                    try {
                        val db = TasbihDatabase.getDatabase(applicationContext, serviceScope)
                        val repo = TasbihRepository(db.dhikrDao(), db.dhikrLogDao())
                        repo.ensureDefaultAdhkarPopulated()
                        val all = db.dhikrDao().getAllDhikrList()
                        val matching = all.find { it.id == dhikrId }
                            ?: all.find { it.transliteration.equals(translitHint, ignoreCase = true) }
                            ?: all.firstOrNull()
                        if (matching != null) {
                            repo.recordDhikrCount(matching, delta.coerceIn(1, 500))
                        }
                        val prefs = TasbihPreferences(applicationContext)
                        val newToday = repo.getTodayTotal().first()
                        val streak = repo.calculateStreakFlow().first().currentStreak
                        prefs.widgetTodayCount = newToday
                        prefs.widgetStreakDays = streak
                        try {
                            TasbihAppWidgetProvider.updateAllWidgets(applicationContext)
                        } catch (_: Exception) {}

                        WearSyncManager(applicationContext).pushStateToWatch(
                            dhikrId = matching?.id ?: dhikrId,
                            arabicText = matching?.arabicText ?: "سُبْحَانَ اللَّهِ",
                            transliteration = matching?.transliteration ?: "SubhanAllah",
                            targetCount = prefs.savedTargetCount,
                            todayTotal = newToday,
                            streakDays = streak,
                            isNightMode = prefs.isNightMode
                        )
                    } catch (e: Exception) {
                        Log.e("PhoneWearListener", "Error recording wrist increment", e)
                    }
                }
            }

            WearSyncManager.PATH_REQUEST_SYNC -> {
                serviceScope.launch {
                    try {
                        val db = TasbihDatabase.getDatabase(applicationContext, serviceScope)
                        val repo = TasbihRepository(db.dhikrDao(), db.dhikrLogDao())
                        repo.ensureDefaultAdhkarPopulated()
                        val all = db.dhikrDao().getAllDhikrList()
                        val prefs = TasbihPreferences(applicationContext)
                        val active = all.find { it.id == prefs.selectedDhikrId } ?: all.firstOrNull()
                        val today = repo.getTodayTotal().first()
                        val streak = repo.calculateStreakFlow().first().currentStreak
                        WearSyncManager(applicationContext).pushStateToWatch(
                            dhikrId = active?.id ?: 1L,
                            arabicText = active?.arabicText ?: "سُبْحَانَ اللَّهِ",
                            transliteration = active?.transliteration ?: "SubhanAllah",
                            targetCount = prefs.savedTargetCount,
                            todayTotal = today,
                            streakDays = streak,
                            isNightMode = prefs.isNightMode
                        )
                    } catch (e: Exception) {
                        Log.e("PhoneWearListener", "Error handling sync request", e)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
