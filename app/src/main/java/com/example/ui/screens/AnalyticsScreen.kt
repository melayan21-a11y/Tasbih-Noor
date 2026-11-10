package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.MonthDayStat
import com.example.ui.viewmodel.TasbihViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: TasbihViewModel,
    modifier: Modifier = Modifier
) {
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    val typeStats by viewModel.typeStats.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()
    val lifetimeTotal by viewModel.lifetimeTotal.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val currentCalMonth by viewModel.currentCalendarMonth.collectAsState()

    val monthDays = remember(dailyTotals, currentCalMonth) {
        viewModel.getMonthDays(dailyTotals, currentCalMonth)
    }

    var selectedDayStat by remember { mutableStateOf<MonthDayStat?>(null) }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = monthFormat.format(currentCalMonth.time)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF071914) else Color(0xFFF7FAF8))
    ) {
        if (isNightMode) {
            Image(
                painter = painterResource(id = R.drawable.bg_islamic_night),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.22f
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Dhikr Progress Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNightMode) GoldLight else EmeraldDeep
                )
                Text(
                    text = "Monthly consistency analytics and spiritual milestones",
                    fontSize = 13.sp,
                    color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF5A7268)
                )
            }
        }

        // Streak & Total Highlights Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Streak Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("streak_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNightMode) GoldPrimary.copy(alpha = 0.4f) else Color(0xFFA5D6A7)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF6D00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Daily Streak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${streakInfo.currentStreak} Days",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) Color.White else EmeraldDeep
                        )
                        Text(
                            text = "Best: ${streakInfo.longestStreak} days",
                            fontSize = 11.sp,
                            color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                        )
                    }
                }

                // Lifetime Total Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lifetime_total_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNightMode) Color(0xFF1B382F) else Color(0xFFFFF8E1)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNightMode) GoldPrimary.copy(alpha = 0.4f) else Color(0xFFFFE082)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Total",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lifetime Dhikr",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNightMode) GoldLight else Color(0xFF5D4037)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$lifetimeTotal",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else Color(0xFF8D6E63)
                        )
                        Text(
                            text = "Today: $todayTotal",
                            fontSize = 11.sp,
                            color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                        )
                    }
                }
            }
        }

        // Monthly Consistency Heatmap Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_consistency_heatmap"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Consistency",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                            Text(
                                text = monthTitle,
                                fontSize = 13.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        }

                        Row {
                            IconButton(onClick = { viewModel.changeCalendarMonth(-1) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Month",
                                    tint = if (isNightMode) GoldPrimary else EmeraldPrimary
                                )
                            }
                            IconButton(onClick = { viewModel.changeCalendarMonth(1) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Month",
                                    tint = if (isNightMode) GoldPrimary else EmeraldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of Week Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight.copy(alpha = 0.5f) else Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Heatmap Grid: 7 columns
                    val firstDayCal = Calendar.getInstance().apply {
                        time = currentCalMonth.time
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val dayOfWeekOffset = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // Sunday=0

                    // Build cells with empty leading spaces for proper calendar alignment
                    val totalCells = dayOfWeekOffset + monthDays.size
                    val rows = (totalCells + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayIndex = cellIndex - dayOfWeekOffset
                                if (dayIndex in monthDays.indices) {
                                    val dayStat = monthDays[dayIndex]
                                    HeatmapDayCell(
                                        stat = dayStat,
                                        isNightMode = isNightMode,
                                        isSelected = selectedDayStat?.dateString == dayStat.dateString,
                                        onClick = { selectedDayStat = dayStat },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Selected Day Detail Box
                    if (selectedDayStat != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date: ${selectedDayStat?.dateString}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isNightMode) Color.White else EmeraldDeep
                                )
                                Text(
                                    text = "${selectedDayStat?.count} Adhkar Recited",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) GoldLight else EmeraldPrimary
                                )
                            }
                        }
                    }

                    // Legend Row
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Less", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        listOf(
                            if (isNightMode) Color(0xFF103328) else Color(0xFFE8ECE9),
                            if (isNightMode) Color(0xFF165B47) else Color(0xFFA5D6A7),
                            if (isNightMode) Color(0xFF1E8267) else Color(0xFF4CAF50),
                            GoldPrimary
                        ).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("More", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Dhikr Distribution Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dhikr_distribution_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Most Recited Prayers",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightMode) GoldLight else EmeraldDeep
                    )
                    Text(
                        text = "Your top supplications ranked by total recitations",
                        fontSize = 12.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.65f) else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (typeStats.isEmpty()) {
                        Text(
                            text = "Recite on the prayer beads to see your prayers breakdown here.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val maxCount = typeStats.maxOfOrNull { it.totalCount } ?: 1
                        typeStats.take(5).forEachIndexed { index, stat ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isNightMode) Color(0xFF0D2C23) else Color(0xFFF7FAF8),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isNightMode) Color(0xFF164739) else Color(0xFFE5EDE8)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Rank Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = when (index) {
                                                0 -> GoldPrimary
                                                1 -> if (isNightMode) Color(0xFF1B614D) else EmeraldPrimary
                                                else -> if (isNightMode) Color(0xFF144436) else Color(0xFFD6E4DC)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (index) {
                                                        0 -> Color(0xFF071914)
                                                        1 -> Color.White
                                                        else -> if (isNightMode) GoldLight else EmeraldDeep
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Dhikr Title (Clutter-free, Single Line with Ellipsis)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stat.dhikrTitle,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isNightMode) Color.White else Color(0xFF263238),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Count & Label
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "${stat.totalCount}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isNightMode) GoldLight else EmeraldDeep
                                            )
                                            Text(
                                                text = " times",
                                                fontSize = 10.sp,
                                                color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                                modifier = Modifier.padding(bottom = 1.dp, start = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Clean proportional progress bar
                                    LinearProgressIndicator(
                                        progress = { stat.totalCount.toFloat() / maxCount },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (index == 0) GoldPrimary else (if (isNightMode) GoldLight.copy(alpha = 0.8f) else EmeraldPrimary),
                                        trackColor = if (isNightMode) Color(0xFF081C16) else Color(0xFFE2EBE5),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Spiritual Milestones & Perpetual Devotion Mastery
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("milestones_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF09241C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isNightMode) Color(0xFF1E483C) else Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Milestones",
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Spiritual Milestones & Ranks",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else EmeraldDeep
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Perpetual Spiritual Mastery Rank Card
                    val devotionLevel = (lifetimeTotal / 2500) + 1
                    val currentLevelProgress = lifetimeTotal % 2500
                    val currentLevelTarget = 2500
                    val rankTitle = when {
                        devotionLevel >= 40 -> "Al-Muqarrab (Dhikr Luminary)"
                        devotionLevel >= 20 -> "Taj adh-Dhikr (Crown of Remembrance)"
                        devotionLevel >= 10 -> "Al-Dhakir al-Akbar (Venerable Devotee)"
                        devotionLevel >= 5 -> "Al-Mustaghfir (Steadfast Supplicant)"
                        else -> "Al-Musabbih (Devout Seeker)"
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isNightMode) Color(0xFF10362B) else Color(0xFFF1F8F4),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isNightMode) GoldPrimary.copy(alpha = 0.4f) else Color(0xFFC8E6C9)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = GoldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.WorkspacePremium,
                                                contentDescription = null,
                                                tint = Color(0xFF071914),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Rank Tier $devotionLevel: $rankTitle",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNightMode) GoldLight else EmeraldDeep
                                        )
                                        Text(
                                            text = "Perpetual Devotion Horizon",
                                            fontSize = 11.sp,
                                            color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "To Level ${devotionLevel + 1}",
                                    fontSize = 11.sp,
                                    color = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                                Text(
                                    text = "$currentLevelProgress / $currentLevelTarget Adhkar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) GoldLight else EmeraldPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { currentLevelProgress.toFloat() / currentLevelTarget },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GoldPrimary,
                                trackColor = if (isNightMode) Color(0xFF0A221B) else Color(0xFFDEE7E1),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Foundation & Devotion Milestones",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) Color.White else Color(0xFF263238)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val milestones = listOf(
                        Milestone("Seed of Jannah", "Recite 100 total Adhkar", lifetimeTotal >= 100, "100"),
                        Milestone("Steadfast Heart", "Maintain a 3-day Dhikr streak", streakInfo.longestStreak >= 3, "3d"),
                        Milestone("Noor of Dawn", "Maintain a 7-day Dhikr streak", streakInfo.longestStreak >= 7, "7d"),
                        Milestone("Tasbih Devotee", "Recite 1,000 total Adhkar", lifetimeTotal >= 1000, "1K"),
                        Milestone("Fortnight of Light", "Maintain a 14-day Dhikr streak", streakInfo.longestStreak >= 14, "14d"),
                        Milestone("Constant Remembrance", "Recite 10,000 total Adhkar", lifetimeTotal >= 10000, "10K"),
                        Milestone("Zakireen & Zakirat", "Recite 25,000 total Adhkar (Quran 33:35)", lifetimeTotal >= 25000, "25K"),
                        Milestone("Istiqamah Master", "Maintain a 30-day continuous Dhikr streak", streakInfo.longestStreak >= 30, "30d"),
                        Milestone("Crown of Dhikr", "Recite 50,000 total Adhkar", lifetimeTotal >= 50000, "50K"),
                        Milestone("Al-Qalb as-Salim", "Recite 100,000 total Adhkar", lifetimeTotal >= 100000, "100K")
                    )

                    milestones.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (m.isAchieved) GoldPrimary else (if (isNightMode) Color(0xFF143B30) else Color(0xFFE0E0E0)),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (m.isAchieved) Color(0xFF071914) else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (m.isAchieved) (if (isNightMode) GoldLight else EmeraldDeep) else (if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.DarkGray)
                                )
                                Text(
                                    text = m.description,
                                    fontSize = 11.sp,
                                    color = if (isNightMode) Color.White.copy(alpha = 0.6f) else Color.Gray
                                )
                            }
                            if (m.isAchieved) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isNightMode) Color(0xFF133F34) else Color(0xFFE8F5E9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Unlocked ✨",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = m.badgeTag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) Color.White.copy(alpha = 0.4f) else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}



private data class Milestone(
    val title: String,
    val description: String,
    val isAchieved: Boolean,
    val badgeTag: String = ""
)

@Composable
private fun HeatmapDayCell(
    stat: MonthDayStat,
    isNightMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellColor = when {
        stat.isFuture -> Color.Transparent
        stat.count >= 200 -> GoldPrimary
        stat.count >= 70 -> if (isNightMode) Color(0xFF1E8267) else Color(0xFF4CAF50)
        stat.count >= 1 -> if (isNightMode) Color(0xFF165B47) else Color(0xFFA5D6A7)
        else -> if (isNightMode) Color(0xFF0E3026) else Color(0xFFEFF3F0)
    }

    val textColor = when {
        stat.count >= 200 -> Color(0xFF071914)
        stat.count >= 70 -> Color.White
        stat.count >= 1 -> if (isNightMode) Color.White else Color(0xFF1B5E20)
        stat.isFuture -> Color.Gray.copy(alpha = 0.3f)
        else -> if (isNightMode) Color.White.copy(alpha = 0.5f) else Color.Gray
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(cellColor)
            .then(
                if (isSelected || stat.isToday) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = if (stat.isToday) GoldLight else Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            )
            .clickable(enabled = !stat.isFuture, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${stat.dayOfMonth}",
            fontSize = 11.sp,
            fontWeight = if (stat.count > 0 || stat.isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}
