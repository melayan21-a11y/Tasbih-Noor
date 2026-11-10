package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

data class ReminderSetting(
    val id: Int,
    val title: String,
    val subtitle: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    var isEnabled: Boolean = false,
    var hour: Int = defaultHour,
    var minute: Int = defaultMinute
)

class DhikrReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleReminder(reminder: ReminderSetting) {
        if (alarmManager == null) return

        val intent = Intent(context, DhikrReminderReceiver::class.java).apply {
            putExtra(DhikrReminderReceiver.EXTRA_TITLE, reminder.title)
            putExtra(DhikrReminderReceiver.EXTRA_MESSAGE, reminder.subtitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If time is already past today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("DhikrReminderManager", "Failed to schedule reminder ${reminder.id}", e)
        }
    }

    fun cancelReminder(reminderId: Int) {
        if (alarmManager == null) return
        val intent = Intent(context, DhikrReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("DhikrReminderManager", "Failed to cancel reminder $reminderId", e)
        }
    }
}
