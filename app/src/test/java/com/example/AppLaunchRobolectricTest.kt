package com.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.util.AudioHapticManager
import com.example.util.SoundEffectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppLaunchRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun audioHapticManager_generatesValidWavAndPlaysAllSoundEffects() {
        val manager = AudioHapticManager(ApplicationProvider.getApplicationContext())
        val pcm = manager.synthesizeMultiModalBead(
            f1 = 680.0,
            f2 = 1260.0,
            f3 = 2120.0,
            decay1 = 36.0,
            decay2 = 62.0,
            decay3 = 110.0,
            transientStrength = 0.34,
            durationMs = 85,
            seed = 101
        )
        assertTrue(pcm.size > 4000)

        val wav = manager.buildWavBytes(pcm)
        assertEquals(pcm.size + 44, wav.size)
        assertEquals('R'.code.toByte(), wav[0])
        assertEquals('I'.code.toByte(), wav[1])
        assertEquals('F'.code.toByte(), wav[2])
        assertEquals('F'.code.toByte(), wav[3])
        assertEquals('W'.code.toByte(), wav[8])
        assertEquals('A'.code.toByte(), wav[9])
        assertEquals('V'.code.toByte(), wav[10])
        assertEquals('E'.code.toByte(), wav[11])

        SoundEffectType.entries.forEach { sound ->
            manager.playBeadSound(sound)
        }
        manager.destroy()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mainActivity_launchesAndNavigatesAllTabsWithoutCrashing() {
        composeTestRule.waitForIdle()

        // Interact with main Tasbih bead screen
        composeTestRule.onNodeWithTag("center_counter_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("night_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("night_mode_toggle").performClick()
        composeTestRule.waitForIdle()

        // Navigate across all tabs
        composeTestRule.onNodeWithTag("nav_tab_library").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_qibla").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_analytics").performClick()
        composeTestRule.waitForIdle()

        // Open Settings and interact with Bead Materials, Islamic Bead Craft, Sound, Voice, and Themes
        composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("bead_material_black_onyx"))
        composeTestRule.onNodeWithTag("bead_material_black_onyx").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("bead_design_rub_al_hizb"))
        composeTestRule.onNodeWithTag("bead_design_rub_al_hizb").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("sound_chip_pearl_tap"))
        composeTestRule.onNodeWithTag("sound_chip_pearl_tap").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("voice_lang_english"))
        composeTestRule.onNodeWithTag("voice_lang_english").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("voice_lang_arabic").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("cadence_reflective"))
        composeTestRule.onNodeWithTag("cadence_reflective").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cadence_natural").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("theme_chip_kaaba_kiswah"))
        composeTestRule.onNodeWithTag("theme_chip_kaaba_kiswah").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("theme_chip_andalusian_night"))
        composeTestRule.onNodeWithTag("theme_chip_andalusian_night").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("theme_chip_madinah_marble"))
        composeTestRule.onNodeWithTag("theme_chip_madinah_marble").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("theme_chip_ottoman_iznik"))
        composeTestRule.onNodeWithTag("theme_chip_ottoman_iznik").performClick()
        composeTestRule.waitForIdle()

        // Verify Library, Qibla, Analytics, and Tasbih render cleanly under Ottoman Iznik theme
        composeTestRule.onNodeWithTag("nav_tab_library").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_qibla").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_analytics").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_tab_tasbih").performClick()
        composeTestRule.waitForIdle()

        // Switch back to Settings and restore Midnight Emerald
        composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("theme_chip_midnight_emerald"))
        composeTestRule.onNodeWithTag("theme_chip_midnight_emerald").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_tasbih").performClick()
        composeTestRule.waitForIdle()
    }
}
