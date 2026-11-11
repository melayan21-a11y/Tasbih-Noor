package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension

class TasbihPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var soundType: SoundEffectType
        get() {
            val name = prefs.getString(KEY_SOUND_TYPE, SoundEffectType.WOOD_CLICK.name)
            return try {
                SoundEffectType.valueOf(name ?: SoundEffectType.WOOD_CLICK.name)
            } catch (e: Exception) {
                SoundEffectType.WOOD_CLICK
            }
        }
        set(value) {
            prefs.edit().putString(KEY_SOUND_TYPE, value.name).apply()
        }

    var hapticIntensity: HapticIntensity
        get() {
            val name = prefs.getString(KEY_HAPTIC_INTENSITY, HapticIntensity.MEDIUM.name)
            return try {
                HapticIntensity.valueOf(name ?: HapticIntensity.MEDIUM.name)
            } catch (e: Exception) {
                HapticIntensity.MEDIUM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_HAPTIC_INTENSITY, value.name).apply()
        }

    var beadTension: BeadTension
        get() {
            val name = prefs.getString(KEY_BEAD_TENSION, BeadTension.STANDARD.name)
            return try {
                BeadTension.valueOf(name ?: BeadTension.STANDARD.name)
            } catch (e: Exception) {
                BeadTension.STANDARD
            }
        }
        set(value) {
            prefs.edit().putString(KEY_BEAD_TENSION, value.name).apply()
        }

    var singleBeadLock: Boolean
        get() = prefs.getBoolean(KEY_SINGLE_BEAD_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_SINGLE_BEAD_LOCK, value).apply()

    var beadMaterial: BeadMaterial
        get() {
            val name = prefs.getString(KEY_BEAD_MATERIAL, BeadMaterial.OLIVE_WOOD.name)
            return try {
                BeadMaterial.valueOf(name ?: BeadMaterial.OLIVE_WOOD.name)
            } catch (e: Exception) {
                BeadMaterial.OLIVE_WOOD
            }
        }
        set(value) {
            prefs.edit().putString(KEY_BEAD_MATERIAL, value.name).apply()
        }

    var isNightMode: Boolean
        get() = prefs.getBoolean(KEY_IS_NIGHT_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_NIGHT_MODE, value).apply()

    var keepScreenAwake: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_AWAKE, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_AWAKE, value).apply()

    var selectedDhikrId: Long
        get() = prefs.getLong(KEY_SELECTED_DHIKR_ID, 1L)
        set(value) = prefs.edit().putLong(KEY_SELECTED_DHIKR_ID, value).apply()

    var savedTargetCount: Int
        get() = prefs.getInt(KEY_TARGET_COUNT, 33)
        set(value) = prefs.edit().putInt(KEY_TARGET_COUNT, value).apply()

    var isSalahSetMode: Boolean
        get() = prefs.getBoolean(KEY_IS_SALAH_SET_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_SALAH_SET_MODE, value).apply()

    // Prayer Times & Qibla Settings
    var selectedCityName: String
        get() = prefs.getString(KEY_CITY_NAME, "Makkah") ?: "Makkah"
        set(value) = prefs.edit().putString(KEY_CITY_NAME, value).apply()

    var selectedCityCountry: String
        get() = prefs.getString(KEY_CITY_COUNTRY, "Saudi Arabia") ?: "Saudi Arabia"
        set(value) = prefs.edit().putString(KEY_CITY_COUNTRY, value).apply()

    var selectedLatitude: Double
        get() = prefs.getFloat(KEY_LATITUDE, 21.4225f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_LATITUDE, value.toFloat()).apply()

    var selectedLongitude: Double
        get() = prefs.getFloat(KEY_LONGITUDE, 39.8262f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_LONGITUDE, value.toFloat()).apply()

    var selectedCalculationMethod: String
        get() = prefs.getString(KEY_CALC_METHOD, "UMM_AL_QURA") ?: "UMM_AL_QURA"
        set(value) = prefs.edit().putString(KEY_CALC_METHOD, value).apply()

    var selectedAsrJuristic: String
        get() = prefs.getString(KEY_ASR_JURISTIC, "STANDARD") ?: "STANDARD"
        set(value) = prefs.edit().putString(KEY_ASR_JURISTIC, value).apply()

    // Quick Widget Cached Count
    var widgetTodayCount: Int
        get() = prefs.getInt(KEY_WIDGET_TODAY_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_WIDGET_TODAY_COUNT, value).apply()

    var widgetStreakDays: Int
        get() = prefs.getInt(KEY_WIDGET_STREAK_DAYS, 0)
        set(value) = prefs.edit().putInt(KEY_WIDGET_STREAK_DAYS, value).apply()

    var widgetReminderIndex: Int
        get() = prefs.getInt(KEY_WIDGET_REMINDER_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_WIDGET_REMINDER_INDEX, value).apply()

    companion object {
        private const val PREFS_NAME = "tasbih_user_preferences"
        private const val KEY_SOUND_TYPE = "pref_sound_type"
        private const val KEY_HAPTIC_INTENSITY = "pref_haptic_intensity"
        private const val KEY_BEAD_TENSION = "pref_bead_tension"
        private const val KEY_SINGLE_BEAD_LOCK = "pref_single_bead_lock"
        private const val KEY_BEAD_MATERIAL = "pref_bead_material"
        private const val KEY_IS_NIGHT_MODE = "pref_is_night_mode"
        private const val KEY_KEEP_SCREEN_AWAKE = "pref_keep_screen_awake"
        private const val KEY_SELECTED_DHIKR_ID = "pref_selected_dhikr_id"
        private const val KEY_TARGET_COUNT = "pref_target_count"
        private const val KEY_IS_SALAH_SET_MODE = "pref_is_salah_set_mode"
        private const val KEY_CITY_NAME = "pref_city_name"
        private const val KEY_CITY_COUNTRY = "pref_city_country"
        private const val KEY_LATITUDE = "pref_latitude"
        private const val KEY_LONGITUDE = "pref_longitude"
        private const val KEY_CALC_METHOD = "pref_calc_method"
        private const val KEY_ASR_JURISTIC = "pref_asr_juristic"
        private const val KEY_WIDGET_TODAY_COUNT = "pref_widget_today_count"
        private const val KEY_WIDGET_STREAK_DAYS = "pref_widget_streak_days"
        private const val KEY_WIDGET_REMINDER_INDEX = "pref_widget_reminder_index"
    }
}

