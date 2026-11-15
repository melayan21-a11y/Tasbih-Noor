package com.example.wear

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import kotlinx.coroutines.launch

private val MidnightEmerald = Color(0xFF061712)
private val DeepCardEmerald = Color(0xFF0E2B23)
private val NoorGold = Color(0xFFD4AF37)
private val NoorGoldLight = Color(0xFFF3D97E)
private val CrescentEmerald = Color(0xFF1A6B52)

class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WearTasbihStore.ensureLoaded(applicationContext)

        setContent {
            MaterialTheme {
                WearTasbihScreen()
            }
        }
    }
}

@Composable
fun WearTasbihScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by WearTasbihStore.state.collectAsState()
    var connectedPhones by remember { mutableIntStateOf(0) }
    var syncStatusMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        WearTasbihStore.ensureLoaded(context)
        connectedPhones = WearDataSync.getConnectedPhoneCount(context)
        WearDataSync.requestPhoneSync(context)
        focusRequester.requestFocus()
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        modifier = Modifier.background(MidnightEmerald)
    ) {
        ScalingLazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 24.dp, bottom = 36.dp, start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent { event ->
                    scope.launch {
                        listState.scrollBy(event.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            // 1. Primary Full-Face Radial Bead Tap Dial
            item {
                WearBeadDialCard(
                    state = state,
                    onTapBead = {
                        val (updated, reachedTarget) = WearTasbihStore.incrementBead(context)
                        triggerWristHaptic(context, isMilestone = reachedTarget)
                        scope.launch {
                            WearDataSync.sendIncrementToPhone(
                                context = context,
                                dhikrId = updated.dhikrId,
                                transliteration = updated.transliteration,
                                delta = 1
                            )
                        }
                    }
                )
            }

            // 2. Quick Undo / Reset / Sync Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    CompactChip(
                        onClick = {
                            WearTasbihStore.undoBead(context)
                            triggerWristHaptic(context, isMilestone = false)
                        },
                        label = { Text("Undo", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo bead",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(
                            backgroundColor = DeepCardEmerald,
                            contentColor = NoorGoldLight
                        ),
                        modifier = Modifier.testTag("wear_undo_btn")
                    )

                    CompactChip(
                        onClick = {
                            WearTasbihStore.resetCount(context)
                            triggerWristHaptic(context, isMilestone = false)
                        },
                        label = { Text("Reset", fontSize = 11.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset count",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = ChipDefaults.secondaryChipColors(
                            backgroundColor = DeepCardEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("wear_reset_btn")
                    )
                }
            }

            // 3. After-Salah 33-33-33-1 Guided Set Toggle
            item {
                ToggleChip(
                    checked = state.salahModeEnabled,
                    onCheckedChange = {
                        WearTasbihStore.toggleSalahMode(context)
                        triggerWristHaptic(context, isMilestone = true)
                    },
                    label = {
                        Text(
                            text = "After-Salah 33×3+1",
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = if (state.salahModeEnabled) {
                                "Stage ${state.salahStageIndex + 1} of 4"
                            } else {
                                "Auto-advance Sunnah set"
                            },
                            fontSize = 10.sp
                        )
                    },
                    toggleControl = {
                        Icon(
                            imageVector = ToggleChipDefaults.switchIcon(checked = state.salahModeEnabled),
                            contentDescription = null
                        )
                    },
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedStartBackgroundColor = CrescentEmerald,
                        checkedEndBackgroundColor = DeepCardEmerald,
                        checkedContentColor = NoorGoldLight,
                        uncheckedStartBackgroundColor = DeepCardEmerald,
                        uncheckedEndBackgroundColor = DeepCardEmerald
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wear_salah_toggle")
                )
            }

            // 4. Phone Sync Button
            item {
                Chip(
                    onClick = {
                        scope.launch {
                            val count = WearDataSync.getConnectedPhoneCount(context)
                            connectedPhones = count
                            val requested = WearDataSync.requestPhoneSync(context)
                            syncStatusMessage = when {
                                requested -> "Sync requested with phone"
                                count > 0 -> "Phone paired ($count)"
                                else -> "Standalone offline mode"
                            }
                        }
                    },
                    label = {
                        Text(
                            text = if (connectedPhones > 0) "Sync Phone ($connectedPhones)" else "Sync with Phone",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = syncStatusMessage ?: "Streak: ${state.streakDays}d • Today: ${state.todayTotal}",
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync with phone",
                            tint = NoorGold
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = DeepCardEmerald,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wear_sync_btn")
                )
            }

            // 5. Dhikr Presets Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NoorGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adhkar Presets",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoorGoldLight
                    )
                }
            }

            items(WearTasbihStore.defaultPresets) { preset ->
                val isSelected = state.dhikrId == preset.id && !state.salahModeEnabled
                Chip(
                    onClick = {
                        WearTasbihStore.selectPreset(context, preset)
                        triggerWristHaptic(context, isMilestone = false)
                    },
                    label = {
                        Text(
                            text = preset.transliteration,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "${preset.arabicText} • ${preset.defaultTarget}",
                            fontFamily = AmiriWearFontFamily,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (isSelected) CrescentEmerald else DeepCardEmerald,
                        contentColor = if (isSelected) NoorGoldLight else Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WearBeadDialCard(
    state: WearTasbihState,
    onTapBead: () -> Unit
) {
    val safeTarget = state.targetCount.coerceAtLeast(1)
    val progress = (state.currentCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(164.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF123C30), DeepCardEmerald, MidnightEmerald)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTapBead
            )
            .testTag("wear_bead_dial")
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val strokeWidth = 9.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Subtle track
            drawArc(
                color = Color(0xFF1B4D3E),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Golden progress sweep
            drawArc(
                brush = Brush.sweepGradient(listOf(NoorGold, NoorGoldLight, NoorGold)),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = state.arabicText,
                fontFamily = AmiriWearFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NoorGoldLight,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = state.transliteration,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${state.currentCount} / $safeTarget",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Tap Bead • Today ${state.todayTotal}",
                fontSize = 10.sp,
                color = NoorGold,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun triggerWristHaptic(context: Context, isMilestone: Boolean) {
    val vibrator: Vibrator = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }) ?: return

    if (!vibrator.hasVibrator()) return

    if (isMilestone) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 45, 70, 80), intArrayOf(0, 180, 0, 240), -1)
        )
    } else {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }
}
