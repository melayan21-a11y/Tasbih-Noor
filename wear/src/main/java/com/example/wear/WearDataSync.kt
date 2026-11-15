package com.example.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object WearDataSync {

    const val PATH_TASBIH_STATE = "/tasbih/state"
    const val PATH_WRIST_INCREMENT = "/tasbih/wrist_increment"
    const val PATH_REQUEST_SYNC = "/tasbih/request_sync"

    private suspend fun <T> Task<T>.awaitSafe(): T? = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener {
            if (cont.isActive) cont.resume(null)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.resume(null)
        }
    }

    suspend fun getConnectedPhoneCount(context: Context): Int = withContext(Dispatchers.IO) {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val ok = try {
                GoogleApiAvailability.getInstance().checkApiAvailability(nodeClient).awaitSafe() != null
            } catch (_: Exception) {
                false
            }
            if (!ok) return@withContext 0
            nodeClient.connectedNodes.awaitSafe()?.size ?: 0
        } catch (_: Exception) {
            0
        }
    }

    suspend fun sendIncrementToPhone(
        context: Context,
        dhikrId: Long,
        transliteration: String,
        delta: Int = 1
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val ok = try {
                GoogleApiAvailability.getInstance().checkApiAvailability(nodeClient).awaitSafe() != null
            } catch (_: Exception) {
                false
            }
            if (!ok) return@withContext false

            val nodes = nodeClient.connectedNodes.awaitSafe().orEmpty()
            if (nodes.isEmpty()) return@withContext false

            val messageClient = Wearable.getMessageClient(context)
            val payload = "$delta|$dhikrId|$transliteration".toByteArray(Charsets.UTF_8)
            var sentAny = false
            nodes.forEach { node ->
                val result = messageClient.sendMessage(node.id, PATH_WRIST_INCREMENT, payload).awaitSafe()
                if (result != null) sentAny = true
            }
            sentAny
        } catch (e: Exception) {
            Log.d("WearDataSync", "Wrist increment sync skipped: ${e.message}")
            false
        }
    }

    suspend fun requestPhoneSync(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val ok = try {
                GoogleApiAvailability.getInstance().checkApiAvailability(nodeClient).awaitSafe() != null
            } catch (_: Exception) {
                false
            }
            if (!ok) return@withContext false

            val nodes = nodeClient.connectedNodes.awaitSafe().orEmpty()
            if (nodes.isEmpty()) return@withContext false

            val messageClient = Wearable.getMessageClient(context)
            var sentAny = false
            nodes.forEach { node ->
                val result = messageClient.sendMessage(node.id, PATH_REQUEST_SYNC, ByteArray(0)).awaitSafe()
                if (result != null) sentAny = true
            }
            sentAny
        } catch (e: Exception) {
            Log.d("WearDataSync", "Request sync skipped: ${e.message}")
            false
        }
    }
}
