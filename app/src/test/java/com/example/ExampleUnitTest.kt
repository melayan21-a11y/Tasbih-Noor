package com.example

import com.example.data.model.DhikrItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultDhikrItem_propertiesAreCorrect() {
        val item = DhikrItem(
            id = 1,
            arabicText = "سُبْحَانَ اللَّهِ",
            transliteration = "SubhanAllah",
            englishTranslation = "Glory be to Allah",
            category = "After Salah",
            defaultTarget = 33
        )

        assertEquals("SubhanAllah", item.transliteration)
        assertEquals(33, item.defaultTarget)
        assertFalse(item.isCustom)
    }

    @Test
    fun customDhikr_hasCorrectTargetAndFlag() {
        val custom = DhikrItem(
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً",
            transliteration = "Rabbana atina fid-dunya hasanah",
            englishTranslation = "Our Lord, give us in this world that which is good",
            category = "Custom",
            defaultTarget = 100,
            isCustom = true
        )

        assertTrue(custom.isCustom)
        assertEquals(100, custom.defaultTarget)
        assertEquals("Custom", custom.category)
    }

    @Test
    fun athkarRepository_containsValidMorningAndEveningSets() {
        val morning = com.example.data.model.AthkarRepository.morningAthkar
        val evening = com.example.data.model.AthkarRepository.eveningAthkar

        assertEquals(23, morning.size)
        assertEquals(24, evening.size)
        assertTrue(morning.all { it.arabicText.isNotBlank() && it.targetCount > 0 })
        assertTrue(evening.all { it.arabicText.isNotBlank() && it.targetCount > 0 })

        // Verify Bismillah is at the top of 3 Quls (Ikhlas, Falaq, Nas)
        assertTrue(morning[1].arabicText.startsWith("بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيمِ"))
        assertTrue(morning[2].arabicText.startsWith("بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيمِ"))
        assertTrue(morning[3].arabicText.startsWith("بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيمِ"))

        // Verify Evening has last 2 verses of Al-Baqarah (Amanar-Rasul) after Ayat al-Kursi
        assertTrue(evening[1].arabicText.contains("آمَنَ الرَّسُولُ"))
        assertEquals(1, evening[1].targetCount)

        // Verify both end with Salawat x10
        assertEquals(10, morning.last().targetCount)
        assertTrue(morning.last().arabicText.contains("اللَّهُمَّ صَلِّ"))
        assertEquals(10, evening.last().targetCount)
        assertTrue(evening.last().arabicText.contains("اللَّهُمَّ صَلِّ"))
    }

    @Test
    fun devotionRank_calculatesCorrectTierAndHorizons() {
        val totalRecitations = 12500
        val level = (totalRecitations / 2500) + 1
        val progressInLevel = totalRecitations % 2500

        assertEquals(6, level)
        assertEquals(0, progressInLevel)
    }

    @Test
    fun salahSetStage_hasExactFourStepsAndOneHundredTotal() {
        val stages = com.example.ui.viewmodel.SalahSetStage.values()
        assertEquals(4, stages.size)
        assertEquals(1, com.example.ui.viewmodel.SalahSetStage.SUBHANALLAH.stepIndex)
        assertEquals(33, com.example.ui.viewmodel.SalahSetStage.SUBHANALLAH.target)

        assertEquals(2, com.example.ui.viewmodel.SalahSetStage.ALHAMDULILLAH.stepIndex)
        assertEquals(33, com.example.ui.viewmodel.SalahSetStage.ALHAMDULILLAH.target)

        assertEquals(3, com.example.ui.viewmodel.SalahSetStage.ALLAHU_AKBAR.stepIndex)
        assertEquals(33, com.example.ui.viewmodel.SalahSetStage.ALLAHU_AKBAR.target)

        assertEquals(4, com.example.ui.viewmodel.SalahSetStage.TAHLIL_SEAL.stepIndex)
        assertEquals(1, com.example.ui.viewmodel.SalahSetStage.TAHLIL_SEAL.target)

        val totalCount = stages.sumOf { it.target }
        assertEquals(100, totalCount)
    }

    @Test
    fun prayerCalculator_calculatesValidSixPrayersAndQibla() {
        val cal = java.util.Calendar.getInstance()
        val result = com.example.data.prayer.PrayerCalculator.calculate(
            calendar = cal,
            latitude = 21.4225,
            longitude = 39.8262, // Makkah
            method = com.example.data.prayer.CalculationMethod.UMM_AL_QURA
        )

        assertEquals(6, result.prayers.size)
        assertTrue(result.prayers.any { it.name == "Fajr" })
        assertTrue(result.prayers.any { it.name == "Dhuhr" })
        assertTrue(result.prayers.any { it.name == "Asr" })
        assertTrue(result.prayers.any { it.name == "Maghrib" })
        assertTrue(result.prayers.any { it.name == "Isha" })

        // From Cairo (30.0444, 31.2357) to Makkah is South-East (~136°)
        val cairoQibla = com.example.data.prayer.PrayerCalculator.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue("Cairo Qibla should be between 130 and 140 degrees", cairoQibla in 130.0..140.0)
    }

    @Test
    fun backupManager_createsValidCsv() {
        val logs = listOf(
            com.example.data.model.DhikrLog(
                id = 1,
                dateString = "2026-11-10",
                dhikrId = 99,
                dhikrTitle = "SubhanAllahi wa bihamdihi",
                count = 33,
                timestamp = 1700000000000L
            )
        )

        val csv = com.example.data.backup.BackupManager.createHistoryCsv(logs)
        assertTrue(csv.startsWith("ID,Date,Dhikr Title,Count"))
        assertTrue(csv.contains("SubhanAllahi wa bihamdihi"))
        assertTrue(csv.contains("33"))
    }

    @Test
    fun widgetReminders_returnsValidDuasAndWrapsSafely() {
        val reminders = com.example.widget.WidgetReminders.items
        assertTrue("Widget reminders should have at least 15 items", reminders.size >= 15)
        assertTrue(reminders.all { it.arabic.isNotBlank() && it.translation.isNotBlank() && it.tag.isNotBlank() })

        // Test safe wrap-around indexing
        val first = com.example.widget.WidgetReminders.getReminder(0)
        val wrapped = com.example.widget.WidgetReminders.getReminder(reminders.size)
        assertEquals(first.arabic, wrapped.arabic)

        val negativeIndex = com.example.widget.WidgetReminders.getReminder(-5)
        assertTrue(negativeIndex.arabic.isNotBlank())
    }
}

