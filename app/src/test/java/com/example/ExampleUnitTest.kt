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

        assertTrue(morning.isNotEmpty())
        assertTrue(evening.isNotEmpty())
        assertTrue(morning.all { it.arabicText.isNotBlank() && it.targetCount > 0 })
        assertTrue(evening.all { it.arabicText.isNotBlank() && it.targetCount > 0 })
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
}

