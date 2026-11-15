package com.example.wearsync

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class WearConnectionStatus(
    val isPlayServicesAvailable: Boolean = false,
    val connectedWatches: List<String> = emptyList(),
    val lastSyncTimeMillis: Long = 0L
)

class WearSyncManager(private val context: Context) {

    companion object {
        const val PATH_TASBIH_STATE = "/tasbih/state"
        const val PATH_WRIST_INCREMENT = "/tasbih/wrist_increment"
        const val PATH_REQUEST_SYNC = "/tasbih/request_sync"
    }

    private fun isGmsAvailable(): Boolean {
        return try {
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        } catch (_: Throwable) {
            false
        }
    }

    private suspend fun <T> Task<T>.awaitSafe(): T? = suspendCancellableCoroutine { cont ->
        try {
            addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result)
            }
            addOnFailureListener {
                if (cont.isActive) cont.resume(null)
            }
            addOnCanceledListener {
                if (cont.isActive) cont.resume(null)
            }
        } catch (_: Throwable) {
            if (cont.isActive) cont.resume(null)
        }
    }

    suspend fun getConnectionStatus(): WearConnectionStatus = withContext(Dispatchers.IO) {
        try {
            if (!isGmsAvailable()) {
                return@withContext WearConnectionStatus(isPlayServicesAvailable = false)
            }
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.awaitSafe().orEmpty()
            WearConnectionStatus(
                isPlayServicesAvailable = true,
                connectedWatches = nodes.mapNotNull { it.displayName }
            )
        } catch (t: Throwable) {
            Log.d("WearSyncManager", "Wearable API unavailable: ${t.message}")
            WearConnectionStatus(isPlayServicesAvailable = false)
        }
    }

    suspend fun pushStateToWatch(
        dhikrId: Long,
        arabicText: String,
        transliteration: String,
        targetCount: Int,
        todayTotal: Int,
        streakDays: Int,
        isNightMode: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isGmsAvailable()) return@withContext false

            val dataClient = Wearable.getDataClient(context)
            val request = PutDataMapRequest.create(PATH_TASBIH_STATE).apply {
                dataMap.putLong("dhikr_id", dhikrId)
                dataMap.putString("arabic_text", arabicText)
                dataMap.putString("transliteration", transliteration)
                dataMap.putInt("target_count", targetCount)
                dataMap.putInt("today_total", todayTotal)
                dataMap.putInt("streak_days", streakDays)
                dataMap.putBoolean("is_night_mode", isNightMode)
                dataMap.putLong("updated_at", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            dataClient.putDataItem(request).awaitSafe() != null
        } catch (t: Throwable) {
            Log.d("WearSyncManager", "Push state to watch skipped: ${t.message}")
            false
        }
    }
}
