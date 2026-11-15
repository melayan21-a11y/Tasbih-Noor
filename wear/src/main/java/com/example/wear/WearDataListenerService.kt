package com.example.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == WearDataSync.PATH_TASBIH_STATE
            ) {
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                WearTasbihStore.applyPhoneSync(
                    context = applicationContext,
                    dhikrId = map.getLong("dhikr_id", 1L),
                    arabicText = map.getString("arabic_text", "سُبْحَانَ اللَّهِ"),
                    transliteration = map.getString("transliteration", "SubhanAllah"),
                    targetCount = map.getInt("target_count", 33),
                    todayTotal = map.getInt("today_total", 0),
                    streakDays = map.getInt("streak_days", 0),
                    isNightMode = map.getBoolean("is_night_mode", true)
                )
            }
        }
    }
}
