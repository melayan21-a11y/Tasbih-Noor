package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BeadDesign
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.ui.components.DraggablePrayerBeads
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.ArabicCalligraphyFontFamily
import com.example.ui.theme.DayEmeraldDeep
import com.example.ui.theme.DayGoldAccent
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalTasbihPalette
import com.example.data.model.AthkarType
import com.example.ui.theme.NightBackground
import com.example.ui.viewmodel.SalahSetStage
import com.example.ui.viewmodel.TasbihViewModel

private enum class SpiritualDayPhase(
    val label: String,
    val emoji: String,
    val athkarType: AthkarType,
    val athkarLabel: String,
    val dayOverlayTop: Color,
    val dayOverlayBottom: Color,
    val nightTint: Color
) {
    DAWN_MORNING(
        label = "Morning Noor",
        emoji = "🌅",
        athkarType = AthkarType.MORNING,
        athkarLabel = "Morning Adhkar",
        dayOverlayTop = Color(0xFFFFF8E8),
        dayOverlayBottom = Color(0xFFF3E7CB),
        nightTint = Color(0xFF061B1A)
    ),
    MIDDAY_DUHA(
        label = "Midday Light",
        emoji = "☀️",
        athkarType = AthkarType.MORNING,
        athkarLabel = "Morning Adhkar",
        dayOverlayTop = Color(0xFFFAF7F0),
        dayOverlayBottom = Color(0xFFEFE6D2),
        nightTint = Color(0xFF071914)
    ),
    AFTERNOON_EVENING(
        label = "Evening Noor",
        emoji = "🌇",
        athkarType = AthkarType.EVENING,
        athkarLabel = "Evening Adhkar",
        dayOverlayTop = Color(0xFFFBF3E6),
        dayOverlayBottom = Color(0xFFEADBC4),
        nightTint = Color(0xFF0B1C19)
    ),
    NIGHT_TRANQUILITY(
        label = "Night Calm",
        emoji = "🌙",
        athkarType = AthkarType.EVENING,
        athkarLabel = "Evening Adhkar",
        dayOverlayTop = Color(0xFFF6F4EE),
        dayOverlayBottom = Color(0xFFE6E0D3),
        nightTint = Color(0xFF041418)
    );

    companion object {
        fun fromHour(hour: Int): SpiritualDayPhase = when (hour) {
            in 4..11 -> DAWN_MORNING
            in 12..14 -> MIDDAY_DUHA
            in 15..19 -> AFTERNOON_EVENING
            else -> NIGHT_TRANQUILITY
        }
    }
}

