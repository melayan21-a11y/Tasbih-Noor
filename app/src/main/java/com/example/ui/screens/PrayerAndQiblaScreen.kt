package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.prayer.CityLocation
import com.example.data.prayer.CityLocations
import com.example.data.prayer.PrayerTimesResult
import com.example.ui.theme.DayEmeraldDeep
import com.example.ui.theme.DayGoldAccent
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LightBackground
import com.example.ui.theme.NightBackground
import com.example.ui.viewmodel.TasbihViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dedicated Qibla Compass Screen.
 * Provides high-precision Great-Circle Kaaba calculation, real-time sensor compass,
 * dynamic Kaaba alignment indicator, distance to Makkah in km, and city selector.
 */
@Composable
fun QiblaScreen(
    viewModel: TasbihViewModel,
    modifier: Modifier = Modifier
) {
    val isNightMode by viewModel.isNightMode.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val prayerResult by viewModel.prayerTimesResult.collectAsState()
    val azimuthDegrees by viewModel.azimuthDegrees.collectAsState()
    val isSensorAvailable by viewModel.compassSensorManager.isSensorAvailable.collectAsState()

    var showCityPicker by remember { mutableStateOf(false) }

    // Start compass sensor when viewing Qibla screen, stop when disposed
    DisposableEffect(Unit) {
        viewModel.compassSensorManager.startListening()
        onDispose {
            viewModel.compassSensorManager.stopListening()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isNightMode) NightBackground else LightBackground)
    ) {
        // Islamic Background Artwork (Night / Day)
        if (isNightMode) {
            Image(
                painter = painterResource(id = R.drawable.bg_islamic_night),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.22f
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.bg_islamic_day),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.18f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header: Title & City Selector Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Qibla Compass",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNightMode) GoldLight else DayEmeraldDeep
                    )
                    Text(
                        text = "اتجاه القبلة الشريفة • Kaaba Direction",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isNightMode) GoldPrimary.copy(alpha = 0.85f) else DayGoldAccent
                    )
                }

                // City Selector Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isNightMode) Color(0xFF133F34) else Color(0xFFF3ECE0),
                    border = BorderStroke(
                        1.dp,
                        if (isNightMode) GoldPrimary.copy(alpha = 0.6f) else DayGoldAccent.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clickable { showCityPicker = true }
                        .testTag("city_selector_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Select City",
                            tint = if (isNightMode) GoldLight else DayEmeraldDeep,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Text(
                                text = selectedCity.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else DayEmeraldDeep
                            )
                        }
                    }
                }
            }

            // Standalone Qibla Compass Content
            QiblaCompassView(
                result = prayerResult,
                selectedCity = selectedCity,
                azimuthDegrees = azimuthDegrees,
                isSensorAvailable = isSensorAvailable,
                isNightMode = isNightMode
            )
        }

        // City Picker Dialog
        if (showCityPicker) {
            CityPickerDialog(
                currentCity = selectedCity,
                onDismiss = { showCityPicker = false },
                onSelectCity = {
                    viewModel.setCity(it)
                    showCityPicker = false
                }
            )
        }
    }
}

/**
 * Backward compatibility alias so existing callers or navigation actions
 * seamlessly route to the dedicated Qibla screen.
 */
@Composable
fun PrayerAndQiblaScreen(
    viewModel: TasbihViewModel,
    onNavigateToSalahSet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    QiblaScreen(viewModel = viewModel, modifier = modifier)
}

