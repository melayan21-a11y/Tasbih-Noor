package com.example.data.repository

import com.example.data.dao.DhikrDailyStat
import com.example.data.dao.DhikrDao
import com.example.data.dao.DhikrLogDao
import com.example.data.dao.DhikrTypeStat
import com.example.data.model.DhikrItem
import com.example.data.model.DhikrLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasbihRepository(
    private val dhikrDao: DhikrDao,
    private val dhikrLogDao: DhikrLogDao
) {
    val allDhikrs: Flow<List<DhikrItem>> = dhikrDao.getAllDhikrItems()
    val dailyTotals: Flow<List<DhikrDailyStat>> = dhikrLogDao.getDailyTotals()
    val typeStats: Flow<List<DhikrTypeStat>> = dhikrLogDao.getStatsByDhikrType()
    val lifetimeTotal: Flow<Int> = dhikrLogDao.getLifetimeTotal()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getTodayTotal(): Flow<Int> {
        return dhikrLogDao.getTodayTotal(getTodayDateString())
    }

    suspend fun recordDhikrCount(dhikr: DhikrItem, count: Int) {
        if (count <= 0) return
        val log = DhikrLog(
            dateString = getTodayDateString(),
            dhikrId = dhikr.id,
            dhikrTitle = dhikr.transliteration,
            count = count
        )
        dhikrLogDao.insertLog(log)
    }

    suspend fun addCustomDhikr(item: DhikrItem): Long {
        return dhikrDao.insertDhikr(item)
    }

    suspend fun deleteDhikr(item: DhikrItem) {
        dhikrDao.deleteDhikr(item)
    }

    suspend fun updateDhikr(item: DhikrItem) {
        dhikrDao.updateDhikr(item)
    }

    suspend fun ensureDefaultAdhkarPopulated() {
        dhikrDao.removeDuplicates()
        if (dhikrDao.countDhikrItems() == 0) {
            com.example.data.TasbihDatabase.populateInitialAdhkar(dhikrDao)
        }
    }

    fun calculateStreakFlow(): Flow<StreakInfo> {
        return dhikrLogDao.getActiveDates().map { activeDatesList ->
            calculateStreak(activeDatesList.toSet())
        }
    }

    private fun calculateStreak(activeDates: Set<String>): StreakInfo {
        if (activeDates.isEmpty()) return StreakInfo(currentStreak = 0, longestStreak = 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        // Check if user did dhikr today
        var currentStreak = 0
        if (activeDates.contains(todayStr)) {
            currentStreak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday: if user did dhikr yesterday, current streak is still alive
            val checkCal = Calendar.getInstance()
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(checkCal.time)
            if (activeDates.contains(yesterdayStr)) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return StreakInfo(currentStreak = 0, longestStreak = calculateLongest(activeDates, sdf))
            }
        }

        // Count consecutive days backward
        while (true) {
            val dateStr = sdf.format(cal.time)
            if (activeDates.contains(dateStr)) {
                if (dateStr != todayStr) {
                    currentStreak++
                }
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        val longest = maxOf(currentStreak, calculateLongest(activeDates, sdf))
        return StreakInfo(currentStreak = currentStreak, longestStreak = longest)
    }

    private fun calculateLongest(activeDates: Set<String>, sdf: SimpleDateFormat): Int {
        if (activeDates.isEmpty()) return 0
        val sortedDates = activeDates.sorted()
        var maxStreak = 1
        var runningStreak = 1

        val calPrev = Calendar.getInstance()
        val calCurr = Calendar.getInstance()

        for (i in 1 until sortedDates.size) {
            try {
                calPrev.time = sdf.parse(sortedDates[i - 1]) ?: continue
                calCurr.time = sdf.parse(sortedDates[i]) ?: continue
                calPrev.add(Calendar.DAY_OF_YEAR, 1)

                if (calPrev.get(Calendar.YEAR) == calCurr.get(Calendar.YEAR) &&
                    calPrev.get(Calendar.DAY_OF_YEAR) == calCurr.get(Calendar.DAY_OF_YEAR)
                ) {
                    runningStreak++
                    if (runningStreak > maxStreak) maxStreak = runningStreak
                } else {
                    runningStreak = 1
                }
            } catch (e: Exception) {
                runningStreak = 1
            }
        }
        return maxStreak
    }
}

data class StreakInfo(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)
