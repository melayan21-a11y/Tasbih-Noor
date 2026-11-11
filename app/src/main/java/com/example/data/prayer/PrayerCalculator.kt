package com.example.data.prayer

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.*

enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0
) {
    UMM_AL_QURA("Umm al-Qura (Makkah)", 18.5, 0.0, 90),
    MUSLIM_WORLD_LEAGUE("Muslim World League", 18.0, 17.0),
    ISNA("ISNA (North America)", 15.0, 15.0),
    EGYPTIAN("Egyptian General Authority", 19.5, 17.5),
    DUBAI("Gulf / Dubai", 18.2, 18.2),
    KARACHI("Karachi (Univ. of Islamic Sci.)", 18.0, 18.0)
}

enum class JuristicMethod(val title: String, val shadowFactor: Double) {
    STANDARD("Shafi, Maliki, Hanbali (1x Shadow)", 1.0),
    HANAFI("Hanafi (2x Shadow)", 2.0)
}

data class PrayerTimeItem(
    val name: String,
    val arabicName: String,
    val timeMillis: Long,
    val timeFormatted: String,
    val isPassed: Boolean = false,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

data class PrayerTimesResult(
    val date: Date,
    val prayers: List<PrayerTimeItem>,
    val nextPrayer: PrayerTimeItem?,
    val timeRemainingMillis: Long,
    val qiblaBearing: Double,
    val distanceToMakkahKm: Double
)

object PrayerCalculator {

    const val KAABA_LAT = 21.422487
    const val KAABA_LNG = 39.826206

    /**
     * Calculates prayer times for a given date, coordinates, and calculation method.
     */
    fun calculate(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        method: CalculationMethod = CalculationMethod.UMM_AL_QURA,
        juristic: JuristicMethod = JuristicMethod.STANDARD,
        timeZone: TimeZone = calendar.timeZone
    ): PrayerTimesResult {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tzOffsetHours = timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        // Julian Day calculation
        val jd = julianDate(year, month, day) - (longitude / (15.0 * 24.0))
        val d = jd - 2451545.0

        // Sun's position
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d

        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0
        val sinDelta = sin(Math.toRadians(e)) * sin(Math.toRadians(l))
        val delta = Math.toDegrees(asin(sinDelta))

        // Equation of Time in hours
        val eqT = (q / 15.0) - ra

        // Solar Noon in hours
        val noon = fixHour(12.0 + tzOffsetHours - (longitude / 15.0) - eqT)

        // Sunrise & Sunset (center of sun 50 arcminutes = 0.833 degrees below horizon)
        val sunriseDiff = hourAngle(-0.8333, latitude, delta)
        val sunrise = noon - sunriseDiff
        val sunset = noon + sunriseDiff

        // Fajr
        val fajrDiff = hourAngle(-method.fajrAngle, latitude, delta)
        val fajr = noon - fajrDiff

        // Asr (shadow factor: 1.0 for standard, 2.0 for Hanafi)
        val asrAltitude = -Math.toDegrees(atan(1.0 / (juristic.shadowFactor + tan(Math.toRadians(abs(latitude - delta))))))
        val asrDiff = hourAngle(asrAltitude, latitude, delta)
        val asr = noon + asrDiff

        // Maghrib = Sunset
        val maghrib = sunset

        // Isha
        val isha = if (method.ishaIntervalMinutes > 0) {
            maghrib + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaDiff = hourAngle(-method.ishaAngle, latitude, delta)
            noon + ishaDiff
        }

        // Convert calculated hours into Calendar timestamp
        fun hoursToMillis(hours: Double): Long {
            val cal = calendar.clone() as Calendar
            val h = hours.toInt()
            val remMinutes = ((hours - h) * 60.0).roundToInt()
            cal.set(Calendar.HOUR_OF_DAY, h % 24)
            cal.set(Calendar.MINUTE, remMinutes % 60)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        val fajrMillis = hoursToMillis(fajr)
        val sunriseMillis = hoursToMillis(sunrise)
        val dhuhrMillis = hoursToMillis(noon)
        val asrMillis = hoursToMillis(asr)
        val maghribMillis = hoursToMillis(maghrib)
        val ishaMillis = hoursToMillis(isha)

        val nowMillis = System.currentTimeMillis()

        fun formatTime(millis: Long): String {
            val c = Calendar.getInstance().apply { timeInMillis = millis }
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val min = c.get(Calendar.MINUTE)
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            return String.format(java.util.Locale.getDefault(), "%02d:%02d %s", displayHour, min, amPm)
        }

        val rawList = listOf(
            PrayerTimeItem("Fajr", "الفجر", fajrMillis, formatTime(fajrMillis)),
            PrayerTimeItem("Sunrise", "الشروق", sunriseMillis, formatTime(sunriseMillis)),
            PrayerTimeItem("Dhuhr", "الظهر", dhuhrMillis, formatTime(dhuhrMillis)),
            PrayerTimeItem("Asr", "العصر", asrMillis, formatTime(asrMillis)),
            PrayerTimeItem("Maghrib", "المغرب", maghribMillis, formatTime(maghribMillis)),
            PrayerTimeItem("Isha", "العشاء", ishaMillis, formatTime(ishaMillis))
        )

        // Find next and current prayer
        val nextIndex = rawList.indexOfFirst { it.timeMillis > nowMillis }
        val nextPrayer = if (nextIndex != -1) rawList[nextIndex] else rawList.first() // If past Isha, next is tomorrow Fajr

        val timeRemaining = if (nextIndex != -1) {
            maxOf(0L, nextPrayer.timeMillis - nowMillis)
        } else {
            // Tomorrow Fajr
            maxOf(0L, (fajrMillis + 86400000L) - nowMillis)
        }

        val currentIndex = when {
            nextIndex == 0 -> -1 // Before Fajr
            nextIndex == -1 -> rawList.lastIndex // After Isha
            else -> nextIndex - 1
        }

        val enrichedList = rawList.mapIndexed { idx, item ->
            item.copy(
                isPassed = item.timeMillis <= nowMillis,
                isCurrent = idx == currentIndex,
                isNext = idx == nextIndex
            )
        }

        val qiblaBearing = calculateQiblaBearing(latitude, longitude)
        val distanceKm = calculateDistanceToMakkah(latitude, longitude)

        return PrayerTimesResult(
            date = calendar.time,
            prayers = enrichedList,
            nextPrayer = nextPrayer,
            timeRemainingMillis = timeRemaining,
            qiblaBearing = qiblaBearing,
            distanceToMakkahKm = distanceKm
        )
    }

    /**
     * Great Circle bearing to Kaaba (Makkah). Returns 0..360 degrees.
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val lat1 = Math.toRadians(latitude)
        val lng1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lng2 = Math.toRadians(KAABA_LNG)

        val deltaLng = lng2 - lng1
        val y = sin(deltaLng)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(deltaLng)
        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return (qibla * 10.0).roundToInt() / 10.0
    }

    /**
     * Haversine distance in kilometers to Makkah
     */
    fun calculateDistanceToMakkah(latitude: Double, longitude: Double): Double {
        val r = 6371.0 // Earth radius in km
        val lat1 = Math.toRadians(latitude)
        val lng1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lng2 = Math.toRadians(KAABA_LNG)

        val dLat = lat2 - lat1
        val dLng = lng2 - lng1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).roundToInt().toDouble()
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun hourAngle(altitude: Double, latitude: Double, delta: Double): Double {
        val altRad = Math.toRadians(altitude)
        val latRad = Math.toRadians(latitude)
        val deltaRad = Math.toRadians(delta)
        val cosH = (sin(altRad) - sin(latRad) * sin(deltaRad)) / (cos(latRad) * cos(deltaRad))
        val clampedCos = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCos)) / 15.0
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(h: Double): Double {
        var hour = h - 24.0 * floor(h / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }
}
