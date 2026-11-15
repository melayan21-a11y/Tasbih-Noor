package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffectType {
    WOOD_CLICK,
    CRYSTAL_CLICK,
    SOFT_CHIME,
    PEARL_TAP,
    MUTE
}

enum class HapticIntensity {
    OFF,
    MILD,
    MEDIUM,
    STRONG
}

enum class SpeechCadence(val label: String, val rate: Float) {
    REFLECTIVE("Reflective", 0.88f),
    NATURAL("Natural", 0.98f),
    BRISK("Brisk", 1.08f)
}

enum class VoiceQualityTier(val label: String) {
    HD_NEURAL("HD Natural Voice"),
    STANDARD("Standard System Voice"),
    SYNTHESIZER_FALLBACK("Legacy Offline Synth")
}

data class VoiceOption(
    val name: String,
    val displayLabel: String,
    val languageTag: String,
    val quality: Int,
    val isNetworkRequired: Boolean,
    val isArabic: Boolean
)

data class VoiceEngineState(
    val isReady: Boolean = false,
    val isArabicSupported: Boolean = false,
    val activeVoiceLabel: String = "Initializing...",
    val qualityTier: VoiceQualityTier = VoiceQualityTier.STANDARD,
    val selectedVoiceName: String? = null,
    val availableVoices: List<VoiceOption> = emptyList()
)

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

    private var preferredVoiceName: String? = null
    private var speechCadence: SpeechCadence = SpeechCadence.NATURAL
    private var preferArabicVoice: Boolean = true

    private val _voiceState = MutableStateFlow(VoiceEngineState())
    val voiceState: StateFlow<VoiceEngineState> = _voiceState.asStateFlow()

    // Multiple subtle organic variations per material so repeating 33/99 clicks stays alive & non-robotic
    private var woodVariations: List<ByteArray> = emptyList()
    private var crystalVariations: List<ByteArray> = emptyList()
    private var chimeVariations: List<ByteArray> = emptyList()
    private var pearlVariations: List<ByteArray> = emptyList()
    private var variationCursor = 0
    private val sampleRate = 44100

    init {
        initTts()
        generatePcmSounds()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                try {
                    if (status == TextToSpeech.SUCCESS) {
                        isTtsReady = true
                        refreshAndApplyVoice()
                    } else {
                        _voiceState.value = VoiceEngineState(
                            isReady = false,
                            activeVoiceLabel = "Speech Engine Unavailable",
                            qualityTier = VoiceQualityTier.SYNTHESIZER_FALLBACK
                        )
                    }
                } catch (t: Throwable) {
                    Log.e("AudioHapticManager", "TTS onInit error", t)
                    _voiceState.value = VoiceEngineState(
                        isReady = false,
                        activeVoiceLabel = "System Voice Fallback",
                        qualityTier = VoiceQualityTier.SYNTHESIZER_FALLBACK
                    )
                }
            }
        } catch (t: Throwable) {
            Log.e("AudioHapticManager", "TTS init failed", t)
        }
    }

    fun applyVoicePreferences(
        selectedVoiceName: String?,
        cadence: SpeechCadence,
        preferArabic: Boolean
    ) {
        this.preferredVoiceName = selectedVoiceName
        this.speechCadence = cadence
        this.preferArabicVoice = preferArabic
        if (isTtsReady) {
            refreshAndApplyVoice()
        }
    }

    fun refreshAndApplyVoice() {
        val engine = tts ?: return
        if (!isTtsReady) return

        try {
            val arLocale = Locale.forLanguageTag("ar-SA")
            val genericArLocale = Locale.forLanguageTag("ar")
            val arAvail = try {
                maxOf(
                    engine.isLanguageAvailable(arLocale),
                    engine.isLanguageAvailable(genericArLocale)
                )
            } catch (_: Throwable) {
                TextToSpeech.LANG_MISSING_DATA
            }
            isArabicSupported = arAvail >= TextToSpeech.LANG_AVAILABLE

            val allVoices = try {
                engine.voices?.filterNotNull().orEmpty()
            } catch (_: Throwable) {
                emptyList()
            }

            // Filter & score Arabic + high-quality English voices safely (voice.locale can be null on some OEM engines)
            val curatedOptions = allVoices
                .filter { v ->
                    val lang = v.locale?.language?.lowercase(Locale.ROOT).orEmpty()
                    lang == "ar" || lang == "en"
                }
                .sortedWith(
                    compareByDescending<Voice> { it.locale?.language.equals("ar", ignoreCase = true) }
                        .thenByDescending { scoreVoice(it) }
                )
                .distinctBy { it.name.orEmpty() }
                .take(12)
                .mapNotNull { v ->
                    val voiceName = v.name ?: return@mapNotNull null
                    val loc = v.locale
                    VoiceOption(
                        name = voiceName,
                        displayLabel = formatVoiceLabel(v),
                        languageTag = loc?.toLanguageTag().orEmpty(),
                        quality = v.quality,
                        isNetworkRequired = v.isNetworkConnectionRequired,
                        isArabic = loc?.language.equals("ar", ignoreCase = true)
                    )
                }

            val targetIsArabic = preferArabicVoice && isArabicSupported
            val chosenVoice: Voice? = if (preferredVoiceName != null) {
                allVoices.find { it.name == preferredVoiceName }
                    ?: pickBestVoice(allVoices, preferArabic = targetIsArabic)
            } else {
                pickBestVoice(allVoices, preferArabic = targetIsArabic)
            }

            if (chosenVoice != null) {
                try {
                    engine.voice = chosenVoice
                    chosenVoice.locale?.let { engine.language = it }
                } catch (_: Throwable) {
                    try {
                        engine.language = if (targetIsArabic) genericArLocale else Locale.ENGLISH
                    } catch (_: Throwable) {}
                }
            } else {
                try {
                    engine.language = if (targetIsArabic) genericArLocale else Locale.ENGLISH
                } catch (_: Throwable) {}
            }

            try {
                engine.setSpeechRate(speechCadence.rate)
                engine.setPitch(1.0f)
            } catch (_: Throwable) {}

            val chosenName = chosenVoice?.name.orEmpty()
            val tier = when {
                chosenVoice == null -> VoiceQualityTier.SYNTHESIZER_FALLBACK
                chosenVoice.quality >= Voice.QUALITY_HIGH ||
                    chosenName.contains("neural", ignoreCase = true) ||
                    chosenName.contains("network", ignoreCase = true) ||
                    chosenName.contains("wavenet", ignoreCase = true) -> VoiceQualityTier.HD_NEURAL
                chosenVoice.quality >= Voice.QUALITY_NORMAL -> VoiceQualityTier.STANDARD
                else -> VoiceQualityTier.SYNTHESIZER_FALLBACK
            }

            val activeLabel = chosenVoice?.let { formatVoiceLabel(it) }
                ?: if (targetIsArabic) "Arabic System Voice" else "English Transliteration"

            _voiceState.value = VoiceEngineState(
                isReady = isTtsReady,
                isArabicSupported = isArabicSupported,
                activeVoiceLabel = activeLabel,
                qualityTier = tier,
                selectedVoiceName = preferredVoiceName,
                availableVoices = curatedOptions
            )
        } catch (t: Throwable) {
            Log.e("AudioHapticManager", "Error refreshing TTS voices", t)
            _voiceState.value = VoiceEngineState(
                isReady = isTtsReady,
                isArabicSupported = isArabicSupported,
                activeVoiceLabel = "System Voice",
                qualityTier = VoiceQualityTier.STANDARD
            )
        }
    }

    private fun pickBestVoice(voices: List<Voice>, preferArabic: Boolean): Voice? {
        val candidates = if (preferArabic) {
            voices.filter { it.locale?.language.equals("ar", ignoreCase = true) }
                .ifEmpty { voices.filter { it.locale?.language.equals("en", ignoreCase = true) } }
        } else {
            voices.filter { it.locale?.language.equals("en", ignoreCase = true) }
        }
        return candidates.maxByOrNull { scoreVoice(it) }
    }

    private fun scoreVoice(voice: Voice): Int {
        var score = voice.quality * 10
        val lowerName = (voice.name ?: "").lowercase(Locale.ROOT)
        val country = voice.locale?.country.orEmpty()
        if (!voice.isNetworkConnectionRequired) score += 250
        if (lowerName.contains("neural") || lowerName.contains("wavenet") || lowerName.contains("studio")) score += 400
        if (lowerName.contains("hq") || lowerName.contains("enhanced")) score += 200
        if (country.equals("SA", ignoreCase = true)) score += 120
        if (country.equals("US", ignoreCase = true)) score += 80
        val notInstalled = try {
            voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true
        } catch (_: Throwable) {
            false
        }
        if (notInstalled) score -= 2000
        return score
    }

    private fun formatVoiceLabel(voice: Voice): String {
        val locale = voice.locale
        val lang = locale?.language?.lowercase(Locale.ROOT).orEmpty()
        val country = locale?.country.orEmpty()
        val langName = when (lang) {
            "ar" -> if (country.isNotBlank()) "Arabic ($country)" else "Arabic"
            "en" -> if (country.isNotBlank()) "English ($country)" else "English"
            else -> locale?.displayLanguage?.takeIf { it.isNotBlank() } ?: "Voice"
        }
        val qualityTag = when {
            voice.quality >= Voice.QUALITY_VERY_HIGH -> "Studio HD"
            voice.quality >= Voice.QUALITY_HIGH -> "High Quality"
            voice.quality >= Voice.QUALITY_NORMAL -> "Standard"
            else -> "Basic"
        }
        val offlineTag = if (voice.isNetworkConnectionRequired) "Cloud" else "Offline"
        return "$langName • $qualityTag ($offlineTag)"
    }

    fun openTtsVoiceInstaller(activityContext: Context) {
        try {
            val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityContext.startActivity(installIntent)
        } catch (_: Throwable) {
            try {
                val settingsIntent = Intent("com.android.settings.TTS_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activityContext.startActivity(settingsIntent)
            } catch (e: Throwable) {
                Log.e("AudioHapticManager", "Unable to open TTS settings", e)
            }
        }
    }

    private fun generatePcmSounds() {
        try {
            val offsets = listOf(0.975, 1.0, 1.028)
            // Madinah Olive Wood: dry wooden knock transient + 3 warm woody body resonances
            woodVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 760.0 * pitch,
                    f2 = 1390.0 * pitch,
                    f3 = 2380.0 * pitch,
                    decay1 = 92.0,
                    decay2 = 145.0,
                    decay3 = 230.0,
                    transientStrength = 0.28,
                    durationMs = 42,
                    seed = 101 + idx
                )
            }
            // Crystal Agate: crisp glassy strike + luminous high crystalline overtones
            crystalVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 1980.0 * pitch,
                    f2 = 3420.0 * pitch,
                    f3 = 5180.0 * pitch,
                    decay1 = 58.0,
                    decay2 = 95.0,
                    decay3 = 160.0,
                    transientStrength = 0.22,
                    durationMs = 54,
                    seed = 201 + idx
                )
            }
            // Brass Sanctuary Chime: warm harmonic singing-bell sustain
            chimeVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 1046.5 * pitch,
                    f2 = 2093.0 * pitch,
                    f3 = 3136.0 * pitch,
                    decay1 = 26.0,
                    decay2 = 42.0,
                    decay3 = 68.0,
                    transientStrength = 0.08,
                    durationMs = 110,
                    seed = 301 + idx
                )
            }
            // Pearl Silk Tap: soft warm nacre click with gentle muted low-mid resonance
            pearlVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 620.0 * pitch,
                    f2 = 1140.0 * pitch,
                    f3 = 1780.0 * pitch,
                    decay1 = 120.0,
                    decay2 = 190.0,
                    decay3 = 290.0,
                    transientStrength = 0.18,
                    durationMs = 34,
                    seed = 401 + idx
                )
            }
        } catch (e: Exception) {
            Log.e("AudioHapticManager", "Error generating PCM sounds", e)
        }
    }

    private fun synthesizeMultiModalBead(
        f1: Double,
        f2: Double,
        f3: Double,
        decay1: Double,
        decay2: Double,
        decay3: Double,
        transientStrength: Double,
        durationMs: Int,
        seed: Int
    ): ByteArray {
        val signalSamples = (sampleRate * durationMs / 1000.0).toInt().coerceAtLeast(64)
        val minBytes = try {
            AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(0)
        } catch (_: Throwable) {
            0
        }
        val totalBytes = maxOf(signalSamples * 2, minBytes).let { if (it % 2 == 0) it else it + 1 }
        val pcmData = ByteArray(totalBytes)
        val rng = Random(seed)

        for (i in 0 until signalSamples) {
            val t = i.toDouble() / sampleRate
            // Smooth 0.4ms attack ramp avoids digital DC click
            val attack = if (t < 0.0004) t / 0.0004 else 1.0

            // Coupled modal body resonances (higher modes decay naturally faster)
            val body = (
                0.56 * sin(2.0 * PI * f1 * t) * exp(-decay1 * t) +
                0.30 * sin(2.0 * PI * f2 * t) * exp(-decay2 * t) +
                0.14 * sin(2.0 * PI * f3 * t) * exp(-decay3 * t)
            )

            // Short percussive contact burst in first ~3ms for physical material texture
            val transientEnv = exp(-1100.0 * t)
            val transient = (rng.nextDouble() * 2.0 - 1.0) * transientStrength * transientEnv

            val sampleVal = ((body + transient) * attack).coerceIn(-1.0, 1.0)
            val sampleShort = (sampleVal * 32767).toInt().toShort()
            pcmData[i * 2] = (sampleShort.toInt() and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
        }
        return pcmData
    }

    fun playBeadSound(soundType: SoundEffectType) {
        if (soundType == SoundEffectType.MUTE) return
        val bank = when (soundType) {
            SoundEffectType.WOOD_CLICK -> woodVariations
            SoundEffectType.CRYSTAL_CLICK -> crystalVariations
            SoundEffectType.SOFT_CHIME -> chimeVariations
            SoundEffectType.PEARL_TAP -> pearlVariations
            SoundEffectType.MUTE -> emptyList()
        }
        if (bank.isEmpty()) return
        val pcm = bank[variationCursor % bank.size]
        if (pcm.isEmpty()) return
        variationCursor = (variationCursor + 1) % 300

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

            if (track.state != AudioTrack.STATE_INITIALIZED) {
                try {
                    track.release()
                } catch (_: Throwable) {}
                return
            }

            track.write(pcm, 0, pcm.size)
            track.play()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }
                    track.release()
                } catch (_: Throwable) {}
            }, 220)
        } catch (t: Throwable) {
            Log.e("AudioHapticManager", "Failed to play sound", t)
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
        } catch (e: Throwable) {
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
        } catch (_: Throwable) {}
    }

    fun speakRecitation(arabicText: String, transliteration: String) {
        val engine = tts ?: return
        if (!isTtsReady) return

        try {
            val shouldSpeakArabic = preferArabicVoice && isArabicSupported && arabicText.isNotBlank()
            val voices = try { engine.voices?.filterNotNull().orEmpty() } catch (_: Throwable) { emptyList() }
            val chosen = if (preferredVoiceName != null) {
                voices.find { it.name == preferredVoiceName }
            } else {
                pickBestVoice(voices, preferArabic = shouldSpeakArabic)
            }

            if (chosen != null) {
                try {
                    engine.voice = chosen
                    chosen.locale?.let { engine.language = it }
                } catch (_: Throwable) {}
            } else {
                try {
                    engine.language = if (shouldSpeakArabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
                } catch (_: Throwable) {}
            }
            try {
                engine.setSpeechRate(speechCadence.rate)
                engine.setPitch(1.0f)
            } catch (_: Throwable) {}

            val rawText = if (chosen?.locale?.language.equals("ar", ignoreCase = true) || (chosen == null && shouldSpeakArabic)) {
                arabicText.ifBlank { transliteration }
            } else {
                transliteration.ifBlank { arabicText }
            }

            // Clean formatting symbols that cause robotic pauses in TTS
            val cleanText = rawText
                .replace("ﷺ", "")
                .replace("•", ",")
                .replace("(", "")
                .replace(")", "")
                .trim()

            if (cleanText.isNotEmpty()) {
                engine.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "dhikr_recitation")
            }
        } catch (t: Throwable) {
            Log.e("AudioHapticManager", "TTS speak failed", t)
        }
    }

    fun stopRecitation() {
        try {
            tts?.stop()
        } catch (_: Throwable) {}
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {
        } finally {
            tts = null
        }
    }
}
