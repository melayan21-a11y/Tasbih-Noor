package com.example.wearsync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class PairedWatchNode(
    val id: String,
    val displayName: String,
    val isAppInstalled: Boolean
)

data class WearConnectionStatus(
    val isPlayServicesAvailable: Boolean = false,
    val connectedWatches: List<String> = emptyList(),
    val pairedNodes: List<PairedWatchNode> = emptyList(),
    val lastSyncTimeMillis: Long = 0L
) {
    val installedWatches: List<PairedWatchNode>
        get() = pairedNodes.filter { it.isAppInstalled }

    val missingAppWatches: List<PairedWatchNode>
        get() = pairedNodes.filter { !it.isAppInstalled }
}

enum class WatchInstallLaunchResult(val userMessage: String) {
    OPENED_ON_WATCH("Opened Play Store on your paired watch. Note: before publishing the Wear track on Google Play, sideload wear-debug.apk via Wireless ADB or Wear Installer."),
    NO_WATCH_CONNECTED("No paired Wear OS watch detected via Bluetooth. Pair your watch in the Galaxy Wearable / Pixel Watch / Wear OS phone app first."),
    PLAY_SERVICES_UNAVAILABLE("Wear OS Play Services is unavailable on this device. You can still build & sideload wear-debug.apk directly onto your watch."),
    FAILED("Could not reach the watch Play Store right now. Make sure Bluetooth & Wi-Fi are enabled on your watch, or sideload wear-debug.apk.")
}

class WearSyncManager(private val context: Context) {

    companion object {
        const val PATH_TASBIH_STATE = "/tasbih/state"
        const val PATH_WRIST_INCREMENT = "/tasbih/wrist_increment"
        const val PATH_REQUEST_SYNC = "/tasbih/request_sync"
        const val CAPABILITY_WEAR_APP = "tasbih_noor_wear_app"
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

            val installedNodeIds: Set<String> = try {
                val capInfo = Wearable.getCapabilityClient(context)
                    .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL)
                    .awaitSafe()
                capInfo?.nodes?.map { it.id }?.toSet().orEmpty()
            } catch (_: Throwable) {
                emptySet()
            }

            val paired = nodes.map { node ->
                PairedWatchNode(
                    id = node.id,
                    displayName = node.displayName,
                    isAppInstalled = node.id in installedNodeIds
                )
            }

            WearConnectionStatus(
                isPlayServicesAvailable = true,
                connectedWatches = paired.map { it.displayName },
                pairedNodes = paired
            )
        } catch (t: Throwable) {
            Log.d("WearSyncManager", "Wearable API unavailable: ${t.message}")
            WearConnectionStatus(isPlayServicesAvailable = false)
        }
    }

    suspend fun openPlayStoreOnWatch(): WatchInstallLaunchResult = withContext(Dispatchers.IO) {
        try {
            if (!isGmsAvailable()) {
                return@withContext WatchInstallLaunchResult.PLAY_SERVICES_UNAVAILABLE
            }
            val status = getConnectionStatus()
            val targetNodes = status.missingAppWatches.ifEmpty { status.pairedNodes }
            if (targetNodes.isEmpty()) {
                return@withContext WatchInstallLaunchResult.NO_WATCH_CONNECTED
            }

            val executor = Dispatchers.IO.asExecutor()
            val remoteActivityHelper = RemoteActivityHelper(context, executor)
            val playIntent = Intent(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setData(Uri.parse("market://details?id=${context.packageName}"))

            var anyOpened = false
            for (node in targetNodes) {
                val success = suspendCancellableCoroutine { cont ->
                    try {
                        val future = remoteActivityHelper.startRemoteActivity(playIntent, node.id)
                        future.addListener(
                            {
                                val ok = try {
                                    future.get()
                                    true
                                } catch (_: Throwable) {
                                    false
                                }
                                if (cont.isActive) cont.resume(ok)
                            },
                            executor
                        )
                    } catch (_: Throwable) {
                        if (cont.isActive) cont.resume(false)
                    }
                }
                if (success) anyOpened = true
            }

            if (anyOpened) WatchInstallLaunchResult.OPENED_ON_WATCH
            else WatchInstallLaunchResult.FAILED
        } catch (t: Throwable) {
            Log.d("WearSyncManager", "Failed to open Play Store on watch: ${t.message}")
            WatchInstallLaunchResult.FAILED
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
