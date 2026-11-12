package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AthkarItem
import com.example.data.model.AthkarRepository
import com.example.data.model.AthkarType
import com.example.data.model.DhikrItem
import com.example.ui.theme.DayEmeraldDeep
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NightBackground
import com.example.ui.viewmodel.TasbihViewModel

@Composable
fun AthkarReaderScreen(
    viewModel: TasbihViewModel,
    initialType: AthkarType = AthkarType.MORNING,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(initialType) }
    val items = if (selectedType == AthkarType.MORNING) AthkarRepository.morningAthkar else AthkarRepository.eveningAthkar

    var currentIndex by remember(selectedType) { mutableIntStateOf(0) }
    var currentItemCount by remember(currentIndex, selectedType) { mutableIntStateOf(0) }
    var showVirtue by remember { mutableStateOf(false) }
    var isCompletedSession by remember { mutableStateOf(false) }
    var swipeAccumulator by remember { mutableFloatStateOf(0f) }

    val isNightMode by viewModel.isNightMode.collectAsState()
    val soundType by viewModel.soundType.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()

    BackHandler {
        if (isCompletedSession) {
            isCompletedSession = false
        } else {
            onBack()
        }
    }

    val currentItem = items.getOrElse(currentIndex) { items.first() }
    val target = currentItem.targetCount
    val isItemDone = currentItemCount >= target

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF071914) else Color(0xFFFAF7F0))
    ) {
        // Ambient background artwork for both Night Noor and Day Noor
        Image(
            painter = painterResource(id = if (isNightMode) R.drawable.bg_islamic_night else R.drawable.bg_islamic_day),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (isNightMode) 0.22f else 0.16f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("athkar_reader_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isNightMode) GoldLight else EmeraldDeep
                    )
                }

                // Morning / Evening Selector Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isNightMode) Color(0xFF0C2B22) else Color(0xFFE8F1EC),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNightMode) GoldPrimary.copy(alpha = 0.35f) else Color(0xFFD0E0D6)
                    )
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedType == AthkarType.MORNING) {
                                if (isNightMode) GoldPrimary else EmeraldPrimary
                            } else Color.Transparent,
                            modifier = Modifier.clickable {
                                selectedType = AthkarType.MORNING
                                currentIndex = 0
                                currentItemCount = 0
                                isCompletedSession = false
                            }
                        ) {
                            Text(
                                text = "Morning",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == AthkarType.MORNING) {
                                    if (isNightMode) NightBackground else Color.White
                                } else (if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.DarkGray),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedType == AthkarType.EVENING) {
                                if (isNightMode) GoldPrimary else EmeraldPrimary
                            } else Color.Transparent,
                            modifier = Modifier.clickable {
                                selectedType = AthkarType.EVENING
                                currentIndex = 0
                                currentItemCount = 0
                                isCompletedSession = false
                            }
                        ) {
                            Text(
                                text = "Evening",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == AthkarType.EVENING) {
                                    if (isNightMode) NightBackground else Color.White
                                } else (if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.DarkGray),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Voice audio recitation button
                IconButton(
                    onClick = {
                        viewModel.audioHapticManager.speakRecitation(currentItem.arabicText, currentItem.transliteration)
                    },
                    modifier = Modifier.testTag("athkar_speak_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Recite",
                        tint = GoldPrimary
                    )
                }
            }

            // Progress across entire Athkar booklet
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedType.titleEnglish} (${selectedType.titleArabic})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNightMode) GoldLight else EmeraldDeep
                )
                Text(
                    text = "${currentIndex + 1} of ${items.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + (if (isItemDone) 1f else currentItemCount.toFloat() / target)) / items.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isNightMode) GoldPrimary else EmeraldPrimary,
                trackColor = if (isNightMode) Color(0xFF133F34) else Color(0xFFDEE9E2),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isCompletedSession) {
                // Completed Session Celebration Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isNightMode) GoldPrimary else EmeraldPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GoldPrimary,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF071914),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Alhamdulillah!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "You have completed your ${selectedType.titleEnglish}.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) Color.White else Color(0xFF263238),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "May Allah place you under His divine protection, illuminate your heart with tranquility, and accept every word of your remembrance.",
                                fontSize = 13.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isNightMode) GoldPrimary else EmeraldPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Return to Tasbih",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) NightBackground else Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Active Supplication Card with Scrollable Body and Horizontal Swipe Navigation
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(currentIndex, selectedType) {
                            detectHorizontalDragGestures(
                                onDragStart = { swipeAccumulator = 0f },
                                onDragEnd = {
                                    if (swipeAccumulator < -40f) {
                                        // Swiped left -> Next Athkar
                                        if (currentIndex < items.size - 1) {
                                            currentIndex += 1
                                            currentItemCount = 0
                                        } else {
                                            isCompletedSession = true
                                        }
                                    } else if (swipeAccumulator > 40f) {
                                        // Swiped right -> Previous Athkar
                                        if (currentIndex > 0) {
                                            currentIndex -= 1
                                            currentItemCount = 0
                                        }
                                    }
                                    swipeAccumulator = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    if (kotlin.math.abs(dragAmount) > 2f) {
                                        change.consume()
                                        swipeAccumulator += dragAmount
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNightMode) Color(0xFF0A261E).copy(alpha = 0.94f) else Color(0xFFFFFDF8).copy(alpha = 0.96f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (isNightMode) GoldPrimary.copy(alpha = 0.35f) else Color(0xFFDFCC9D)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Arabic Text in magnificent calligraphy layout
                            Text(
                                text = currentItem.arabicText,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else DayEmeraldDeep,
                                textAlign = TextAlign.Right,
                                lineHeight = 36.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Transliteration
                            Text(
                                text = currentItem.transliteration,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) Color(0xFFFFD54F) else Color(0xFF8C6418),
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // English Translation
                            Text(
                                text = currentItem.englishTranslation,
                                fontSize = 12.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.85f) else Color(0xFF4C6B61),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Virtue & Hadith reference toggle button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isNightMode) Color(0xFF10362B) else Color(0xFFF1F8F4))
                                    .clickable { showVirtue = !showVirtue }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Virtue",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hadith Virtue & Reference",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNightMode) GoldLight else EmeraldDeep
                                    )
                                }
                                Text(
                                    text = if (showVirtue) "Hide ▲" else "Read ▼",
                                    fontSize = 10.sp,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showVirtue) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isNightMode) Color(0xFF081C16) else Color(0xFFFAFBF9))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = currentItem.virtue,
                                        fontSize = 11.sp,
                                        color = if (isNightMode) Color.White.copy(alpha = 0.8f) else Color(0xFF455A64),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentItem.reference,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tap Counter Button for Current Supplication
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isItemDone) {
                                if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                            } else {
                                if (isNightMode) Color(0xFF103328) else Color(0xFFF1F8F4)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isItemDone) GoldPrimary else if (isNightMode) Color(0xFF1E483C) else Color(0xFFD0E0D6)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isItemDone) {
                                        viewModel.audioHapticManager.playBeadSound(soundType)
                                        viewModel.audioHapticManager.triggerBeadHaptic(hapticIntensity)

                                        val newC = currentItemCount + 1
                                        currentItemCount = newC

                                        // Record count in repository so it updates streak and heatmap
                                        viewModel.onBeadPulled()

                                        if (newC >= target) {
                                            viewModel.audioHapticManager.triggerTargetReachedHaptic()
                                        }
                                    }
                                }
                                .testTag("athkar_tap_counter")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isItemDone) "Complete ✨" else "Tap to Recite",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isItemDone) GoldPrimary else (if (isNightMode) Color.White else Color(0xFF263238))
                                    )
                                    Text(
                                        text = if (isItemDone) "Target reached for this supplication" else "Prescribed: $target time${if (target > 1) "s" else ""}",
                                        fontSize = 11.sp,
                                        color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$currentItemCount",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isItemDone) GoldLight else (if (isNightMode) Color.White else EmeraldDeep)
                                    )
                                    Text(
                                        text = " / $target",
                                        fontSize = 14.sp,
                                        color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Hint for horizontal swipe or button navigation
                Text(
                    text = "Swipe ◂ ▸ or tap buttons to navigate",
                    fontSize = 11.sp,
                    color = if (isNightMode) Color.White.copy(alpha = 0.5f) else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 6.dp)
                )

                // Bottom Step Navigation (Previous / Reset / Next)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) {
                                currentIndex -= 1
                                currentItemCount = 0
                            }
                        },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("athkar_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev", fontSize = 12.sp)
                    }

                    // Reset current item count button
                    IconButton(
                        onClick = { currentItemCount = 0 },
                        modifier = Modifier.testTag("athkar_reset_item_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset this item",
                            tint = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                        )
                    }

                    Button(
                        onClick = {
                            if (currentIndex < items.size - 1) {
                                currentIndex += 1
                                currentItemCount = 0
                            } else {
                                isCompletedSession = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNightMode) GoldPrimary else EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("athkar_next_button")
                    ) {
                        Text(
                            text = if (currentIndex < items.size - 1) "Next" else "Finish ✨",
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) NightBackground else Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = if (isNightMode) NightBackground else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
