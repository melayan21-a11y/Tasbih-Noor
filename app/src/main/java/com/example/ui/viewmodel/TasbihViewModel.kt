package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TasbihDatabase
import com.example.data.dao.DhikrDailyStat
import com.example.data.dao.DhikrTypeStat
import com.example.data.model.DhikrItem
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
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.util.AudioHapticManager
import com.example.util.HapticIntensity
import com.example.util.SoundEffectType
import com.example.util.TasbihPreferences
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
    private val repository = TasbihRepository(database.dhikrDao(), database.dhikrLogDao())

    val allDhikrs: StateFlow<List<DhikrItem>> = repository.allDhikrs
        .map { list -> list.distinctBy { it.transliteration.trim().lowercase() } }
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

    // After Salah Tasbih (33 SubhanAllah -> 33 Alhamdulillah -> 33 Allahu Akbar -> 1 Seal = 100)
    private val _isSalahSetMode = MutableStateFlow(preferences.isSalahSetMode)
    val isSalahSetMode: StateFlow<Boolean> = _isSalahSetMode.asStateFlow()

    private val _salahSetStage = MutableStateFlow(SalahSetStage.SUBHANALLAH)
    val salahSetStage: StateFlow<SalahSetStage> = _salahSetStage.asStateFlow()

    init {
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

    // Active Dhikr & Counter State
    private val _selectedDhikr = MutableStateFlow<DhikrItem?>(null)
    val selectedDhikr: StateFlow<DhikrItem?> = _selectedDhikr.asStateFlow()

    private val _currentCount = MutableStateFlow(0)
    val currentCount: StateFlow<Int> = _currentCount.asStateFlow()

    private val _lapsCompleted = MutableStateFlow(0)
    val lapsCompleted: StateFlow<Int> = _lapsCompleted.asStateFlow()

    private val _targetCount = MutableStateFlow(preferences.savedTargetCount)
    val targetCount: StateFlow<Int> = _targetCount.asStateFlow()

    // Preferences & Modes - Default to beautiful Night Noor Theme
    private val _isNightMode = MutableStateFlow(preferences.isNightMode)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _beadMaterial = MutableStateFlow(preferences.beadMaterial)
    val beadMaterial: StateFlow<BeadMaterial> = _beadMaterial.asStateFlow()

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

    // Auto-Guide & Voice Recitation
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

    fun startSalahSetMode() {
        _isSalahSetMode.value = true
        preferences.isSalahSetMode = true
        _salahSetStage.value = SalahSetStage.SUBHANALLAH
        _currentCount.value = 0
        _targetCount.value = 33
        val subhanAllah = allDhikrs.value.find { it.id == 1L || it.transliteration.contains("SubhanAllah", ignoreCase = true) }
        if (subhanAllah != null) {
            _selectedDhikr.value = subhanAllah
        }
        stopAutoGuide()
    }

    fun exitSalahSetMode() {
        _isSalahSetMode.value = false
        preferences.isSalahSetMode = false
        _salahSetStage.value = SalahSetStage.SUBHANALLAH
        _currentCount.value = 0
        val savedId = preferences.selectedDhikrId
        val matching = allDhikrs.value.find { it.id == savedId } ?: allDhikrs.value.firstOrNull()
        _selectedDhikr.value = matching
        _targetCount.value = matching?.defaultTarget ?: 33
    }

    fun selectDhikr(dhikr: DhikrItem) {
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
        if (_isSalahSetMode.value) {
            val nextStage = when (_salahSetStage.value) {
                SalahSetStage.SUBHANALLAH -> SalahSetStage.ALHAMDULILLAH
                SalahSetStage.ALHAMDULILLAH -> SalahSetStage.ALLAHU_AKBAR
                SalahSetStage.ALLAHU_AKBAR -> SalahSetStage.TAHLIL_SEAL
                SalahSetStage.TAHLIL_SEAL -> {
                    exitSalahSetMode()
                    return
                }
            }
            _salahSetStage.value = nextStage
            _currentCount.value = 0
            _targetCount.value = nextStage.target
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
        if (_isSalahSetMode.value) {
            val prevStage = when (_salahSetStage.value) {
                SalahSetStage.SUBHANALLAH -> return
                SalahSetStage.ALHAMDULILLAH -> SalahSetStage.SUBHANALLAH
                SalahSetStage.ALLAHU_AKBAR -> SalahSetStage.ALHAMDULILLAH
                SalahSetStage.TAHLIL_SEAL -> SalahSetStage.ALLAHU_AKBAR
            }
            _salahSetStage.value = prevStage
            _currentCount.value = 0
            _targetCount.value = prevStage.target
            return
        }
        val list = allDhikrs.value
        if (list.isEmpty()) return
        val currentId = _selectedDhikr.value?.id
        val currentIndex = list.indexOfFirst { it.id == currentId }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        selectDhikr(list[prevIndex])
    }

    fun onBeadPulled() {
        if (_isSalahSetMode.value) {
            audioHapticManager.playBeadSound(_soundType.value)
            audioHapticManager.triggerBeadHaptic(_hapticIntensity.value)

            val stage = _salahSetStage.value
            val newCount = _currentCount.value + 1

            // Record in database
            val dhikrToRecord = when (stage) {
                SalahSetStage.SUBHANALLAH -> allDhikrs.value.find { it.id == 1L }
                SalahSetStage.ALHAMDULILLAH -> allDhikrs.value.find { it.id == 2L }
                SalahSetStage.ALLAHU_AKBAR -> allDhikrs.value.find { it.id == 3L }
                SalahSetStage.TAHLIL_SEAL -> allDhikrs.value.find { it.id == 5L }
            } ?: _selectedDhikr.value ?: allDhikrs.value.firstOrNull()

            if (dhikrToRecord != null) {
                viewModelScope.launch {
                    repository.recordDhikrCount(dhikrToRecord, 1)
                }
            }

            if (newCount >= stage.target) {
                audioHapticManager.triggerTargetReachedHaptic()
                when (stage) {
                    SalahSetStage.SUBHANALLAH -> {
                        _salahSetStage.value = SalahSetStage.ALHAMDULILLAH
                        _currentCount.value = 0
                        _targetCount.value = 33
                        val alhamd = allDhikrs.value.find { it.id == 2L }
                        if (alhamd != null) _selectedDhikr.value = alhamd
                    }
                    SalahSetStage.ALHAMDULILLAH -> {
                        _salahSetStage.value = SalahSetStage.ALLAHU_AKBAR
                        _currentCount.value = 0
                        _targetCount.value = 33
                        val akbar = allDhikrs.value.find { it.id == 3L }
                        if (akbar != null) _selectedDhikr.value = akbar
                    }
                    SalahSetStage.ALLAHU_AKBAR -> {
                        _salahSetStage.value = SalahSetStage.TAHLIL_SEAL
                        _currentCount.value = 0
                        _targetCount.value = 1
                        val tahlil = allDhikrs.value.find { it.id == 5L }
                        if (tahlil != null) _selectedDhikr.value = tahlil
                    }
                    SalahSetStage.TAHLIL_SEAL -> {
                        _lapsCompleted.value += 1
                        _salahSetStage.value = SalahSetStage.SUBHANALLAH
                        _currentCount.value = 0
                        _targetCount.value = 33
                        val subhan = allDhikrs.value.find { it.id == 1L }
                        if (subhan != null) _selectedDhikr.value = subhan
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

        val newCount = _currentCount.value + 1
        val target = _targetCount.value

        if (target > 0 && newCount >= target) {
            _currentCount.value = 0
            _lapsCompleted.value += 1
            audioHapticManager.triggerTargetReachedHaptic()
        } else {
            _currentCount.value = newCount
        }

        // Record in database
        viewModelScope.launch {
            repository.recordDhikrCount(dhikr, 1)
        }
    }

    fun undoLastBead() {
        if (_isSalahSetMode.value) {
            if (_currentCount.value > 0) {
                _currentCount.value -= 1
            } else {
                when (_salahSetStage.value) {
                    SalahSetStage.TAHLIL_SEAL -> {
                        _salahSetStage.value = SalahSetStage.ALLAHU_AKBAR
                        _currentCount.value = 32
                        _targetCount.value = 33
                        val akbar = allDhikrs.value.find { it.id == 3L }
                        if (akbar != null) _selectedDhikr.value = akbar
                    }
                    SalahSetStage.ALLAHU_AKBAR -> {
                        _salahSetStage.value = SalahSetStage.ALHAMDULILLAH
                        _currentCount.value = 32
                        _targetCount.value = 33
                        val alhamd = allDhikrs.value.find { it.id == 2L }
                        if (alhamd != null) _selectedDhikr.value = alhamd
                    }
                    SalahSetStage.ALHAMDULILLAH -> {
                        _salahSetStage.value = SalahSetStage.SUBHANALLAH
                        _currentCount.value = 32
                        _targetCount.value = 33
                        val subhan = allDhikrs.value.find { it.id == 1L }
                        if (subhan != null) _selectedDhikr.value = subhan
                    }
                    SalahSetStage.SUBHANALLAH -> {
                        if (_lapsCompleted.value > 0) {
                            _lapsCompleted.value -= 1
                            _salahSetStage.value = SalahSetStage.TAHLIL_SEAL
                            _currentCount.value = 0
                            _targetCount.value = 1
                            val tahlil = allDhikrs.value.find { it.id == 5L }
                            if (tahlil != null) _selectedDhikr.value = tahlil
                        }
                    }
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
        if (_isSalahSetMode.value) {
            _salahSetStage.value = SalahSetStage.SUBHANALLAH
            _currentCount.value = 0
            _targetCount.value = 33
            val subhan = allDhikrs.value.find { it.id == 1L }
            if (subhan != null) _selectedDhikr.value = subhan
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

    fun setNightMode(enabled: Boolean) {
        _isNightMode.value = enabled
        preferences.isNightMode = enabled
    }

    fun setBeadMaterial(material: BeadMaterial) {
        _beadMaterial.value = material
        preferences.beadMaterial = material
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
            val json = BackupManager.createBackupJson(dhikrs, logs)
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
                repository.restoreData(result.restoredDhikrs, result.restoredLogs)
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
