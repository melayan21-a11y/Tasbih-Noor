package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TasbihDatabase
import com.example.data.dao.DhikrDailyStat
import com.example.data.dao.DhikrTypeStat
import com.example.data.model.DhikrItem
import com.example.data.model.DhikrPlaylist
import com.example.data.model.DhikrPlaylistSerializer
import com.example.data.model.DhikrPlaylistStep
import com.example.data.repository.StreakInfo
import com.example.data.repository.TasbihRepository
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.CityLocation
import com.example.data.prayer.CityLocations
import com.example.data.prayer.CompassSensorManager
import com.example.data.prayer.JuristicMethod
import com.example.data.prayer.PrayerCalculator
import com.example.data.prayer.PrayerTimesResult
import com.example.data.backup.BackupManager
import com.example.data.backup.RestoreResult
import com.example.widget.TasbihAppWidgetProvider
import com.example.notifications.DhikrReminderManager
import com.example.notifications.ReminderSetting
import com.example.ui.components.BeadDesign
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.ui.theme.AppThemeStyle
import com.example.util.AudioHapticManager
import com.example.util.HapticIntensity
import com.example.util.SoundEffectType
import com.example.util.SpeechCadence
import com.example.util.TasbihPreferences
import com.example.util.VoiceEngineState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthDayStat(
    val dayOfMonth: Int,
    val dateString: String,
    val count: Int,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class MilestoneBanner(
    val id: Long,
    val title: String,
    val subtitle: String,
    val isMajor: Boolean = false
)

enum class SalahSetStage(
    val stepIndex: Int,
    val transliteration: String,
    val arabicText: String,
    val translation: String,
    val target: Int
) {
    SUBHANALLAH(
        stepIndex = 1,
        transliteration = "SubhanAllah",
        arabicText = "سُبْحَانَ اللَّهِ",
        translation = "Glory be to Allah",
        target = 33
    ),
    ALHAMDULILLAH(
        stepIndex = 2,
        transliteration = "Alhamdulillah",
        arabicText = "الْحَمْدُ لِلَّهِ",
        translation = "All praise is due to Allah",
        target = 33
    ),
    ALLAHU_AKBAR(
        stepIndex = 3,
        transliteration = "Allahu Akbar",
        arabicText = "اللَّهُ أَكْبَرُ",
        translation = "Allah is the Greatest",
        target = 33
    ),
    TAHLIL_SEAL(
        stepIndex = 4,
        transliteration = "La ilaha illallah wahdahu la shareeka lah, lahul mulku wa lahul hamdu wa Huwa 'ala kulli shay'in qadeer",
        arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
        translation = "There is no deity except Allah alone without partner, to Him belongs sovereignty and all praise, and He is over all things capable",
        target = 1
    )
}

class TasbihViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = TasbihPreferences(application.applicationContext)
    val audioHapticManager = AudioHapticManager(application.applicationContext)
    val reminderManager = DhikrReminderManager(application.applicationContext)

    private val database = TasbihDatabase.getDatabase(application.applicationContext, viewModelScope)
    private val repository = TasbihRepository(
        database.dhikrDao(),
        database.dhikrLogDao(),
        database.dhikrPlaylistDao()
    )

    val allDhikrs: StateFlow<List<DhikrItem>> = repository.allDhikrs
        .map { list -> list.distinctBy { it.transliteration.trim().lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<DhikrPlaylist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTotals: StateFlow<List<DhikrDailyStat>> = repository.dailyTotals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val typeStats: StateFlow<List<DhikrTypeStat>> = repository.typeStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotal: StateFlow<Int> = repository.getTodayTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lifetimeTotal: StateFlow<Int> = repository.lifetimeTotal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val streakInfo: StateFlow<StreakInfo> = repository.calculateStreakFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakInfo())

    // Guided Playlist Runner (supports built-in After-Salah Set + user custom playlists)
    private val _activePlaylist = MutableStateFlow<DhikrPlaylist?>(null)
    val activePlaylist: StateFlow<DhikrPlaylist?> = _activePlaylist.asStateFlow()

    private val _activePlaylistSteps = MutableStateFlow<List<DhikrPlaylistStep>>(emptyList())
    val activePlaylistSteps: StateFlow<List<DhikrPlaylistStep>> = _activePlaylistSteps.asStateFlow()

    private val _activePlaylistStepIndex = MutableStateFlow(0)
    val activePlaylistStepIndex: StateFlow<Int> = _activePlaylistStepIndex.asStateFlow()

    // Legacy After-Salah Tasbih flags kept synced for full compatibility
    private val _isSalahSetMode = MutableStateFlow(preferences.isSalahSetMode)
    val isSalahSetMode: StateFlow<Boolean> = _isSalahSetMode.asStateFlow()

    private val _salahSetStage = MutableStateFlow(SalahSetStage.SUBHANALLAH)
    val salahSetStage: StateFlow<SalahSetStage> = _salahSetStage.asStateFlow()

    // Active Dhikr & Counter State
    private val _selectedDhikr = MutableStateFlow<DhikrItem?>(null)
    val selectedDhikr: StateFlow<DhikrItem?> = _selectedDhikr.asStateFlow()

    private val _currentCount = MutableStateFlow(0)
    val currentCount: StateFlow<Int> = _currentCount.asStateFlow()

    private val _lapsCompleted = MutableStateFlow(0)
    val lapsCompleted: StateFlow<Int> = _lapsCompleted.asStateFlow()

    private val _targetCount = MutableStateFlow(preferences.savedTargetCount)
    val targetCount: StateFlow<Int> = _targetCount.asStateFlow()

    // Preferences & Themes
    private val _appThemeStyle = MutableStateFlow(preferences.appThemeStyle)
    val appThemeStyle: StateFlow<AppThemeStyle> = _appThemeStyle.asStateFlow()

    private val _isNightMode = MutableStateFlow(preferences.appThemeStyle.isDark)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _beadMaterial = MutableStateFlow(preferences.beadMaterial)
    val beadMaterial: StateFlow<BeadMaterial> = _beadMaterial.asStateFlow()

    private val _beadDesign = MutableStateFlow(preferences.beadDesign)
    val beadDesign: StateFlow<BeadDesign> = _beadDesign.asStateFlow()

    private val _soundType = MutableStateFlow(preferences.soundType)
    val soundType: StateFlow<SoundEffectType> = _soundType.asStateFlow()

    private val _hapticIntensity = MutableStateFlow(preferences.hapticIntensity)
    val hapticIntensity: StateFlow<HapticIntensity> = _hapticIntensity.asStateFlow()

    // Bead String Tension & Sensitivity Control
    private val _beadTension = MutableStateFlow(preferences.beadTension)
    val beadTension: StateFlow<BeadTension> = _beadTension.asStateFlow()

    private val _singleBeadLock = MutableStateFlow(preferences.singleBeadLock)
    val singleBeadLock: StateFlow<Boolean> = _singleBeadLock.asStateFlow()

    // Keep screen awake during dhikr recitation
    private val _keepScreenAwake = MutableStateFlow(preferences.keepScreenAwake)
    val keepScreenAwake: StateFlow<Boolean> = _keepScreenAwake.asStateFlow()

    // Daily Target Goal, Chimes, Focus Mode & Bead Noor Ripple
    private val _dailyGoalTarget = MutableStateFlow(preferences.dailyGoalTarget)
    val dailyGoalTarget: StateFlow<Int> = _dailyGoalTarget.asStateFlow()

    private val _milestoneChimeEnabled = MutableStateFlow(preferences.milestoneChimeEnabled)
    val milestoneChimeEnabled: StateFlow<Boolean> = _milestoneChimeEnabled.asStateFlow()

    private val _isFocusMode = MutableStateFlow(preferences.isFocusMode)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    private val _beadPullSequence = MutableStateFlow(0)
    val beadPullSequence: StateFlow<Int> = _beadPullSequence.asStateFlow()

    private val _milestoneBanner = MutableStateFlow<MilestoneBanner?>(null)
    val milestoneBanner: StateFlow<MilestoneBanner?> = _milestoneBanner.asStateFlow()

    private var milestoneDismissJob: Job? = null
    private var lastCelebratedGoalDate: String? = null

    // Auto-Guide & Natural Voice Engine
    val voiceState: StateFlow<VoiceEngineState> = audioHapticManager.voiceState

    private val _speechCadence = MutableStateFlow(preferences.speechCadence)
    val speechCadence: StateFlow<SpeechCadence> = _speechCadence.asStateFlow()

    private val _preferArabicVoice = MutableStateFlow(preferences.preferArabicVoice)
    val preferArabicVoice: StateFlow<Boolean> = _preferArabicVoice.asStateFlow()

    private val _isAutoGuiding = MutableStateFlow(false)
    val isAutoGuiding: StateFlow<Boolean> = _isAutoGuiding.asStateFlow()

    private val _autoGuideSpeedSeconds = MutableStateFlow(2.5f)
    val autoGuideSpeedSeconds: StateFlow<Float> = _autoGuideSpeedSeconds.asStateFlow()

    private var autoGuideJob: Job? = null

    // Reminders
    private val _reminders = MutableStateFlow(
        listOf(
            ReminderSetting(1, "Morning Adhkar", "Start your morning with SubhanAllah & Alhamdulillah", 6, 30, true),
            ReminderSetting(2, "Evening Adhkar", "Protect your evening with Astaghfirullah & Ayatul Kursi", 18, 0, true),
            ReminderSetting(3, "Night Meditative Dhikr", "Peaceful nighttime remembrance before sleep", 21, 30, false)
        )
    )
    val reminders: StateFlow<List<ReminderSetting>> = _reminders.asStateFlow()

    // Active Monthly Calendar Navigation
    private val _currentCalendarMonth = MutableStateFlow(Calendar.getInstance())
    val currentCalendarMonth: StateFlow<Calendar> = _currentCalendarMonth.asStateFlow()

    // Offline Prayer Times & Qibla Compass
    val compassSensorManager = CompassSensorManager(application.applicationContext)
    val azimuthDegrees: StateFlow<Float> = compassSensorManager.azimuthDegrees

    private val _selectedCity = MutableStateFlow(
        CityLocations.findCity(preferences.selectedCityName)
            ?: CityLocation(preferences.selectedCityName, preferences.selectedCityCountry, preferences.selectedLatitude, preferences.selectedLongitude)
    )
    val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

    private val _calculationMethod = MutableStateFlow(
        try {
            CalculationMethod.valueOf(preferences.selectedCalculationMethod)
        } catch (_: Exception) {
            CalculationMethod.UMM_AL_QURA
        }
    )
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _juristicMethod = MutableStateFlow(
        try {
            JuristicMethod.valueOf(preferences.selectedAsrJuristic)
        } catch (_: Exception) {
            JuristicMethod.STANDARD
        }
    )
    val juristicMethod: StateFlow<JuristicMethod> = _juristicMethod.asStateFlow()

    private val _prayerTimesResult = MutableStateFlow(
        PrayerCalculator.calculate(
            calendar = Calendar.getInstance(),
            latitude = preferences.selectedLatitude,
            longitude = preferences.selectedLongitude,
            method = try { CalculationMethod.valueOf(preferences.selectedCalculationMethod) } catch (_: Exception) { CalculationMethod.UMM_AL_QURA },
            juristic = try { JuristicMethod.valueOf(preferences.selectedAsrJuristic) } catch (_: Exception) { JuristicMethod.STANDARD }
        )
    )
    val prayerTimesResult: StateFlow<PrayerTimesResult> = _prayerTimesResult.asStateFlow()

    init {
        audioHapticManager.applyVoicePreferences(
            selectedVoiceName = preferences.selectedVoiceName,
            cadence = preferences.speechCadence,
            preferArabic = preferences.preferArabicVoice
        )
        viewModelScope.launch {
            repository.ensureDefaultAdhkarPopulated()
            // Restore previously selected Dhikr
            val savedId = preferences.selectedDhikrId
            allDhikrs.collect { list ->
                if (_selectedDhikr.value == null && list.isNotEmpty()) {
                    val matching = list.find { it.id == savedId } ?: list.first()
                    _selectedDhikr.value = matching
                    _targetCount.value = if (preferences.savedTargetCount > 0) preferences.savedTargetCount else matching.defaultTarget
                }
            }
        }
        viewModelScope.launch {
            todayTotal.collect { count ->
                preferences.widgetTodayCount = count
                try {
                    TasbihAppWidgetProvider.updateAllWidgets(application)
                } catch (_: Exception) {}
            }
        }
        viewModelScope.launch {
            streakInfo.collect { streak ->
                preferences.widgetStreakDays = streak.currentStreak
                try {
                    TasbihAppWidgetProvider.updateAllWidgets(application)
                } catch (_: Exception) {}
            }
        }
    }

    fun startPlaylist(playlist: DhikrPlaylist) {
        val steps = playlist.steps()
        if (steps.isEmpty()) return
        _activePlaylist.value = playlist
        _activePlaylistSteps.value = steps
        _activePlaylistStepIndex.value = 0
        _currentCount.value = 0
        _lapsCompleted.value = 0
        _targetCount.value = steps[0].targetCount

        val isSalah = playlist.id == 1L
        _isSalahSetMode.value = isSalah
        preferences.isSalahSetMode = isSalah
        if (isSalah) {
            _salahSetStage.value = SalahSetStage.SUBHANALLAH
        }
        applyPlaylistStepDhikr(steps[0])
        stopAutoGuide()
    }

    private fun applyPlaylistStepDhikr(step: DhikrPlaylistStep) {
        val matching = allDhikrs.value.find { it.id == step.dhikrId }
            ?: DhikrItem(
                id = step.dhikrId,
                arabicText = step.arabicText,
                transliteration = step.transliteration,
                englishTranslation = step.englishTranslation,
                category = _activePlaylist.value?.name ?: "Playlist",
                defaultTarget = step.targetCount
            )
        _selectedDhikr.value = matching
    }

    fun exitPlaylistMode() {
        _activePlaylist.value = null
        _activePlaylistSteps.value = emptyList()
        _activePlaylistStepIndex.value = 0
        _isSalahSetMode.value = false
        preferences.isSalahSetMode = false
        _salahSetStage.value = SalahSetStage.SUBHANALLAH
        _currentCount.value = 0
        val savedId = preferences.selectedDhikrId
        val matching = allDhikrs.value.find { it.id == savedId } ?: allDhikrs.value.firstOrNull()
        _selectedDhikr.value = matching
        _targetCount.value = matching?.defaultTarget ?: 33
    }

    fun startSalahSetMode() {
        val salahPlaylist = allPlaylists.value.find { it.id == 1L } ?: DhikrPlaylist(
            id = 1L,
            name = "After-Salah Tasbih Set",
            subtitle = "Prophetic 100 Praises Chain • Sunnah",
            isBuiltIn = true,
            orderIndex = 1,
            stepsJson = DhikrPlaylistSerializer.stepsToJson(
                SalahSetStage.entries.map {
                    DhikrPlaylistStep(
                        dhikrId = when (it) {
                            SalahSetStage.SUBHANALLAH -> 1L
                            SalahSetStage.ALHAMDULILLAH -> 2L
                            SalahSetStage.ALLAHU_AKBAR -> 3L
                            SalahSetStage.TAHLIL_SEAL -> 5L
                        },
                        arabicText = it.arabicText,
                        transliteration = it.transliteration,
                        englishTranslation = it.translation,
                        targetCount = it.target
                    )
                }
            )
        )
        startPlaylist(salahPlaylist)
    }

    fun exitSalahSetMode() {
        exitPlaylistMode()
    }

    fun selectDhikr(dhikr: DhikrItem) {
        _activePlaylist.value = null
        _activePlaylistSteps.value = emptyList()
        _activePlaylistStepIndex.value = 0
        _isSalahSetMode.value = false
        preferences.isSalahSetMode = false
        _selectedDhikr.value = dhikr
        _targetCount.value = dhikr.defaultTarget
        _currentCount.value = 0
        _lapsCompleted.value = 0
        preferences.selectedDhikrId = dhikr.id
        preferences.savedTargetCount = dhikr.defaultTarget
        stopAutoGuide()
    }

    fun selectNextDhikr() {
        val playlistSteps = _activePlaylistSteps.value
        if (_activePlaylist.value != null && playlistSteps.isNotEmpty()) {
            val nextIdx = _activePlaylistStepIndex.value + 1
            if (nextIdx < playlistSteps.size) {
                _activePlaylistStepIndex.value = nextIdx
                _currentCount.value = 0
                _targetCount.value = playlistSteps[nextIdx].targetCount
                if (_isSalahSetMode.value && nextIdx in SalahSetStage.entries.indices) {
                    _salahSetStage.value = SalahSetStage.entries[nextIdx]
                }
                applyPlaylistStepDhikr(playlistSteps[nextIdx])
            } else {
                exitPlaylistMode()
            }
            return
        }
        val list = allDhikrs.value
        if (list.isEmpty()) return
        val currentId = _selectedDhikr.value?.id
        val currentIndex = list.indexOfFirst { it.id == currentId }
        val nextIndex = if (currentIndex >= 0 && currentIndex < list.size - 1) currentIndex + 1 else 0
        selectDhikr(list[nextIndex])
    }

    fun selectPreviousDhikr() {
        val playlistSteps = _activePlaylistSteps.value
        if (_activePlaylist.value != null && playlistSteps.isNotEmpty()) {
            val prevIdx = _activePlaylistStepIndex.value - 1
            if (prevIdx >= 0) {
                _activePlaylistStepIndex.value = prevIdx
                _currentCount.value = 0
                _targetCount.value = playlistSteps[prevIdx].targetCount
                if (_isSalahSetMode.value && prevIdx in SalahSetStage.entries.indices) {
                    _salahSetStage.value = SalahSetStage.entries[prevIdx]
                }
                applyPlaylistStepDhikr(playlistSteps[prevIdx])
            }
            return
        }
        val list = allDhikrs.value
        if (list.isEmpty()) return
        val currentId = _selectedDhikr.value?.id
        val currentIndex = list.indexOfFirst { it.id == currentId }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        selectDhikr(list[prevIndex])
    }

    private fun emitMilestoneBanner(title: String, subtitle: String, isMajor: Boolean = false) {
        milestoneDismissJob?.cancel()
        val id = System.currentTimeMillis()
        _milestoneBanner.value = MilestoneBanner(
            id = id,
            title = title,
            subtitle = subtitle,
            isMajor = isMajor
        )
        milestoneDismissJob = viewModelScope.launch {
            delay(2700)
            if (_milestoneBanner.value?.id == id) {
                _milestoneBanner.value = null
            }
        }
    }

    private fun checkDailyGoalThreshold(): Boolean {
        val goal = _dailyGoalTarget.value
        val today = repository.getTodayDateString()
        val currentToday = todayTotal.value
        if (goal > 0 && currentToday < goal && currentToday + 1 >= goal && lastCelebratedGoalDate != today) {
            lastCelebratedGoalDate = today
            if (_milestoneChimeEnabled.value) {
                audioHapticManager.playMilestoneChime()
            }
            audioHapticManager.triggerTargetReachedHaptic()
            emitMilestoneBanner(
                title = "Daily Goal Reached ($goal/$goal)",
                subtitle = "MashaAllah! Consistent daily remembrance illuminates the heart.",
                isMajor = true
            )
            return true
        }
        return false
    }

    fun onBeadPulled() {
        _beadPullSequence.value = _beadPullSequence.value + 1

        val activePl = _activePlaylist.value
        val playlistSteps = _activePlaylistSteps.value
        if (activePl != null && playlistSteps.isNotEmpty()) {
            audioHapticManager.playBeadSound(_soundType.value)
            audioHapticManager.triggerBeadHaptic(_hapticIntensity.value)

            val stepIdx = _activePlaylistStepIndex.value.coerceIn(0, playlistSteps.lastIndex)
            val step = playlistSteps[stepIdx]
            val dhikrToRecord = allDhikrs.value.find { it.id == step.dhikrId }
                ?: DhikrItem(
                    id = step.dhikrId,
                    arabicText = step.arabicText,
                    transliteration = step.transliteration,
                    englishTranslation = step.englishTranslation,
                    category = activePl.name,
                    defaultTarget = step.targetCount
                )

            val goalReachedNow = checkDailyGoalThreshold()
            viewModelScope.launch {
                repository.recordDhikrCount(dhikrToRecord, 1)
            }

            val newCount = _currentCount.value + 1
            if (newCount >= step.targetCount) {
                audioHapticManager.triggerTargetReachedHaptic()
                if (stepIdx < playlistSteps.lastIndex) {
                    val nextIdx = stepIdx + 1
                    val nextStep = playlistSteps[nextIdx]
                    _activePlaylistStepIndex.value = nextIdx
                    _currentCount.value = 0
                    _targetCount.value = nextStep.targetCount
                    if (_isSalahSetMode.value && nextIdx in SalahSetStage.entries.indices) {
                        _salahSetStage.value = SalahSetStage.entries[nextIdx]
                    }
                    applyPlaylistStepDhikr(nextStep)
                    if (!goalReachedNow) {
                        if (_milestoneChimeEnabled.value) audioHapticManager.playLapChime()
                        emitMilestoneBanner(
                            title = "Step ${stepIdx + 1} Complete",
                            subtitle = "Continuing to ${nextStep.transliteration} (${nextStep.targetCount}×)"
                        )
                    }
                } else {
                    _lapsCompleted.value += 1
                    _activePlaylistStepIndex.value = 0
                    val firstStep = playlistSteps[0]
                    _currentCount.value = 0
                    _targetCount.value = firstStep.targetCount
                    if (_isSalahSetMode.value) {
                        _salahSetStage.value = SalahSetStage.SUBHANALLAH
                    }
                    applyPlaylistStepDhikr(firstStep)
                    if (!goalReachedNow) {
                        if (_milestoneChimeEnabled.value) audioHapticManager.playMilestoneChime()
                        emitMilestoneBanner(
                            title = "${activePl.name} Complete!",
                            subtitle = "TaqabbalAllah • ${activePl.totalCount()} praises recorded",
                            isMajor = true
                        )
                    }
                }
            } else {
                _currentCount.value = newCount
            }
            return
        }

        val dhikr = _selectedDhikr.value ?: allDhikrs.value.firstOrNull() ?: return
        if (_selectedDhikr.value == null) {
            _selectedDhikr.value = dhikr
            _targetCount.value = dhikr.defaultTarget
        }

        // Play feedback
        audioHapticManager.playBeadSound(_soundType.value)
        audioHapticManager.triggerBeadHaptic(_hapticIntensity.value)

        val goalReachedNow = checkDailyGoalThreshold()
        val newCount = _currentCount.value + 1
        val target = _targetCount.value

        if (target > 0 && newCount >= target) {
            _currentCount.value = 0
            _lapsCompleted.value += 1
            audioHapticManager.triggerTargetReachedHaptic()
            if (!goalReachedNow) {
                if (_milestoneChimeEnabled.value) audioHapticManager.playLapChime()
                emitMilestoneBanner(
                    title = "Lap ${_lapsCompleted.value} Complete • MashaAllah",
                    subtitle = "$target ${dhikr.transliteration} finished"
                )
            }
        } else {
            _currentCount.value = newCount
        }

        // Record in database
        viewModelScope.launch {
            repository.recordDhikrCount(dhikr, 1)
        }
    }

    fun undoLastBead() {
        val activePl = _activePlaylist.value
        val playlistSteps = _activePlaylistSteps.value
        if (activePl != null && playlistSteps.isNotEmpty()) {
            if (_currentCount.value > 0) {
                _currentCount.value -= 1
            } else {
                val curIdx = _activePlaylistStepIndex.value
                if (curIdx > 0) {
                    val prevIdx = curIdx - 1
                    val prevStep = playlistSteps[prevIdx]
                    _activePlaylistStepIndex.value = prevIdx
                    _currentCount.value = (prevStep.targetCount - 1).coerceAtLeast(0)
                    _targetCount.value = prevStep.targetCount
                    if (_isSalahSetMode.value && prevIdx in SalahSetStage.entries.indices) {
                        _salahSetStage.value = SalahSetStage.entries[prevIdx]
                    }
                    applyPlaylistStepDhikr(prevStep)
                } else if (_lapsCompleted.value > 0) {
                    val lastIdx = playlistSteps.lastIndex
                    val lastStep = playlistSteps[lastIdx]
                    _lapsCompleted.value -= 1
                    _activePlaylistStepIndex.value = lastIdx
                    _currentCount.value = (lastStep.targetCount - 1).coerceAtLeast(0)
                    _targetCount.value = lastStep.targetCount
                    if (_isSalahSetMode.value && lastIdx in SalahSetStage.entries.indices) {
                        _salahSetStage.value = SalahSetStage.entries[lastIdx]
                    }
                    applyPlaylistStepDhikr(lastStep)
                }
            }
            return
        }

        if (_currentCount.value > 0) {
            _currentCount.value -= 1
        } else if (_lapsCompleted.value > 0 && _targetCount.value > 0) {
            _lapsCompleted.value -= 1
            _currentCount.value = _targetCount.value - 1
        }
    }

    fun resetCounter() {
        val playlistSteps = _activePlaylistSteps.value
        if (_activePlaylist.value != null && playlistSteps.isNotEmpty()) {
            _activePlaylistStepIndex.value = 0
            _currentCount.value = 0
            _targetCount.value = playlistSteps[0].targetCount
            if (_isSalahSetMode.value) {
                _salahSetStage.value = SalahSetStage.SUBHANALLAH
            }
            applyPlaylistStepDhikr(playlistSteps[0])
        } else {
            _currentCount.value = 0
        }
        _lapsCompleted.value = 0
        stopAutoGuide()
    }

    fun setTargetCount(target: Int) {
        _targetCount.value = target
        _currentCount.value = 0
        preferences.savedTargetCount = target
    }

    fun setDailyGoalTarget(goal: Int) {
        val sanitized = goal.coerceIn(10, 10000)
        _dailyGoalTarget.value = sanitized
        preferences.dailyGoalTarget = sanitized
    }

    fun setMilestoneChimeEnabled(enabled: Boolean) {
        _milestoneChimeEnabled.value = enabled
        preferences.milestoneChimeEnabled = enabled
        if (enabled) {
            audioHapticManager.playLapChime()
        }
    }

    fun setFocusMode(enabled: Boolean) {
        _isFocusMode.value = enabled
        preferences.isFocusMode = enabled
    }

    fun createCustomPlaylist(
        name: String,
        subtitle: String,
        steps: List<DhikrPlaylistStep>
    ) {
        if (name.isBlank() || steps.isEmpty()) return
        viewModelScope.launch {
            val totalBeads = steps.sumOf { it.targetCount.coerceAtLeast(1) }
            val resolvedSubtitle = subtitle.ifBlank {
                "${steps.size} steps • $totalBeads beads"
            }
            val playlist = DhikrPlaylist(
                name = name.trim(),
                subtitle = resolvedSubtitle,
                stepsJson = DhikrPlaylistSerializer.stepsToJson(steps),
                isBuiltIn = false,
                orderIndex = (allPlaylists.value.maxOfOrNull { it.orderIndex } ?: 3) + 1
            )
            repository.savePlaylist(playlist)
        }
    }

    fun deletePlaylist(playlist: DhikrPlaylist) {
        viewModelScope.launch {
            if (_activePlaylist.value?.id == playlist.id) {
                exitPlaylistMode()
            }
            repository.deletePlaylist(playlist)
        }
    }

    fun setNightMode(enabled: Boolean) {
        val nextTheme = if (enabled) preferences.lastDarkTheme else preferences.lastLightTheme
        setThemeStyle(nextTheme)
    }

    fun setThemeStyle(style: AppThemeStyle) {
        _appThemeStyle.value = style
        _isNightMode.value = style.isDark
        preferences.appThemeStyle = style
    }

    fun setBeadMaterial(material: BeadMaterial) {
        _beadMaterial.value = material
        preferences.beadMaterial = material
    }

    fun setBeadDesign(design: BeadDesign) {
        _beadDesign.value = design
        preferences.beadDesign = design
    }

    fun setSoundType(type: SoundEffectType) {
        _soundType.value = type
        preferences.soundType = type
        if (type != SoundEffectType.MUTE) {
            audioHapticManager.playBeadSound(type)
        }
    }

    fun setHapticIntensity(intensity: HapticIntensity) {
        _hapticIntensity.value = intensity
        preferences.hapticIntensity = intensity
        audioHapticManager.triggerBeadHaptic(intensity)
    }

    fun setBeadTension(tension: BeadTension) {
        _beadTension.value = tension
        preferences.beadTension = tension
    }

    fun setSingleBeadLock(enabled: Boolean) {
        _singleBeadLock.value = enabled
        preferences.singleBeadLock = enabled
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        _keepScreenAwake.value = enabled
        preferences.keepScreenAwake = enabled
    }

    fun toggleAutoGuide() {
        if (_isAutoGuiding.value) {
            stopAutoGuide()
        } else {
            startAutoGuide()
        }
    }

    private fun startAutoGuide() {
        _isAutoGuiding.value = true
        autoGuideJob?.cancel()
        autoGuideJob = viewModelScope.launch {
            val dhikr = _selectedDhikr.value ?: allDhikrs.value.firstOrNull()
            while (isActive && _isAutoGuiding.value) {
                if (dhikr != null) {
                    audioHapticManager.speakRecitation(dhikr.arabicText, dhikr.transliteration)
                }
                delay(600)
                onBeadPulled()
                val waitMillis = (_autoGuideSpeedSeconds.value * 1000).toLong()
                delay(waitMillis)
            }
        }
    }

    fun stopAutoGuide() {
        _isAutoGuiding.value = false
        autoGuideJob?.cancel()
        autoGuideJob = null
        audioHapticManager.stopRecitation()
    }

    fun speakCurrentDhikr() {
        val dhikr = _selectedDhikr.value ?: allDhikrs.value.firstOrNull() ?: return
        audioHapticManager.speakRecitation(dhikr.arabicText, dhikr.transliteration)
    }

    fun selectTtsVoice(voiceName: String?) {
        preferences.selectedVoiceName = voiceName
        audioHapticManager.applyVoicePreferences(
            selectedVoiceName = voiceName,
            cadence = _speechCadence.value,
            preferArabic = _preferArabicVoice.value
        )
    }

    fun setSpeechCadence(cadence: SpeechCadence) {
        _speechCadence.value = cadence
        preferences.speechCadence = cadence
        audioHapticManager.applyVoicePreferences(
            selectedVoiceName = preferences.selectedVoiceName,
            cadence = cadence,
            preferArabic = _preferArabicVoice.value
        )
    }

    fun setPreferArabicVoice(preferArabic: Boolean) {
        _preferArabicVoice.value = preferArabic
        preferences.preferArabicVoice = preferArabic
        // Reset explicit override if language changed so auto-selection picks best voice for that language
        preferences.selectedVoiceName = null
        audioHapticManager.applyVoicePreferences(
            selectedVoiceName = null,
            cadence = _speechCadence.value,
            preferArabic = preferArabic
        )
    }

    fun refreshVoices() {
        audioHapticManager.refreshAndApplyVoice()
    }

    fun addCustomDhikr(
        arabic: String,
        transliteration: String,
        translation: String,
        target: Int,
        category: String
    ) {
        viewModelScope.launch {
            val newItem = DhikrItem(
                arabicText = arabic.ifBlank { transliteration },
                transliteration = transliteration,
                englishTranslation = translation,
                category = category.ifBlank { "Custom" },
                defaultTarget = if (target > 0) target else 33,
                isCustom = true
            )
            repository.addCustomDhikr(newItem)
        }
    }

    fun deleteDhikr(item: DhikrItem) {
        viewModelScope.launch {
            repository.deleteDhikr(item)
            if (_selectedDhikr.value?.id == item.id) {
                _selectedDhikr.value = allDhikrs.value.firstOrNull { it.id != item.id }
            }
        }
    }

    fun toggleReminder(reminderId: Int, isEnabled: Boolean) {
        val updated = _reminders.value.map {
            if (it.id == reminderId) it.copy(isEnabled = isEnabled) else it
        }
        _reminders.value = updated
        val reminder = updated.find { it.id == reminderId } ?: return
        if (isEnabled) {
            reminderManager.scheduleReminder(reminder)
        } else {
            reminderManager.cancelReminder(reminderId)
        }
    }

    fun updateReminderTime(reminderId: Int, hour: Int, minute: Int) {
        val updated = _reminders.value.map {
            if (it.id == reminderId) it.copy(hour = hour, minute = minute) else it
        }
        _reminders.value = updated
        val reminder = updated.find { it.id == reminderId } ?: return
        if (reminder.isEnabled) {
            reminderManager.scheduleReminder(reminder)
        }
    }

    fun changeCalendarMonth(monthDelta: Int) {
        val newCal = Calendar.getInstance().apply {
            time = _currentCalendarMonth.value.time
            add(Calendar.MONTH, monthDelta)
        }
        _currentCalendarMonth.value = newCal
    }

    fun getMonthDays(dailyStats: List<DhikrDailyStat>, calMonth: Calendar): List<MonthDayStat> {
        val statMap = dailyStats.associate { it.dateString to it.totalCount }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)

        val cal = Calendar.getInstance().apply {
            time = calMonth.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val now = Calendar.getInstance()

        val list = mutableListOf<MonthDayStat>()
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = sdf.format(cal.time)
            val count = statMap[dateStr] ?: 0
            val isToday = dateStr == todayStr
            val isFuture = cal.after(now) && !isToday

            list.add(
                MonthDayStat(
                    dayOfMonth = day,
                    dateString = dateStr,
                    count = count,
                    isToday = isToday,
                    isFuture = isFuture
                )
            )
        }
        return list
    }

    // Prayer Times & Qibla Actions
    fun setCity(city: CityLocation) {
        _selectedCity.value = city
        preferences.selectedCityName = city.name
        preferences.selectedCityCountry = city.country
        preferences.selectedLatitude = city.latitude
        preferences.selectedLongitude = city.longitude
        refreshPrayerTimes()
    }

    fun setCalculationMethod(method: CalculationMethod) {
        _calculationMethod.value = method
        preferences.selectedCalculationMethod = method.name
        refreshPrayerTimes()
    }

    fun setJuristicMethod(juristic: JuristicMethod) {
        _juristicMethod.value = juristic
        preferences.selectedAsrJuristic = juristic.name
        refreshPrayerTimes()
    }

    fun refreshPrayerTimes() {
        val result = PrayerCalculator.calculate(
            calendar = Calendar.getInstance(),
            latitude = _selectedCity.value.latitude,
            longitude = _selectedCity.value.longitude,
            method = _calculationMethod.value,
            juristic = _juristicMethod.value
        )
        _prayerTimesResult.value = result
        compassSensorManager.setQiblaBearing(result.qiblaBearing)
    }

    // Backup & Data Export / Restore
    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val dhikrs = repository.getAllDhikrsSnapshot().filter { it.isCustom }
            val logs = repository.getAllLogsSnapshot()
            val playlists = repository.getCustomPlaylistsSnapshot()
            val json = BackupManager.createBackupJson(dhikrs, logs, playlists)
            onResult(json)
        }
    }

    fun exportHistoryCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val logs = repository.getAllLogsSnapshot()
            val csv = BackupManager.createHistoryCsv(logs)
            onResult(csv)
        }
    }

    fun restoreBackupJson(jsonString: String, onComplete: (RestoreResult) -> Unit) {
        viewModelScope.launch {
            val result = BackupManager.parseBackupJson(jsonString)
            if (result.success) {
                repository.restoreData(
                    items = result.restoredDhikrs,
                    logs = result.restoredLogs,
                    playlists = result.restoredPlaylists
                )
            }
            onComplete(result)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioHapticManager.destroy()
        compassSensorManager.stopListening()
    }
}
