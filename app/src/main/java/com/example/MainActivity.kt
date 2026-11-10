package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AthkarType
import com.example.ui.screens.AthkarReaderScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DhikrLibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasbihMainScreen
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NightBackground
import com.example.ui.theme.TasbihTheme
import com.example.ui.viewmodel.TasbihViewModel

enum class MainTab {
    TASBIH,
    LIBRARY,
    ANALYTICS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TasbihViewModel = viewModel()
            val isNightMode by viewModel.isNightMode.collectAsState()
            val keepAwake by viewModel.keepScreenAwake.collectAsState()

            androidx.compose.runtime.LaunchedEffect(keepAwake) {
                if (keepAwake) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            TasbihTheme(darkTheme = isNightMode) {
                TasbihApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TasbihApp(viewModel: TasbihViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.TASBIH) }
    var activeAthkarType by remember { mutableStateOf<AthkarType?>(null) }
    val isNightMode by viewModel.isNightMode.collectAsState()

    // Handle back button: exit Athkar Reader if active, or return to Tasbih tab
    BackHandler(enabled = activeAthkarType != null || currentTab != MainTab.TASBIH) {
        if (activeAthkarType != null) {
            activeAthkarType = null
        } else {
            currentTab = MainTab.TASBIH
        }
    }

    if (activeAthkarType != null) {
        AthkarReaderScreen(
            viewModel = viewModel,
            initialType = activeAthkarType ?: AthkarType.MORNING,
            onBack = { activeAthkarType = null },
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        )
    } else {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = if (isNightMode) Color(0xFF061813) else Color.White,
                tonalElevation = 8.dp
            ) {
                // 1. Tasbih Bead Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.TASBIH,
                    onClick = { currentTab = MainTab.TASBIH },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.TASBIH) Icons.Filled.RadioButtonChecked else Icons.Outlined.Circle,
                            contentDescription = "Prayer Beads"
                        )
                    },
                    label = { Text("Tasbih", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isNightMode) Color(0xFF061813) else Color.White,
                        selectedTextColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        indicatorColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        unselectedIconColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        unselectedTextColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_tasbih")
                )

                // 2. Dhikr Library Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.LIBRARY,
                    onClick = { currentTab = MainTab.LIBRARY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.LIBRARY) Icons.AutoMirrored.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "Adhkar Library"
                        )
                    },
                    label = { Text("Adhkar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isNightMode) Color(0xFF061813) else Color.White,
                        selectedTextColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        indicatorColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        unselectedIconColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        unselectedTextColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_library")
                )

                // 3. Analytics Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.ANALYTICS,
                    onClick = { currentTab = MainTab.ANALYTICS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.ANALYTICS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Analytics"
                        )
                    },
                    label = { Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isNightMode) Color(0xFF061813) else Color.White,
                        selectedTextColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        indicatorColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        unselectedIconColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        unselectedTextColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_analytics")
                )

                // 4. Settings & Reminders Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { currentTab = MainTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isNightMode) Color(0xFF061813) else Color.White,
                        selectedTextColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        indicatorColor = if (isNightMode) GoldPrimary else EmeraldPrimary,
                        unselectedIconColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                        unselectedTextColor = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.TASBIH -> {
                    TasbihMainScreen(
                        viewModel = viewModel,
                        onNavigateToLibrary = { currentTab = MainTab.LIBRARY },
                        onOpenAthkarReader = { activeAthkarType = it }
                    )
                }
                MainTab.LIBRARY -> {
                    DhikrLibraryScreen(
                        viewModel = viewModel,
                        onDhikrSelected = {
                            currentTab = MainTab.TASBIH
                        },
                        onOpenAthkarReader = { activeAthkarType = it }
                    )
                }
                MainTab.ANALYTICS -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
    }
}
