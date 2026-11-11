package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.TasbihDatabase
import com.example.data.model.DhikrLog
import com.example.util.TasbihPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasbihAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = TasbihPreferences(context)

        when (intent.action) {
            ACTION_WIDGET_TAP_BEAD -> {
                val currentCount = prefs.widgetTodayCount + 1
                prefs.widgetTodayCount = currentCount

                // Haptic tap feedback
                triggerWidgetHaptic(context, 30)

                // Persist to Room database asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = TasbihDatabase.getDatabase(context, this)
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val todayStr = sdf.format(Date())
                        val selectedDhikr = db.dhikrDao().getDhikrById(prefs.selectedDhikrId)
                        val title = selectedDhikr?.transliteration ?: "SubhanAllah"
                        db.dhikrLogDao().insertLog(
                            DhikrLog(
                                dateString = todayStr,
                                dhikrId = prefs.selectedDhikrId,
                                count = 1,
                                dhikrTitle = title
                            )
                        )
                    } catch (_: Exception) {}
                }

                // Update UI immediately
                updateAllWidgets(context)
            }

            ACTION_WIDGET_NEXT_REMINDER -> {
                // Shuffle / cycle to the next inspiring Athkar / Dua / reminder
                prefs.widgetReminderIndex = prefs.widgetReminderIndex + 1
                triggerWidgetHaptic(context, 20)
                updateAllWidgets(context)
            }
        }
    }

    private fun triggerWidgetHaptic(context: Context, durationMs: Long) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    companion object {
        const val ACTION_WIDGET_TAP_BEAD = "com.example.tasbih.ACTION_WIDGET_TAP_BEAD"
        const val ACTION_WIDGET_NEXT_REMINDER = "com.example.tasbih.ACTION_WIDGET_NEXT_REMINDER"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager?, appWidgetId: Int) {
            try {
                if (appWidgetManager == null) return
                val prefs = TasbihPreferences(context)
                val views = RemoteViews(context.packageName, R.layout.widget_tasbih)

                // Streak Badge
                val streak = maxOf(1, prefs.widgetStreakDays)
                views.setTextViewText(R.id.widget_streak_text, "🔥 ${streak}d")

                // Inspiring Spiritual Athkar / Dua / Reminder
                val reminder = WidgetReminders.getReminder(prefs.widgetReminderIndex)
                views.setTextViewText(R.id.widget_reminder_tag, reminder.tag)
                views.setTextViewText(R.id.widget_reminder_arabic, reminder.arabic)
                views.setTextViewText(R.id.widget_reminder_english, reminder.translation)

                // Shuffle Next Reminder PendingIntent (on "↻ Next Dua" or clicking reminder card)
                val shuffleIntent = Intent(context, TasbihAppWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_NEXT_REMINDER
                }
                val shufflePendingIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    shuffleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_shuffle, shufflePendingIntent)
                views.setOnClickPendingIntent(R.id.widget_reminder_card, shufflePendingIntent)

                // Open App PendingIntent (on "Open Noor ➔" or tapping root)
                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openPendingIntent = PendingIntent.getActivity(
                    context,
                    1,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_open, openPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {}
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
                val widgetComponent = ComponentName(context, TasbihAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent) ?: return
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {}
        }
    }
}
