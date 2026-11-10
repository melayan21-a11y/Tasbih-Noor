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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.ui.components.BeadMaterial
import com.example.ui.components.BeadTension
import com.example.ui.components.DraggablePrayerBeads
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NightBackground
import com.example.ui.viewmodel.TasbihViewModel

@Composable
fun TasbihMainScreen(
    viewModel: TasbihViewModel,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDhikr by viewModel.selectedDhikr.collectAsState()
    val allDhikrs by viewModel.allDhikrs.collectAsState()
    val currentDhikr = selectedDhikr ?: allDhikrs.firstOrNull()

    val currentCount by viewModel.currentCount.collectAsState()
    val lapsCompleted by viewModel.lapsCompleted.collectAsState()
    val targetCount by viewModel.targetCount.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()

    val isNightMode by viewModel.isNightMode.collectAsState()
    val beadMaterial by viewModel.beadMaterial.collectAsState()
    val beadTension by viewModel.beadTension.collectAsState()
    val singleBeadLock by viewModel.singleBeadLock.collectAsState()
    val isAutoGuiding by viewModel.isAutoGuiding.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Night Recitation Mode background image or daytime ambient gradient
        if (isNightMode) {
            Image(
                painter = painterResource(id = R.drawable.bg_islamic_night),
                contentDescription = "Night ambient background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Soft dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F8F4),
                                Color(0xFFE5EFE9),
                                Color(0xFFD6E6DC)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Night Mode Toggle & Today's Total
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
                    color = if (isNightMode) Color(0xFF0F352C).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNightMode) GoldPrimary.copy(alpha = 0.4f) else EmeraldPrimary.copy(alpha = 0.2f)
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
                            color = if (isNightMode) GoldLight else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$todayTotal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else EmeraldPrimary
                        )
                    }
                }

                // Night Recitation Mode button
                FilledTonalButton(
                    onClick = { viewModel.setNightMode(!isNightMode) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isNightMode) GoldPrimary else Color.White.copy(alpha = 0.85f),
                        contentColor = if (isNightMode) NightBackground else EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("night_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isNightMode) Icons.Default.Bedtime else Icons.Default.LightMode,
                        contentDescription = "Toggle Night Mode",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNightMode) "Night Noor ✨" else "Light Theme",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Dhikr Banner Card (Tappable to change Dhikr)
            Card(
                onClick = onNavigateToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_dhikr_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF0C2B24).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) GoldPrimary.copy(alpha = 0.35f) else Color(0xFFD4E3DB)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category & change affordance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isNightMode) GoldPrimary.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = currentDhikr?.category ?: "Daily",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) GoldLight else EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Change Dhikr ▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNightMode) GoldPrimary else EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Arabic Calligraphy Text
                    Text(
                        text = currentDhikr?.arabicText ?: "سُبْحَانَ اللَّهِ",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightMode) GoldLight else EmeraldDeep,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Transliteration
                    Text(
                        text = currentDhikr?.transliteration ?: "SubhanAllah",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) Color.White.copy(alpha = 0.9f) else Color(0xFF263238),
                        textAlign = TextAlign.Center
                    )

                    // English Translation
                    Text(
                        text = currentDhikr?.englishTranslation ?: "Glory be to Allah",
                        fontSize = 12.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF546E7A),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

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
                            if (isNightMode) GoldPrimary else EmeraldPrimary
                        } else {
                            if (isNightMode) Color(0xFF10332B).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GoldLight else Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { viewModel.setTargetCount(target) }
                            .testTag("target_chip_$target")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                if (isNightMode) NightBackground else Color.White
                            } else {
                                if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.DarkGray
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Counter HUD Display
            Card(
                onClick = { viewModel.onBeadPulled() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF07211B).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.94f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isNightMode) GoldPrimary else EmeraldPrimary.copy(alpha = 0.35f)
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
                        color = if (isNightMode) GoldLight else EmeraldDeep
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (targetCount > 0) "/ $targetCount" else "Target: ∞",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                        if (lapsCompleted > 0) {
                            Text(
                                text = "Laps: $lapsCompleted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldPrimary else EmeraldPrimary
                            )
                        }
                    }
                }
            }

            // Hero Draggable Prayer Beads Strand (Big beads sliding horizontally across screen)
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
                    tension = beadTension,
                    singleBeadLock = singleBeadLock,
                    isNightMode = isNightMode,
                    onBeadPulled = { viewModel.onBeadPulled() },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("draggable_prayer_beads")
                )
            }

            // Quick String Tension & 1-Bead Lock Bar (stops scrolling 3-5 beads by accident)
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
                        if (isNightMode) GoldPrimary.copy(alpha = 0.25f) else EmeraldPrimary.copy(alpha = 0.12f)
                    } else {
                        if (isNightMode) Color(0xFF103328) else Color(0xFFE8ECE9)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (singleBeadLock) (if (isNightMode) GoldPrimary else EmeraldPrimary) else Color.Transparent
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
                            if (isNightMode) GoldLight else EmeraldPrimary
                        } else {
                            if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
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
                            color = if (isSelected) {
                                if (isNightMode) GoldPrimary else EmeraldPrimary
                            } else {
                                if (isNightMode) Color(0xFF10332B) else Color(0xFFF1F5F3)
                            },
                            modifier = Modifier
                                .clickable { viewModel.setBeadTension(t) }
                                .testTag("tension_${label.lowercase()}")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    if (isNightMode) Color(0xFF071914) else Color.White
                                } else {
                                    if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.DarkGray
                                },
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
                    containerColor = if (isNightMode) Color(0xFF0F352C).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.95f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) GoldPrimary.copy(alpha = 0.3f) else Color(0xFFD4E3DB)
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
                            tint = if (isNightMode) GoldLight else Color.DarkGray
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
                            tint = if (isNightMode) GoldPrimary else EmeraldPrimary
                        )
                    }

                    // Auto-Guide Pacing Mode (Guides recitations at continuous calming cadence)
                    FilledTonalButton(
                        onClick = { viewModel.toggleAutoGuide() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isAutoGuiding) {
                                Color(0xFFC0392B)
                            } else {
                                if (isNightMode) GoldPrimary else EmeraldPrimary
                            },
                            contentColor = Color.White
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
                            tint = if (isNightMode) GoldLight else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