@Composable
private fun QiblaCompassView(
    result: PrayerTimesResult,
    selectedCity: CityLocation,
    azimuthDegrees: Float,
    isSensorAvailable: Boolean,
    isNightMode: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val qiblaBearing = result.qiblaBearing

    // Relative angle between device heading and Kaaba
    val relativeAngle = ((qiblaBearing - azimuthDegrees + 540.0) % 360.0) - 180.0
    val isAligned = abs(relativeAngle) <= 4.0

    // Provide haptic feedback once when alignment is achieved
    var hasVibratedForCurrentAlignment by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !hasVibratedForCurrentAlignment) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasVibratedForCurrentAlignment = true
        } else if (!isAligned) {
            hasVibratedForCurrentAlignment = false
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -azimuthDegrees,
        animationSpec = tween(durationMillis = 200),
        label = "compass_rotation"
    )

    val alignmentColor by animateColorAsState(
        targetValue = if (isAligned) GoldPrimary else if (isNightMode) EmeraldPrimary else DayEmeraldDeep,
        animationSpec = tween(durationMillis = 300),
        label = "alignment_glow"
    )

    val cardinalDirection = when (((azimuthDegrees + 22.5) % 360 / 45).toInt()) {
        0 -> "N"
        1 -> "NE"
        2 -> "E"
        3 -> "SE"
        4 -> "S"
        5 -> "SW"
        6 -> "W"
        7 -> "NW"
        else -> "N"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Alignment Banner Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isAligned) {
                    if (isNightMode) Color(0xFF1B432B) else Color(0xFFE4F4E8)
                } else {
                    if (isNightMode) Color(0xFF0F2E24) else Color(0xFFF7F3E9)
                },
                border = BorderStroke(
                    if (isAligned) 2.dp else 1.dp,
                    if (isAligned) GoldPrimary else if (isNightMode) Color(0xFF1E5242) else Color(0xFFDCC89E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("qibla_status_banner")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isAligned) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Aligned",
                                tint = if (isNightMode) GoldLight else Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isAligned) {
                                "🕋 Facing the Kaaba (Qibla) ✓"
                            } else if (relativeAngle > 0) {
                                "Turn ${(relativeAngle.roundToInt())}° to the Right ➔"
                            } else {
                                "Turn ${(-relativeAngle.roundToInt())}° to the Left ⬅"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) {
                                if (isNightMode) GoldLight else DayEmeraldDeep
                            } else {
                                if (isNightMode) Color.White else Color(0xFF263238)
                            }
                        )
                    }

                    Text(
                        text = if (isAligned) {
                            "Alhamdulillah — You are precisely aligned with the Holy Sanctuary"
                        } else {
                            "Rotate your device until the gold Kaaba needle points straight up"
                        },
                        fontSize = 11.sp,
                        color = if (isNightMode) Color.White.copy(alpha = 0.75f) else Color(0xFF5A7268),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fixed Top Heading Indicator Arrow
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Device Facing Direction",
                tint = if (isAligned) GoldPrimary else if (isNightMode) GoldLight.copy(alpha = 0.7f) else DayGoldAccent,
                modifier = Modifier
                    .size(26.dp)
                    .padding(bottom = 2.dp)
            )

            // Compass Dial Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(290.dp)
                    .testTag("qibla_compass_dial")
            ) {
                // Background Outer Glow
                Canvas(modifier = Modifier.size(290.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                alignmentColor.copy(alpha = if (isAligned) 0.40f else 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width / 2f
                        )
                    )
                    drawCircle(
                        color = if (isAligned) GoldPrimary else if (isNightMode) Color(0xFF134235) else Color(0xFFD7C7A3),
                        radius = size.width / 2f - 8f,
                        style = Stroke(width = if (isAligned) 4f else 2.5f)
                    )
                }

                // Rotating Compass Dial
                Canvas(
                    modifier = Modifier
                        .size(266.dp)
                        .rotate(animatedRotation)
                        .testTag("qibla_compass_canvas")
                ) {
                    val radius = size.width / 2f
                    val center = Offset(radius, radius)

                    // Draw Dial Marks & Cardinal Points
                    for (i in 0 until 360 step 15) {
                        val angleRad = Math.toRadians((i - 90).toDouble())
                        val isCardinal = i % 90 == 0
                        val isSemiCardinal = i % 45 == 0 && !isCardinal
                        val len = when {
                            isCardinal -> 24f
                            isSemiCardinal -> 16f
                            else -> 10f
                        }
                        val start = Offset(
                            (center.x + (radius - len - 10) * cos(angleRad)).toFloat(),
                            (center.y + (radius - len - 10) * sin(angleRad)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (radius - 10) * cos(angleRad)).toFloat(),
                            (center.y + (radius - 10) * sin(angleRad)).toFloat()
                        )
                        drawLine(
                            color = when {
                                i == 0 -> Color(0xFFE53935) // North is Red
                                isCardinal -> if (isNightMode) GoldPrimary else DayEmeraldDeep
                                else -> if (isNightMode) Color(0xFF2C6B58) else Color(0xFFB0A080)
                            },
                            start = start,
                            end = end,
                            strokeWidth = if (isCardinal) 4.5f else if (isSemiCardinal) 3f else 1.5f
                        )
                    }

                    // Draw Qibla Pointer Indicator (Ray to Kaaba)
                    val qiblaRad = Math.toRadians((qiblaBearing - 90.0))
                    val qiblaTip = Offset(
                        (center.x + (radius - 36) * cos(qiblaRad)).toFloat(),
                        (center.y + (radius - 36) * sin(qiblaRad)).toFloat()
                    )

                    // Kaaba Ray Line
                    drawLine(
                        color = Color(0xFFD4AF37),
                        start = center,
                        end = qiblaTip,
                        strokeWidth = 4.5f
                    )

                    // Kaaba Direction Marker Dot
                    drawCircle(
                        color = Color(0xFFF9E79F),
                        radius = 12f,
                        center = qiblaTip
                    )
                    drawCircle(
                        color = Color(0xFF996515),
                        radius = 12f,
                        center = qiblaTip,
                        style = Stroke(width = 2.5f)
                    )
                }

                // Center Kaaba Emblem
                Surface(
                    shape = CircleShape,
                    color = if (isAligned) GoldPrimary else if (isNightMode) Color(0xFF0F362A) else Color(0xFFFFFDF8),
                    border = BorderStroke(2.5.dp, GoldPrimary),
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🕋", fontSize = 30.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Compass Telemetry Cards (2x2 Grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Qibla Bearing
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNightMode) Color(0xFF0D2A21) else Color(0xFFF8F4EA)
                        ),
                        border = BorderStroke(1.dp, if (isNightMode) Color(0xFF1B4E41) else Color(0xFFE2D4B7))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Qibla Bearing",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNightMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B5838)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${result.qiblaBearing}°",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else DayEmeraldDeep
                            )
                            Text(
                                text = "from True North",
                                fontSize = 10.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8A7650)
                            )
                        }
                    }

                    // Device Heading
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNightMode) Color(0xFF0D2A21) else Color(0xFFF8F4EA)
                        ),
                        border = BorderStroke(1.dp, if (isNightMode) Color(0xFF1B4E41) else Color(0xFFE2D4B7))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Current Heading",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNightMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B5838)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${azimuthDegrees.roundToInt()}° $cardinalDirection",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else DayEmeraldDeep
                            )
                            Text(
                                text = if (isAligned) "Aligned with Qibla" else "Deviation: ${abs(relativeAngle.roundToInt())}°",
                                fontSize = 10.sp,
                                color = if (isAligned) (if (isNightMode) GoldPrimary else DayEmeraldDeep) else (if (isNightMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8A7650))
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Distance to Kaaba
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNightMode) Color(0xFF0D2A21) else Color(0xFFF8F4EA)
                        ),
                        border = BorderStroke(1.dp, if (isNightMode) Color(0xFF1B4E41) else Color(0xFFE2D4B7))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Distance to Makkah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNightMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B5838)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${result.distanceToMakkahKm.roundToInt()} km",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else DayEmeraldDeep
                            )
                            Text(
                                text = "from ${selectedCity.name}",
                                fontSize = 10.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8A7650)
                            )
                        }
                    }

                    // Holy Kaaba Coordinates
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNightMode) Color(0xFF0D2A21) else Color(0xFFF8F4EA)
                        ),
                        border = BorderStroke(1.dp, if (isNightMode) Color(0xFF1B4E41) else Color(0xFFE2D4B7))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Holy Sanctuary",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNightMode) Color.White.copy(alpha = 0.65f) else Color(0xFF6B5838)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "21.42° N, 39.83° E",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) GoldLight else DayEmeraldDeep
                            )
                            Text(
                                text = "Makkah al-Mukarramah",
                                fontSize = 10.sp,
                                color = if (isNightMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8A7650)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calibration Guidance & Sensor Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF092019) else Color(0xFFF4EDE0)
                ),
                border = BorderStroke(1.dp, if (isNightMode) Color(0xFF143B30) else Color(0xFFE5D7BE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Calibration",
                        tint = if (isNightMode) GoldPrimary else DayGoldAccent,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sensor Calibration & Guidance",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) GoldLight else DayEmeraldDeep
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Hold your device flat like a physical compass in your palm. If the heading drifts, move the phone smoothly in a figure-8 motion away from metallic surfaces, laptops, or magnetic cases.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = if (isNightMode) Color.White.copy(alpha = 0.75f) else Color(0xFF5A7268)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSensorAvailable) "✓ Compass sensor active (3-Axis Vector)" else "⚠️ Compass sensor unavailable on device",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSensorAvailable) (if (isNightMode) GoldLight else DayEmeraldDeep) else Color(0xFFE53935)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityPickerDialog(
    currentCity: CityLocation,
    onDismiss: () -> Unit,
    onSelectCity: (CityLocation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = CityLocations.predefinedCities.filter {
        searchQuery.isBlank() ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.country.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select City for Qibla Bearing",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city or country...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(filteredCities) { city ->
                        val isSelected = city.name.equals(currentCity.name, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCity(city) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.name,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(text = city.country, fontSize = 11.sp, color = Color.Gray)
                            }
                            if (isSelected) {
                                Text(text = "✓", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
