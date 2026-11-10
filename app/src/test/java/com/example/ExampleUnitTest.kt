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
}
