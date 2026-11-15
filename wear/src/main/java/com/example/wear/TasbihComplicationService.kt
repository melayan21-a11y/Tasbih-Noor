package com.example.wear

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

class TasbihComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return buildComplicationData(
            type = type,
            todayTotal = 99,
            currentCount = 21,
            targetCount = 33,
            transliteration = "SubhanAllah",
            tapAction = null
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val state = WearTasbihStore.ensureLoaded(applicationContext)
        val intent = Intent(applicationContext, WearMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapIntent = PendingIntent.getActivity(
            applicationContext,
            request.complicationInstanceId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return buildComplicationData(
            type = request.complicationType,
            todayTotal = state.todayTotal,
            currentCount = state.currentCount,
            targetCount = state.targetCount,
            transliteration = state.transliteration,
            tapAction = tapIntent
        )
    }

    private fun buildComplicationData(
        type: ComplicationType,
        todayTotal: Int,
        currentCount: Int,
        targetCount: Int,
        transliteration: String,
        tapAction: PendingIntent?
    ): ComplicationData? {
        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(applicationContext, R.drawable.ic_complication_tasbih)
        ).build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("$todayTotal").build(),
                    contentDescription = PlainComplicationText.Builder("Today's Dhikr total: $todayTotal").build()
                )
                    .setTitle(PlainComplicationText.Builder("Dhikr").build())
                    .setMonochromaticImage(icon)
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.RANGED_VALUE -> {
                val safeMax = targetCount.coerceAtLeast(1).toFloat()
                val safeValue = currentCount.coerceIn(0, targetCount.coerceAtLeast(1)).toFloat()
                val shortName = transliteration.take(10)
                RangedValueComplicationData.Builder(
                    value = safeValue,
                    min = 0f,
                    max = safeMax,
                    contentDescription = PlainComplicationText.Builder(
                        "$transliteration: $currentCount of $targetCount. Today total: $todayTotal"
                    ).build()
                )
                    .setText(PlainComplicationText.Builder("$currentCount/$targetCount").build())
                    .setTitle(PlainComplicationText.Builder(shortName).build())
                    .setMonochromaticImage(icon)
                    .setTapAction(tapAction)
                    .build()
            }

            else -> null
        }
    }
}
