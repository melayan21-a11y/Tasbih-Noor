package com.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
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

        val chimePcm = manager.synthesizeHarmonicChime(
            frequenciesHz = listOf(587.33, 880.0),
            noteOffsetSeconds = 0.085,
            durationMs = 460
        )
        assertTrue(chimePcm.size > 8000)
        assertEquals(0, chimePcm.size % 2)

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
        manager.playLapChime()
        manager.playMilestoneChime()
        manager.destroy()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mainActivity_launchesAndNavigatesAllTabsWithoutCrashing() {
        composeTestRule.waitForIdle()

        // Interact with main Tasbih bead screen, Daily Goal Dialog, Focus Mode, and Guided Set
        composeTestRule.onNodeWithTag("center_counter_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("today_count_badge").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("goal_preset_500").performClick()
        composeTestRule.onNodeWithTag("save_daily_goal_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("focus_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("night_mode_toggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("focus_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("night_mode_toggle").assertIsDisplayed()

        composeTestRule.onNodeWithTag("night_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("night_mode_toggle").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Adhkar Library, create a custom playlist, and launch a guided playlist
        composeTestRule.onNodeWithTag("nav_tab_library").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasTestTag("playlists_section"))
        composeTestRule.onNodeWithTag("create_playlist_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("playlist_name_input").performTextInput("Evening Peace")
        composeTestRule.onNodeWithTag("playlist_add_dhikr_1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("playlist_add_dhikr_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("save_playlist_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("playlist_card_2").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("playlist_stepper_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("center_counter_button").performClick()
        composeTestRule.onNodeWithTag("exit_playlist_chip").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_qibla").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_analytics").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasTestTag("daily_goal_card"))
        composeTestRule.onNodeWithTag("daily_goal_card").assertIsDisplayed()

        // Open Settings and interact with Bead Materials, Islamic Bead Craft, Sound, Chimes, Voice, and Themes
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

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("milestone_chime_switch"))
        composeTestRule.onNodeWithTag("milestone_chime_switch").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("milestone_chime_switch").performClick()
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
