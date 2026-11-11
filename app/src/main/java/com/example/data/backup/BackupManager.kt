package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DhikrItem
import com.example.data.model.DhikrLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RestoreResult(
    val success: Boolean,
    val restoredDhikrs: List<DhikrItem>,
    val restoredLogs: List<DhikrLog>,
    val message: String
)

object BackupManager {

    private const val BACKUP_VERSION = 1

    /**
     * Serializes all custom dhikrs and logs into a formatted JSON string.
     */
    fun createBackupJson(
        customDhikrs: List<DhikrItem>,
        logs: List<DhikrLog>
    ): String {
        val root = JSONObject()
        root.put("app", "Tasbih Noor")
        root.put("version", BACKUP_VERSION)
        root.put("exportedAtTimestamp", System.currentTimeMillis())
        root.put("exportedAtFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Custom Dhikrs
        val dhikrsArray = JSONArray()
        for (item in customDhikrs) {
            val itemObj = JSONObject()
            itemObj.put("arabicText", item.arabicText)
            itemObj.put("transliteration", item.transliteration)
            itemObj.put("englishTranslation", item.englishTranslation)
            itemObj.put("category", item.category)
            itemObj.put("defaultTarget", item.defaultTarget)
            itemObj.put("virtues", item.virtues)
            itemObj.put("isCustom", item.isCustom)
            dhikrsArray.put(itemObj)
        }
        root.put("customDhikrs", dhikrsArray)

        // Dhikr Logs
        val logsArray = JSONArray()
        for (log in logs) {
            val logObj = JSONObject()
            logObj.put("dateString", log.dateString)
            logObj.put("dhikrId", log.dhikrId)
            logObj.put("dhikrTitle", log.dhikrTitle)
            logObj.put("count", log.count)
            logObj.put("timestamp", log.timestamp)
            logsArray.put(logObj)
        }
        root.put("dhikrLogs", logsArray)

        return root.toString(2)
    }

    /**
     * Serializes recitation logs into standard RFC-4180 CSV string.
     */
    fun createHistoryCsv(logs: List<DhikrLog>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Dhikr Title,Count,Timestamp,Formatted Time\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (log in logs) {
            val titleEscaped = "\"" + log.dhikrTitle.replace("\"", "\"\"") + "\""
            val formattedTime = sdf.format(Date(log.timestamp))
            sb.append("${log.id},${log.dateString},$titleEscaped,${log.count},${log.timestamp},$formattedTime\n")
        }
        return sb.toString()
    }

    /**
     * Parses a JSON backup string and validates its structure.
     */
    fun parseBackupJson(jsonString: String): RestoreResult {
        try {
            val root = JSONObject(jsonString)
            val dhikrsList = mutableListOf<DhikrItem>()
            val logsList = mutableListOf<DhikrLog>()

            if (root.has("customDhikrs")) {
                val dhikrsArray = root.getJSONArray("customDhikrs")
                for (i in 0 until dhikrsArray.length()) {
                    val obj = dhikrsArray.getJSONObject(i)
                    dhikrsList.add(
                        DhikrItem(
                            arabicText = obj.optString("arabicText", ""),
                            transliteration = obj.optString("transliteration", "Custom Dhikr"),
                            englishTranslation = obj.optString("englishTranslation", ""),
                            category = obj.optString("category", "Custom"),
                            defaultTarget = obj.optInt("defaultTarget", 33),
                            virtues = obj.optString("virtues", ""),
                            isCustom = true
                        )
                    )
                }
            }

            if (root.has("dhikrLogs")) {
                val logsArray = root.getJSONArray("dhikrLogs")
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    logsList.add(
                        DhikrLog(
                            dateString = obj.optString("dateString", "2026-01-01"),
                            dhikrId = obj.optLong("dhikrId", 1L),
                            dhikrTitle = obj.optString("dhikrTitle", "Dhikr"),
                            count = obj.optInt("count", 1),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            return RestoreResult(
                success = true,
                restoredDhikrs = dhikrsList,
                restoredLogs = logsList,
                message = "Backup loaded successfully: ${dhikrsList.size} custom prayers and ${logsList.size} logs."
            )
        } catch (e: Exception) {
            return RestoreResult(
                success = false,
                restoredDhikrs = emptyList(),
                restoredLogs = emptyList(),
                message = "Invalid backup format: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Writes content to a user-chosen Document URI via ContentResolver
     */
    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, Charsets.UTF_8).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reads text content from a chosen Document URI via ContentResolver
     */
    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Shares a backup file directly using the system Sharesheet
     */
    fun shareContentAsFile(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String
    ) {
        try {
            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()
            val file = File(backupDir, fileName)
            file.writeText(content, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Export $fileName")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}
