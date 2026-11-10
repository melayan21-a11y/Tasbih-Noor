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
import com.example.notifications.DhikrReminderManager
import com.example.notifications.ReminderSetting
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.util.AudioHapticManager
import com.example.util.HapticIntensity
import com.example.util.SoundEffectType
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

class TasbihViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TasbihRepository
    val audioHapticManager = AudioHapticManager(application.applicationContext)
    val reminderManager = DhikrReminderManager(application.applicationContext)

    init {
        val database = TasbihDatabase.getDatabase(application.applicationContext, viewModelScope)
        repository = TasbihRepository(database.dhikrDao(), database.dhikrLogDao())
        viewModelScope.launch {
            repository.ensureDefaultAdhkarPopulated()
        }
    }

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

    // Active Dhikr & Counter State
    private val _selectedDhikr = MutableStateFlow<DhikrItem?>(null)
    val selectedDhikr: StateFlow<DhikrItem?> = _selectedDhikr.asStateFlow()

    private val _currentCount = MutableStateFlow(0)
    val currentCount: StateFlow<Int> = _currentCount.asStateFlow()

    private val _lapsCompleted = MutableStateFlow(0)
    val lapsCompleted: StateFlow<Int> = _lapsCompleted.asStateFlow()

    private val _targetCount = MutableStateFlow(33)
    val targetCount: StateFlow<Int> = _targetCount.asStateFlow()

    // Preferences & Modes - Default to beautiful Night Noor Theme
    private val _isNightMode = MutableStateFlow(true)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _beadMaterial = MutableStateFlow(BeadMaterial.OLIVE_WOOD)
    val beadMaterial: StateFlow<BeadMaterial> = _beadMaterial.asStateFlow()

    private val _soundType = MutableStateFlow(SoundEffectType.WOOD_CLICK)
    val soundType: StateFlow<SoundEffectType> = _soundType.asStateFlow()

    private val _hapticIntensity = MutableStateFlow(HapticIntensity.MEDIUM)
    val hapticIntensity: StateFlow<HapticIntensity> = _hapticIntensity.asStateFlow()

    // Bead String Tension & Sensitivity Control
    private val _beadTension = MutableStateFlow(BeadTension.STANDARD)
    val beadTension: StateFlow<BeadTension> = _beadTension.asStateFlow()

    private val _singleBeadLock = MutableStateFlow(true)
    val singleBeadLock: StateFlow<Boolean> = _singleBeadLock.asStateFlow()

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

    fun selectDhikr(dhikr: DhikrItem) {
        _selectedDhikr.value = dhikr
        _targetCount.value = dhikr.defaultTarget
        _currentCount.value = 0
        _lapsCompleted.value = 0
        stopAutoGuide()
    }

    fun onBeadPulled() {
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
        if (_currentCount.value > 0) {
            _currentCount.value -= 1
        } else if (_lapsCompleted.value > 0 && _targetCount.value > 0) {
            _lapsCompleted.value -= 1
            _currentCount.value = _targetCount.value - 1
        }
    }

    fun resetCounter() {
        _currentCount.value = 0
        _lapsCompleted.value = 0
        stopAutoGuide()
    }

    fun setTargetCount(target: Int) {
        _targetCount.value = target
        _currentCount.value = 0
    }

    fun setNightMode(enabled: Boolean) {
        _isNightMode.value = enabled
    }

    fun setBeadMaterial(material: BeadMaterial) {
        _beadMaterial.value = material
    }

    fun setSoundType(type: SoundEffectType) {
        _soundType.value = type
        if (type != SoundEffectType.MUTE) {
            audioHapticManager.playBeadSound(type)
        }
    }

    fun setHapticIntensity(intensity: HapticIntensity) {
        _hapticIntensity.value = intensity
        audioHapticManager.triggerBeadHaptic(intensity)
    }

    fun setBeadTension(tension: BeadTension) {
        _beadTension.value = tension
    }

    fun setSingleBeadLock(enabled: Boolean) {
        _singleBeadLock.value = enabled
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

    override fun onCleared() {
        super.onCleared()
        audioHapticManager.destroy()
    }
}
