package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.abs
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

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Preloaded SoundPool using USAGE_MEDIA so bead taps play reliably with zero cold-start clipping
    private var soundPool: SoundPool? = null
    private val loadedSoundIds = ConcurrentHashMap<Int, Boolean>()
    private val soundBanks = ConcurrentHashMap<SoundEffectType, List<Int>>()

    // PCM bank retained as immediate fallback while SoundPool finishes initial load
    private var woodVariations: List<ByteArray> = emptyList()
    private var crystalVariations: List<ByteArray> = emptyList()
    private var chimeVariations: List<ByteArray> = emptyList()
    private var pearlVariations: List<ByteArray> = emptyList()
    private var lapChimePcm: ByteArray = ByteArray(0)
    private var milestoneChimePcm: ByteArray = ByteArray(0)
    private var lapChimeSoundId: Int = 0
    private var milestoneChimeSoundId: Int = 0
    private var variationCursor = 0
    private val sampleRate = 44100

    init {
        initTts()
        initSoundEffects()
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

    private fun initSoundEffects() {
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attrs)
                .build().apply {
                    setOnLoadCompleteListener { _, sampleId, status ->
                        if (status == 0) {
                            loadedSoundIds[sampleId] = true
                        }
                    }
                }
        } catch (t: Throwable) {
            Log.e("AudioHapticManager", "SoundPool init error", t)
        }

        audioScope.launch {
            generateAndLoadSounds()
        }
    }

    private fun generateAndLoadSounds() {
        try {
            val offsets = listOf(0.975, 1.0, 1.028)
            // Madinah Olive Wood: crisp resonant wooden knock that speaks cleanly on phone micro-speakers
            woodVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 680.0 * pitch,
                    f2 = 1260.0 * pitch,
                    f3 = 2120.0 * pitch,
                    decay1 = 36.0,
                    decay2 = 62.0,
                    decay3 = 110.0,
                    transientStrength = 0.34,
                    durationMs = 85,
                    seed = 101 + idx
                )
            }
            // Crystal Agate: bright glassy strike + clear crystalline overtones
            crystalVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 1680.0 * pitch,
                    f2 = 2890.0 * pitch,
                    f3 = 4320.0 * pitch,
                    decay1 = 28.0,
                    decay2 = 46.0,
                    decay3 = 82.0,
                    transientStrength = 0.25,
                    durationMs = 110,
                    seed = 201 + idx
                )
            }
            // Brass Sanctuary Chime: warm harmonic singing-bell sustain
            chimeVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 880.0 * pitch,
                    f2 = 1760.0 * pitch,
                    f3 = 2640.0 * pitch,
                    decay1 = 14.0,
                    decay2 = 24.0,
                    decay3 = 42.0,
                    transientStrength = 0.12,
                    durationMs = 180,
                    seed = 301 + idx
                )
            }
            // Pearl Silk Tap: warm soft nacre tap with audible body
            pearlVariations = offsets.mapIndexed { idx, pitch ->
                synthesizeMultiModalBead(
                    f1 = 540.0 * pitch,
                    f2 = 980.0 * pitch,
                    f3 = 1560.0 * pitch,
                    decay1 = 42.0,
                    decay2 = 72.0,
                    decay3 = 120.0,
                    transientStrength = 0.24,
                    durationMs = 75,
                    seed = 401 + idx
                )
            }

            val pool = soundPool ?: return
            val sfxDir = File(context.cacheDir, "tasbih_bead_sfx").apply { mkdirs() }
            val banks = mapOf(
                SoundEffectType.WOOD_CLICK to woodVariations,
                SoundEffectType.CRYSTAL_CLICK to crystalVariations,
                SoundEffectType.SOFT_CHIME to chimeVariations,
                SoundEffectType.PEARL_TAP to pearlVariations
            )
            banks.forEach { (type, variations) ->
                val ids = mutableListOf<Int>()
                variations.forEachIndexed { idx, pcm ->
                    try {
                        val wavFile = File(sfxDir, "${type.name.lowercase()}_v$idx.wav")
                        writePcmAsWav(wavFile, pcm)
                        val id = pool.load(wavFile.absolutePath, 1)
                        if (id > 0) ids.add(id)
                    } catch (t: Throwable) {
                        Log.e("AudioHapticManager", "Failed loading WAV for $type", t)
                    }
                }
                if (ids.isNotEmpty()) {
                    soundBanks[type] = ids
                }
            }

            // Gentle 2-note pentatonic chime for target laps (D5 -> A5)
            lapChimePcm = synthesizeHarmonicChime(
                frequenciesHz = listOf(587.33, 880.0),
                noteOffsetSeconds = 0.085,
                durationMs = 460
            )
            // Radiant 3-note sanctuary chord for playlist / daily goal completion (D5 -> F#5 -> A5)
            milestoneChimePcm = synthesizeHarmonicChime(
                frequenciesHz = listOf(587.33, 739.99, 880.0),
                noteOffsetSeconds = 0.095,
                durationMs = 660
            )

            try {
                val lapWav = File(sfxDir, "lap_sanctuary_chime.wav")
                writePcmAsWav(lapWav, lapChimePcm)
                val lapId = pool.load(lapWav.absolutePath, 1)
                if (lapId > 0) lapChimeSoundId = lapId

                val milestoneWav = File(sfxDir, "milestone_sanctuary_chime.wav")
                writePcmAsWav(milestoneWav, milestoneChimePcm)
                val milestoneId = pool.load(milestoneWav.absolutePath, 1)
                if (milestoneId > 0) milestoneChimeSoundId = milestoneId
            } catch (t: Throwable) {
                Log.e("AudioHapticManager", "Failed loading harmonic chimes", t)
            }
        } catch (e: Throwable) {
            Log.e("AudioHapticManager", "Error generating bead sounds", e)
        }
    }

    internal fun synthesizeHarmonicChime(
        frequenciesHz: List<Double>,
        noteOffsetSeconds: Double,
        durationMs: Int
    ): ByteArray {
        val signalSamples = (sampleRate * durationMs / 1000.0).toInt().coerceAtLeast(256)
        val raw = DoubleArray(signalSamples)
        var maxPeak = 1e-6

        for (i in 0 until signalSamples) {
            val t = i.toDouble() / sampleRate
            var mix = 0.0
            frequenciesHz.forEachIndexed { idx, freq ->
                val localT = t - (idx * noteOffsetSeconds)
                if (localT >= 0.0) {
                    val attack = if (localT < 0.008) localT / 0.008 else 1.0
                    val body = (
                        0.68 * sin(2.0 * PI * freq * localT) * exp(-5.8 * localT) +
                        0.22 * sin(2.0 * PI * freq * 2.0 * localT) * exp(-11.5 * localT) +
                        0.10 * sin(2.0 * PI * freq * 3.0 * localT) * exp(-18.0 * localT)
                    )
                    mix += body * attack
                }
            }
            val tailSamples = (sampleRate * 0.025).toInt().coerceAtLeast(1)
            val tailEnv = if (i >= signalSamples - tailSamples) {
                (signalSamples - 1 - i).toDouble() / tailSamples
            } else 1.0
            val sampleVal = mix * tailEnv
            raw[i] = sampleVal
            val absVal = abs(sampleVal)
            if (absVal > maxPeak) maxPeak = absVal
        }

        val normGain = 0.82 / maxPeak
        val pcmData = ByteArray(signalSamples * 2)
        for (i in 0 until signalSamples) {
            val normalized = (raw[i] * normGain).coerceIn(-1.0, 1.0)
            val sampleShort = (normalized * 32767.0).toInt().toShort()
            pcmData[i * 2] = (sampleShort.toInt() and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
        }
        return pcmData
    }

    internal fun synthesizeMultiModalBead(
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
        val signalSamples = (sampleRate * durationMs / 1000.0).toInt().coerceAtLeast(128)
        val raw = DoubleArray(signalSamples)
        val rng = Random(seed)
        var maxPeak = 1e-6

        for (i in 0 until signalSamples) {
            val t = i.toDouble() / sampleRate
            // Smooth 0.5ms attack + smooth last 6ms tail fade avoids digital click
            val attack = if (t < 0.0005) t / 0.0005 else 1.0
            val tailSamples = (sampleRate * 0.006).toInt().coerceAtLeast(1)
            val tailEnv = if (i >= signalSamples - tailSamples) {
                (signalSamples - 1 - i).toDouble() / tailSamples
            } else {
                1.0
            }

            // Coupled modal body resonances
            val body = (
                0.56 * sin(2.0 * PI * f1 * t) * exp(-decay1 * t) +
                0.30 * sin(2.0 * PI * f2 * t) * exp(-decay2 * t) +
                0.14 * sin(2.0 * PI * f3 * t) * exp(-decay3 * t)
            )

            // Short percussive contact burst in first ~4ms for physical material texture
            val transientEnv = exp(-750.0 * t)
            val transient = (rng.nextDouble() * 2.0 - 1.0) * transientStrength * transientEnv

            val sampleVal = (body + transient) * attack * tailEnv
            raw[i] = sampleVal
            val absVal = abs(sampleVal)
            if (absVal > maxPeak) maxPeak = absVal
        }

        // Peak-normalize to 0.94 so clicks are crisp and audible on every device speaker
        val normGain = 0.94 / maxPeak
        val pcmData = ByteArray(signalSamples * 2)
        for (i in 0 until signalSamples) {
            val normalized = (raw[i] * normGain).coerceIn(-1.0, 1.0)
            val sampleShort = (normalized * 32767.0).toInt().toShort()
            pcmData[i * 2] = (sampleShort.toInt() and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
        }
        return pcmData
    }

    internal fun buildWavBytes(pcmData: ByteArray): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2 // 16-bit mono
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xFF).toByte()
        header[5] = ((totalDataLen shr 8) and 0xFF).toByte()
        header[6] = ((totalDataLen shr 16) and 0xFF).toByte()
        header[7] = ((totalDataLen shr 24) and 0xFF).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // PCM subchunk size
        header[20] = 1 // PCM format
        header[22] = 1 // Mono
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()
        header[32] = 2 // Block align
        header[34] = 16 // Bits per sample
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xFF).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xFF).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xFF).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xFF).toByte()
        return header + pcmData
    }

    private fun writePcmAsWav(file: File, pcmData: ByteArray) {
        val wavBytes = buildWavBytes(pcmData)
        FileOutputStream(file).use { out ->
            out.write(wavBytes)
        }
    }

    fun playBeadSound(soundType: SoundEffectType) {
        if (soundType == SoundEffectType.MUTE) return
        val cursor = variationCursor
        variationCursor = (variationCursor + 1) % 300

        // Primary path: preloaded hardware-mixed SoundPool on USAGE_MEDIA
        val pool = soundPool
        val ids = soundBanks[soundType]
        if (pool != null && !ids.isNullOrEmpty()) {
            val soundId = ids[cursor % ids.size]
            if (loadedSoundIds[soundId] == true) {
                try {
                    val streamId = pool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                    if (streamId != 0) return
                } catch (t: Throwable) {
                    Log.e("AudioHapticManager", "SoundPool play failed", t)
                }
            }
        }

        // Immediate fallback path on background thread using USAGE_MEDIA MODE_STREAM
        val bank = when (soundType) {
            SoundEffectType.WOOD_CLICK -> woodVariations
            SoundEffectType.CRYSTAL_CLICK -> crystalVariations
            SoundEffectType.SOFT_CHIME -> chimeVariations
            SoundEffectType.PEARL_TAP -> pearlVariations
            SoundEffectType.MUTE -> emptyList()
        }
        if (bank.isEmpty()) return
        val pcm = bank[cursor % bank.size]
        if (pcm.isEmpty()) return

        audioScope.launch {
            playPcmStreamFallback(pcm)
        }
    }

    fun playLapChime() {
        val pool = soundPool
        if (pool != null && lapChimeSoundId > 0 && loadedSoundIds[lapChimeSoundId] == true) {
            try {
                val streamId = pool.play(lapChimeSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
                if (streamId != 0) return
            } catch (_: Throwable) {}
        }
        if (lapChimePcm.isNotEmpty()) {
            audioScope.launch { playPcmStreamFallback(lapChimePcm) }
        }
    }

    fun playMilestoneChime() {
        val pool = soundPool
        if (pool != null && milestoneChimeSoundId > 0 && loadedSoundIds[milestoneChimeSoundId] == true) {
            try {
                val streamId = pool.play(milestoneChimeSoundId, 0.92f, 0.92f, 1, 0, 1.0f)
                if (streamId != 0) return
            } catch (_: Throwable) {}
        }
        if (milestoneChimePcm.isNotEmpty()) {
            audioScope.launch { playPcmStreamFallback(milestoneChimePcm) }
        }
    }

    private fun playPcmStreamFallback(pcm: ByteArray) {
        var track: AudioTrack? = null
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(pcm.size)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(maxOf(minBuf, pcm.size))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (track.state != AudioTrack.STATE_INITIALIZED) return

            track.play()
            track.write(pcm, 0, pcm.size)
            val sleepMs = ((pcm.size / 2) * 1000L / sampleRate) + 40L
            Thread.sleep(sleepMs)
        } catch (t: Throwable) {
            Log.e("AudioHapticManager", "Fallback stream playback failed", t)
        } finally {
            try {
                if (track?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track?.release()
            } catch (_: Throwable) {}
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
            audioScope.cancel()
        } catch (_: Throwable) {}
        try {
            soundPool?.release()
        } catch (_: Throwable) {} finally {
            soundPool = null
            loadedSoundIds.clear()
            soundBanks.clear()
        }
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {
        } finally {
            tts = null
        }
    }
}
