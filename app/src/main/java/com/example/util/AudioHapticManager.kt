package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundEffectType {
    WOOD_CLICK,
    CRYSTAL_CLICK,
    SOFT_CHIME,
    MUTE
}

enum class HapticIntensity {
    OFF,
    MILD,
    MEDIUM,
    STRONG
}

class AudioHapticManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isArabicSupported = false

    // Synthesized Sound Audio Tracks for zero-latency, realistic physical bead clicks
    private var woodSoundPcm: ByteArray? = null
    private var crystalSoundPcm: ByteArray? = null
    private var chimeSoundPcm: ByteArray? = null
    private val sampleRate = 44100

    init {
        initTts()
        generatePcmSounds()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                val arLocale = Locale("ar")
                val langResult = tts?.isLanguageAvailable(arLocale)
                if (langResult == TextToSpeech.LANG_AVAILABLE ||
                    langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    langResult == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
                ) {
                    tts?.language = arLocale
                    isArabicSupported = true
                } else {
                    tts?.language = Locale.ENGLISH
                    isArabicSupported = false
                }
                tts?.setSpeechRate(0.85f) // gentle, calm cadence
            }
        }
    }

    private fun generatePcmSounds() {
        try {
            woodSoundPcm = synthesizeBeadClick(frequency1 = 820.0, frequency2 = 1450.0, durationMs = 38, decayRate = 110.0)
            crystalSoundPcm = synthesizeBeadClick(frequency1 = 2100.0, frequency2 = 3300.0, durationMs = 45, decayRate = 75.0)
            chimeSoundPcm = synthesizeBeadClick(frequency1 = 1200.0, frequency2 = 2400.0, durationMs = 90, decayRate = 40.0)
        } catch (e: Exception) {
            Log.e("AudioHapticManager", "Error generating PCM sounds", e)
        }
    }

    private fun synthesizeBeadClick(
        frequency1: Double,
        frequency2: Double,
        durationMs: Int,
        decayRate: Double
    ): ByteArray {
        val totalSamples = (sampleRate * durationMs / 1000.0).toInt()
        val pcmData = ByteArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-decayRate * t)
            val sampleVal = (
                0.65 * sin(2.0 * PI * frequency1 * t) +
                0.35 * sin(2.0 * PI * frequency2 * t)
            ) * envelope

            val sampleShort = (sampleVal.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
            pcmData[i * 2] = (sampleShort.toInt() and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
        }
        return pcmData
    }

    fun playBeadSound(soundType: SoundEffectType) {
        if (soundType == SoundEffectType.MUTE) return
        val pcm = when (soundType) {
            SoundEffectType.WOOD_CLICK -> woodSoundPcm
            SoundEffectType.CRYSTAL_CLICK -> crystalSoundPcm
            SoundEffectType.SOFT_CHIME -> chimeSoundPcm
            SoundEffectType.MUTE -> null
        } ?: return

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(pcm.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            // release track shortly after playback completes
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }, 150)
        } catch (e: Exception) {
            Log.e("AudioHapticManager", "Failed to play sound", e)
        }
    }

    fun triggerBeadHaptic(intensity: HapticIntensity) {
        if (intensity == HapticIntensity.OFF || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val (durationMs, amplitude) = when (intensity) {
                    HapticIntensity.MILD -> 15L to 45
                    HapticIntensity.MEDIUM -> 22L to 95
                    HapticIntensity.STRONG -> 35L to 180
                    HapticIntensity.OFF -> 0L to 0
                }
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                val durationMs = when (intensity) {
                    HapticIntensity.MILD -> 12L
                    HapticIntensity.MEDIUM -> 20L
                    HapticIntensity.STRONG -> 35L
                    HapticIntensity.OFF -> 0L
                }
                vibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("AudioHapticManager", "Vibrate error", e)
        }
    }

    fun triggerTargetReachedHaptic() {
        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 45, 70, 60)
                val amplitudes = intArrayOf(0, 160, 0, 220)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 70, 70), -1)
            }
        } catch (_: Exception) {}
    }

    fun speakRecitation(arabicText: String, transliteration: String) {
        if (!isTtsReady || tts == null) return
        val textToSpeak = if (isArabicSupported && arabicText.isNotBlank()) {
            tts?.language = Locale("ar")
            arabicText
        } else {
            tts?.language = Locale.ENGLISH
            transliteration
        }
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "dhikr_recitation")
    }

    fun stopRecitation() {
        tts?.stop()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
