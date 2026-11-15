package com.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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

        // Open Settings and interact with new Sound, Voice, and Wear Sync controls
        composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
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

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("check_watch_btn"))
        composeTestRule.onNodeWithTag("check_watch_btn").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("install_on_watch_btn"))
        composeTestRule.onNodeWithTag("install_on_watch_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("watch_setup_guide_btn"))
        composeTestRule.onNodeWithTag("watch_setup_guide_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Got It").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_tasbih").performClick()
        composeTestRule.waitForIdle()
    }
}
