package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    val setCumulativeCount = when (salahSetStage) {
        SalahSetStage.SUBHANALLAH -> currentCount
        SalahSetStage.ALHAMDULILLAH -> 33 + currentCount
        SalahSetStage.ALLAHU_AKBAR -> 66 + currentCount
        SalahSetStage.TAHLIL_SEAL -> 99 + currentCount
    }

    val palette = LocalTasbihPalette.current
    val isNightMode by viewModel.isNightMode.collectAsState()
    val beadMaterial by viewModel.beadMaterial.collectAsState()
    val beadDesign by viewModel.beadDesign.collectAsState()
    val beadTension by viewModel.beadTension.collectAsState()
    val singleBeadLock by viewModel.singleBeadLock.collectAsState()
    val isAutoGuiding by viewModel.isAutoGuiding.collectAsState()

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val dayPhase = remember(currentHour) { SpiritualDayPhase.fromHour(currentHour) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Sanctuary Mode Toggle & Today's Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Today's count pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = palette.surface.copy(alpha = 0.90f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        palette.cardBorder
                    ),
                    modifier = Modifier.testTag("today_count_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today:",
                            fontSize = 12.sp,
                            color = palette.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$todayTotal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) palette.accentLight else palette.textPrimary
                        )
                    }
                }

                // Quick Sanctuary Day/Night Switcher displaying current sanctuary theme
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
                        modifier = Modifier.size(16.dp),
                        tint = if (isNightMode) palette.onPrimaryColor else palette.accentLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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

            // Compact Dual Quick Action Row (Time-Aware Daily Adhkar + After-Salah Set)
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
                            text = if (isSalahSetMode) "Exit ✕" else "33×3 ➔",
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

            // Dhikr Banner Card (Swipe left/right or tap chevrons to change Dhikr)
            var dhikrSwipeAccumulator by remember { mutableFloatStateOf(0f) }
            Card(
                onClick = {
                    if (!isSalahSetMode) {
                        onNavigateToLibrary()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(isSalahSetMode) {
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
                    containerColor = palette.surface.copy(alpha = 0.90f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    palette.cardBorder
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
                                text = if (isSalahSetMode) "🕌 After Salah Set • Step ${salahSetStage.stepIndex}/4" else (currentDhikr?.category ?: "Daily"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) palette.accentLight else palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (isSalahSetMode) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNightMode) Color(0xFF421C1C) else Color(0xFFFDE8E8),
                                modifier = Modifier.clickable { viewModel.exitSalahSetMode() }
                            ) {
                                Text(
                                    text = "Exit Set ✕",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) Color(0xFFFFB4B4) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
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
                        if (!isSalahSetMode) {
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
                            // Arabic Calligraphy Text (Bundled Amiri Naskh Calligraphy)
                            Text(
                                text = if (isSalahSetMode) salahSetStage.arabicText else (currentDhikr?.arabicText ?: "سُبْحَانَ اللَّهِ"),
                                fontFamily = ArabicCalligraphyFontFamily,
                                fontSize = if (isSalahSetMode && salahSetStage == SalahSetStage.TAHLIL_SEAL) 22.sp else 29.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isNightMode) palette.accentLight else palette.textPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = if (isSalahSetMode && salahSetStage == SalahSetStage.TAHLIL_SEAL) 36.sp else 45.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Transliteration
                            Text(
                                text = if (isSalahSetMode) salahSetStage.transliteration else (currentDhikr?.transliteration ?: "SubhanAllah"),
                                fontSize = if (isSalahSetMode && salahSetStage == SalahSetStage.TAHLIL_SEAL) 13.sp else 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) palette.textPrimary else palette.accent,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // English Translation
                            Text(
                                text = if (isSalahSetMode) salahSetStage.translation else (currentDhikr?.englishTranslation ?: "Glory be to Allah"),
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!isSalahSetMode) {
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

            // Stepper / Target selector row
            if (isSalahSetMode) {
                // 4-Step Chained After-Salah Progress Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        SalahSetStage.SUBHANALLAH to "1. SubhanAllah (33)",
                        SalahSetStage.ALHAMDULILLAH to "2. Alhamdulillah (33)",
                        SalahSetStage.ALLAHU_AKBAR to "3. Allahu Akbar (33)",
                        SalahSetStage.TAHLIL_SEAL to "4. Seal (1)"
                    ).forEach { (stage, _) ->
                        val isCurrent = salahSetStage == stage
                        val isPast = salahSetStage.stepIndex > stage.stepIndex
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
                                text = if (isPast) "✓ ${stage.stepIndex}" else if (isCurrent) "▶ ${stage.stepIndex}" else "${stage.stepIndex}",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    isCurrent -> palette.onPrimaryColor
                                    isPast -> if (isNightMode) palette.accentLight else palette.textPrimary
                                    else -> palette.textSecondary.copy(alpha = 0.7f)
                                },
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            } else {
                // Target selector row (33, 99, 100, Free)
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

            // Counter HUD Display
            Card(
                onClick = { viewModel.onBeadPulled() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = palette.surface.copy(alpha = 0.92f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isNightMode) palette.accent else palette.cardBorder
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
                        color = if (isNightMode) palette.accentLight else palette.textPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSalahSetMode) "/ ${salahSetStage.target}" else (if (targetCount > 0) "/ $targetCount" else "Target: ∞"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textSecondary
                        )
                        if (isSalahSetMode) {
                            Text(
                                text = "Set: $setCumulativeCount/100",
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
                    onBeadPulled = { viewModel.onBeadPulled() },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("draggable_prayer_beads")
                )
            }

            // Quick String Tension & 1-Bead Lock Bar
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