@Composable
fun TasbihMainScreen(
    viewModel: TasbihViewModel,
    onNavigateToLibrary: () -> Unit,
    onOpenAthkarReader: (AthkarType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedDhikr by viewModel.selectedDhikr.collectAsState()
    val allDhikrs by viewModel.allDhikrs.collectAsState()
    val currentDhikr = selectedDhikr ?: allDhikrs.firstOrNull()

    val currentCount by viewModel.currentCount.collectAsState()
    val lapsCompleted by viewModel.lapsCompleted.collectAsState()
    val targetCount by viewModel.targetCount.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()

    val isSalahSetMode by viewModel.isSalahSetMode.collectAsState()
    val salahSetStage by viewModel.salahSetStage.collectAsState()
    val activePlaylist by viewModel.activePlaylist.collectAsState()
    val playlistSteps by viewModel.activePlaylistSteps.collectAsState()
    val playlistStepIdx by viewModel.activePlaylistStepIndex.collectAsState()

    val isInGuidedPlaylist = activePlaylist != null && playlistSteps.isNotEmpty()
    val currentPlaylistStep = if (isInGuidedPlaylist) {
        playlistSteps.getOrNull(playlistStepIdx.coerceIn(0, playlistSteps.lastIndex))
    } else null
    val playlistTotalCount = remember(playlistSteps) { playlistSteps.sumOf { it.targetCount } }
    val playlistCompletedBeforeCurrent = remember(playlistSteps, playlistStepIdx) {
        playlistSteps.take(playlistStepIdx).sumOf { it.targetCount }
    }
    val setCumulativeCount = playlistCompletedBeforeCurrent + currentCount

    val dailyGoalTarget by viewModel.dailyGoalTarget.collectAsState()
    val isFocusMode by viewModel.isFocusMode.collectAsState()
    val beadPullSequence by viewModel.beadPullSequence.collectAsState()
    val milestoneBanner by viewModel.milestoneBanner.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }

    val dailyGoalProgressRaw = if (dailyGoalTarget > 0) {
        (todayTotal.toFloat() / dailyGoalTarget.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val dailyGoalSweep by animateFloatAsState(
        targetValue = dailyGoalProgressRaw * 360f,
        animationSpec = tween(durationMillis = 500),
        label = "daily_goal_sweep"
    )
    val focusVeilAlpha by animateFloatAsState(
        targetValue = if (isFocusMode) 0.58f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "focus_veil_alpha"
    )

    val palette = LocalTasbihPalette.current
    val isNightMode by viewModel.isNightMode.collectAsState()
    val beadMaterial by viewModel.beadMaterial.collectAsState()
    val beadDesign by viewModel.beadDesign.collectAsState()
    val beadTension by viewModel.beadTension.collectAsState()
    val singleBeadLock by viewModel.singleBeadLock.collectAsState()
    val isAutoGuiding by viewModel.isAutoGuiding.collectAsState()

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val dayPhase = remember(currentHour) { SpiritualDayPhase.fromHour(currentHour) }

    if (showGoalDialog) {
        DailyGoalTargetDialog(
            currentGoal = dailyGoalTarget,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                viewModel.setDailyGoalTarget(newGoal)
                showGoalDialog = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Sanctuary Theme Artwork Background harmonized with time of day
        Image(
            painter = painterResource(id = palette.backgroundRes),
            contentDescription = "${palette.title} sanctuary background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = palette.backgroundArtAlpha
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (palette.isDark) {
                            listOf(
                                palette.background.copy(alpha = 0.68f),
                                palette.background.copy(alpha = 0.34f),
                                palette.background.copy(alpha = 0.62f)
                            )
                        } else {
                            listOf(
                                palette.background.copy(alpha = 0.72f),
                                dayPhase.dayOverlayTop.copy(alpha = 0.36f),
                                palette.surfaceVariant.copy(alpha = 0.68f)
                            )
                        }
                    )
                )
        )

        // Dim Night Focus Mode serene translucent veil
        if (focusVeilAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF04070B).copy(alpha = focusVeilAlpha))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Daily Target Progress Ring Badge, Focus Toggle & Sanctuary Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Daily Target Progress Ring Pill (clickable to customize goal)
                val goalDone = todayTotal >= dailyGoalTarget && dailyGoalTarget > 0
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = palette.surface.copy(alpha = if (isFocusMode) 0.72f else 0.90f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (goalDone) palette.accent else palette.cardBorder
                    ),
                    modifier = Modifier
                        .clickable { showGoalDialog = true }
                        .testTag("today_count_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 11.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = palette.cardBorder.copy(alpha = 0.55f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = palette.accent,
                                    startAngle = -90f,
                                    sweepAngle = dailyGoalSweep,
                                    useCenter = false,
                                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            if (goalDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Daily Goal Reached",
                                    tint = palette.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$todayTotal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) palette.accentLight else palette.textPrimary
                        )
                        Text(
                            text = "/$dailyGoalTarget",
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dim Night Focus Toggle Pill
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isFocusMode) palette.accent else palette.surface.copy(alpha = 0.90f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isFocusMode) palette.accentLight else palette.cardBorder
                        ),
                        modifier = Modifier
                            .clickable { viewModel.setFocusMode(!isFocusMode) }
                            .testTag("focus_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFocusMode) Icons.Default.Visibility else Icons.Default.NightsStay,
                                contentDescription = if (isFocusMode) "Exit Night Focus" else "Dim Night Focus",
                                tint = if (isFocusMode) palette.onPrimaryColor else palette.accent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFocusMode) "Exit Focus" else "Focus",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFocusMode) palette.onPrimaryColor else palette.textPrimary
                            )
                        }
                    }

                    // Quick Sanctuary Day/Night Switcher (hidden during calm Focus Mode)
                    AnimatedVisibility(visible = !isFocusMode) {
                        FilledTonalButton(
                            onClick = { viewModel.setNightMode(!isNightMode) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isNightMode) palette.accent else palette.surface.copy(alpha = 0.95f),
                                contentColor = if (isNightMode) palette.onPrimaryColor else palette.textPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isNightMode) Color.Transparent else palette.cardBorder
                            ),
                            modifier = Modifier.testTag("night_mode_toggle")
                        ) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Default.Bedtime else Icons.Default.LightMode,
                                contentDescription = "Toggle Night Mode",
                                modifier = Modifier.size(15.dp),
                                tint = if (isNightMode) palette.onPrimaryColor else palette.accentLight
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = palette.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isNightMode) palette.onPrimaryColor else palette.textPrimary
                            )
                        }
                    }
                }
            }

            // Compact Dual Quick Action Row (collapses cleanly during Dim Night Focus Mode)
            AnimatedVisibility(
                visible = !isFocusMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                // 1. Time-Aware Daily Adhkar Shortcut Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.surface.copy(alpha = 0.90f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        palette.accent.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenAthkarReader(dayPhase.athkarType) }
                        .testTag("daily_athkar_shortcut")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(text = dayPhase.emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = dayPhase.athkarLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) palette.accentLight else palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = palette.accent.copy(alpha = if (isNightMode) 0.22f else 0.14f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "Now ➔",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) palette.accentLight else palette.accent,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // 2. Compact After-Salah Set Trigger Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSalahSetMode) {
                        palette.accent
                    } else {
                        palette.surfaceVariant.copy(alpha = 0.88f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSalahSetMode) palette.accentLight else palette.cardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (isSalahSetMode) {
                                viewModel.exitSalahSetMode()
                            } else {
                                viewModel.startSalahSetMode()
                            }
                        }
                        .testTag("start_salah_set_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(text = "🕌", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isSalahSetMode) "In Set (${salahSetStage.stepIndex}/4)" else "After-Salah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSalahSetMode) {
                                    palette.onPrimaryColor
                                } else {
                                    if (isNightMode) palette.accentLight else palette.textPrimary
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = if (isInGuidedPlaylist) "Exit ✕" else "33×3 ➔",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSalahSetMode) {
                                palette.onPrimaryColor
                            } else palette.accent,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
            }

            // Dhikr Banner Card (Swipe left/right or tap chevrons to change Dhikr)
            var dhikrSwipeAccumulator by remember { mutableFloatStateOf(0f) }
            Card(
                onClick = {
                    if (!isInGuidedPlaylist) {
                        onNavigateToLibrary()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(isInGuidedPlaylist) {
                        detectHorizontalDragGestures(
                            onDragStart = { dhikrSwipeAccumulator = 0f },
                            onDragEnd = {
                                if (dhikrSwipeAccumulator < -40f) {
                                    viewModel.selectNextDhikr()
                                } else if (dhikrSwipeAccumulator > 40f) {
                                    viewModel.selectPreviousDhikr()
                                }
                                dhikrSwipeAccumulator = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (kotlin.math.abs(dragAmount) > 2f) {
                                    change.consume()
                                    dhikrSwipeAccumulator += dragAmount
                                }
                            }
                        )
                    }
                    .testTag("current_dhikr_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = palette.surface.copy(alpha = if (isFocusMode) 0.72f else 0.90f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isFocusMode) 0.dp else 3.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    if (isFocusMode) palette.accent.copy(alpha = 0.35f) else palette.cardBorder
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category & mode affordance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.primaryContainer.copy(alpha = if (isNightMode) 0.72f else 0.75f)
                        ) {
                            Text(
                                text = if (isInGuidedPlaylist) {
                                    "${activePlaylist?.name ?: "Playlist"} • Step ${playlistStepIdx + 1}/${playlistSteps.size}"
                                } else {
                                    currentDhikr?.category ?: "Daily"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) palette.accentLight else palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (isInGuidedPlaylist) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNightMode) Color(0xFF421C1C) else Color(0xFFFDE8E8),
                                modifier = Modifier
                                    .clickable { viewModel.exitPlaylistMode() }
                                    .testTag("exit_playlist_chip")
                            ) {
                                Text(
                                    text = "Exit Playlist ✕",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) Color(0xFFFFB4B4) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else if (!isFocusMode) {
                            Text(
                                text = "Swipe ◂ ▸ or tap arrows",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNightMode) palette.accent else palette.accentLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Main Dhikr Row flanked with subtle left and right arrows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isInGuidedPlaylist) {
                            IconButton(
                                onClick = { viewModel.selectPreviousDhikr() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("dhikr_prev_arrow")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Dhikr",
                                    tint = palette.accent.copy(alpha = 0.9f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val activeArabic = currentPlaylistStep?.arabicText ?: (currentDhikr?.arabicText ?: "سُبْحَانَ اللَّهِ")
                            val activeTranslit = currentPlaylistStep?.transliteration ?: (currentDhikr?.transliteration ?: "SubhanAllah")
                            val activeTranslation = currentPlaylistStep?.englishTranslation ?: (currentDhikr?.englishTranslation ?: "Glory be to Allah")
                            val isCompactArabic = activeArabic.length > 48

                            // Arabic Calligraphy Text (Bundled Amiri Naskh Calligraphy)
                            Text(
                                text = activeArabic,
                                fontFamily = ArabicCalligraphyFontFamily,
                                fontSize = if (isCompactArabic) 22.sp else 29.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isNightMode || isFocusMode) palette.accentLight else palette.textPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = if (isCompactArabic) 35.sp else 45.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Transliteration
                            Text(
                                text = activeTranslit,
                                fontSize = if (activeTranslit.length > 48) 13.sp else 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode || isFocusMode) Color.White.copy(alpha = 0.92f) else palette.accent,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // English Translation
                            Text(
                                text = activeTranslation,
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!isInGuidedPlaylist) {
                            IconButton(
                                onClick = { viewModel.selectNextDhikr() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("dhikr_next_arrow")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Dhikr",
                                    tint = palette.accent.copy(alpha = 0.9f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Unified Playlist Stepper or Single Dhikr Target Selector
            if (isInGuidedPlaylist) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("playlist_stepper_row"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    playlistSteps.forEachIndexed { idx, step ->
                        val isCurrent = idx == playlistStepIdx
                        val isPast = idx < playlistStepIdx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isCurrent -> palette.accent
                                isPast -> palette.primaryContainer
                                else -> palette.surface.copy(alpha = 0.8f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) palette.accentLight else if (isPast) palette.cardBorder else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isPast) "✓ ${idx + 1}" else if (isCurrent) "▶ ${idx + 1} (${step.targetCount})" else "${idx + 1}",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                color = when {
                                    isCurrent -> palette.onPrimaryColor
                                    isPast -> if (isNightMode) palette.accentLight else palette.textPrimary
                                    else -> palette.textSecondary.copy(alpha = 0.7f)
                                },
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            } else {
                AnimatedVisibility(
                    visible = !isFocusMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(33, 99, 100, 0).forEach { target ->
                            val isSelected = targetCount == target
                            val label = if (target == 0) "Free ∞" else "$target"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) {
                                    palette.accent
                                } else {
                                    palette.surface.copy(alpha = 0.78f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) palette.accentLight else palette.cardBorder.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { viewModel.setTargetCount(target) }
                                    .testTag("target_chip_$target")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) palette.onPrimaryColor else palette.textPrimary.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Counter HUD Display
            Card(
                onClick = { viewModel.onBeadPulled() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = palette.surface.copy(alpha = if (isFocusMode) 0.76f else 0.92f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isFocusMode) 1.dp else 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isNightMode || isFocusMode) palette.accent else palette.cardBorder
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag("center_counter_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$currentCount",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightMode || isFocusMode) palette.accentLight else palette.textPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isInGuidedPlaylist) {
                                "/ ${currentPlaylistStep?.targetCount ?: targetCount}"
                            } else if (targetCount > 0) {
                                "/ $targetCount"
                            } else {
                                "Target: ∞"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                        if (isInGuidedPlaylist) {
                            Text(
                                text = "Total: $setCumulativeCount/$playlistTotalCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accent
                            )
                        } else if (lapsCompleted > 0) {
                            Text(
                                text = "Laps: $lapsCompleted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accent
                            )
                        }
                    }
                }
            }

            // Non-blocking Milestone Celebration Banner
            AnimatedVisibility(
                visible = milestoneBanner != null,
                enter = fadeIn(tween(220)) + expandVertically(),
                exit = fadeOut(tween(260)) + shrinkVertically()
            ) {
                milestoneBanner?.let { banner ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (banner.isMajor) palette.accent else palette.primaryContainer.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            palette.accentLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("milestone_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (banner.isMajor) palette.onPrimaryColor else palette.accentLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = banner.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (banner.isMajor) palette.onPrimaryColor else palette.textPrimary
                                )
                                Text(
                                    text = banner.subtitle,
                                    fontSize = 10.sp,
                                    color = if (banner.isMajor) palette.onPrimaryColor.copy(alpha = 0.88f) else palette.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Hero Draggable Prayer Beads Strand
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DraggablePrayerBeads(
                    currentCount = currentCount,
                    targetCount = targetCount,
                    material = beadMaterial,
                    design = beadDesign,
                    tension = beadTension,
                    singleBeadLock = singleBeadLock,
                    isNightMode = isNightMode,
                    cordColor = palette.cordColor,
                    themeAccent = palette.accent,
                    themeAccentLight = palette.accentLight,
                    beadPullSeq = beadPullSequence,
                    onBeadPulled = { viewModel.onBeadPulled() },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("draggable_prayer_beads")
                )
            }

            // Quick String Tension & 1-Bead Lock Bar (collapses smoothly during Night Focus Mode)
            AnimatedVisibility(
                visible = !isFocusMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // 1-Bead Lock Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (singleBeadLock) {
                        palette.accent.copy(alpha = if (isNightMode) 0.22f else 0.14f)
                    } else {
                        palette.surfaceVariant.copy(alpha = 0.85f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (singleBeadLock) palette.accent else palette.cardBorder.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clickable { viewModel.setSingleBeadLock(!singleBeadLock) }
                        .testTag("single_bead_lock_chip")
                ) {
                    Text(
                        text = if (singleBeadLock) "🔒 1-Bead Lock: ON" else "🔓 Free Glide",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (singleBeadLock) {
                            if (isNightMode) palette.accentLight else palette.accent
                        } else {
                            palette.textSecondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Tension mode chips: Light, Standard, Firm
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        BeadTension.LIGHT to "Light",
                        BeadTension.STANDARD to "Standard",
                        BeadTension.FIRM to "Firm"
                    ).forEach { (t, label) ->
                        val isSelected = beadTension == t
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) palette.accent else palette.surfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier
                                .clickable { viewModel.setBeadTension(t) }
                                .testTag("tension_${label.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) palette.onPrimaryColor else palette.textSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Audio Guidance & Session Control Bar
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = palette.surface.copy(alpha = 0.92f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    palette.cardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undoLastBead() },
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo last bead",
                            tint = if (isNightMode) palette.accentLight else palette.textPrimary
                        )
                    }

                    // Speak Pronunciation / Recitation
                    IconButton(
                        onClick = { viewModel.speakCurrentDhikr() },
                        modifier = Modifier.testTag("speak_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen to pronunciation",
                            tint = palette.accent
                        )
                    }

                    // Auto-Guide Pacing Mode
                    FilledTonalButton(
                        onClick = { viewModel.toggleAutoGuide() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isAutoGuiding) Color(0xFFC0392B) else palette.accent,
                            contentColor = if (isAutoGuiding) Color.White else palette.onPrimaryColor
                        ),
                        modifier = Modifier.testTag("auto_guide_toggle")
                    ) {
                        Icon(
                            imageVector = if (isAutoGuiding) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoGuiding) "Pause Auto Guide" else "Start Auto Guide",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoGuiding) "Guiding..." else "Auto Guide",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetCounter() },
                        modifier = Modifier.testTag("reset_counter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset counter",
                            tint = if (isNightMode) palette.accentLight else palette.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyGoalTargetDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalInput by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Dhikr Target", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set a steady daily goal to illuminate your progress ring across Tasbih and Analytics.",
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100, 300, 500, 1000).forEach { preset ->
                        val selected = goalInput == preset.toString()
                        OutlinedButton(
                            onClick = { goalInput = preset.toString() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("goal_preset_$preset"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$preset",
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it.filter { ch -> ch.isDigit() }.take(5) },
                    label = { Text("Custom Daily Goal (10 – 10,000)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_daily_goal_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = (goalInput.toIntOrNull() ?: currentGoal).coerceIn(10, 10000)
                    onConfirm(parsed)
                },
                modifier = Modifier.testTag("save_daily_goal_btn")
            ) {
                Text("Save Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
