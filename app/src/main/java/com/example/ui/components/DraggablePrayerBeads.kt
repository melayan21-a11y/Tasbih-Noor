package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LapisBase
import com.example.ui.theme.LapisDark
import com.example.ui.theme.LapisLight
import com.example.ui.theme.OliveWoodBase
import com.example.ui.theme.OliveWoodDark
import com.example.ui.theme.OliveWoodLight
import com.example.ui.theme.PearlBase
import com.example.ui.theme.PearlDark
import com.example.ui.theme.PearlLight
import com.example.ui.theme.RoyalJadeBase
import com.example.ui.theme.RoyalJadeDark
import com.example.ui.theme.RoyalJadeLight
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class BeadTension(
    val title: String,
    val description: String,
    val dragDistancePx: Float
) {
    FIRM(
        title = "Firm Tension",
        description = "High resistance, prevents accidental skips",
        dragDistancePx = 175f
    ),
    STANDARD(
        title = "Standard Tension",
        description = "Authentic misbaha cord tension",
        dragDistancePx = 130f
    ),
    LIGHT(
        title = "Light Touch",
        description = "Gentle glide for rapid recitations",
        dragDistancePx = 85f
    )
}

enum class BeadMaterial(
    val displayName: String,
    val baseColor: Color,
    val lightColor: Color,
    val darkColor: Color,
    val accentColor: Color
) {
    OLIVE_WOOD(
        displayName = "Madinah Olive Wood",
        baseColor = OliveWoodBase,
        lightColor = OliveWoodLight,
        darkColor = OliveWoodDark,
        accentColor = GoldPrimary
    ),
    ROYAL_JADE(
        displayName = "Royal Emerald Jade",
        baseColor = RoyalJadeBase,
        lightColor = RoyalJadeLight,
        darkColor = RoyalJadeDark,
        accentColor = GoldLight
    ),
    PEARL(
        displayName = "Pearl of Noor",
        baseColor = PearlBase,
        lightColor = PearlLight,
        darkColor = PearlDark,
        accentColor = Color(0xFFC0C0C0)
    ),
    LAPIS_LAZULI(
        displayName = "Celestial Lapis",
        baseColor = LapisBase,
        lightColor = LapisLight,
        darkColor = LapisDark,
        accentColor = GoldPrimary
    )
}

@Composable
fun DraggablePrayerBeads(
    currentCount: Int,
    targetCount: Int,
    material: BeadMaterial,
    tension: BeadTension,
    singleBeadLock: Boolean,
    isNightMode: Boolean,
    onBeadPulled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // Cumulative continuous bead position offset
    val beadOffsetAnim = remember { Animatable(0f) }
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }
    var hasPassedBeadInCurrentGesture by remember { mutableStateOf(false) }

    val threshold = tension.dragDistancePx

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Single tap advances exactly one bead smoothly
                        coroutineScope.launch {
                            beadOffsetAnim.animateTo(
                                targetValue = beadOffsetAnim.value + 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        onBeadPulled()
                    }
                )
            }
            .pointerInput(tension, singleBeadLock) {
                detectDragGestures(
                    onDragStart = {
                        accumulatedDrag = 0f
                        hasPassedBeadInCurrentGesture = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaX = -dragAmount.x + (dragAmount.y * 0.20f)

                        // If single-bead lock is enabled and the user has already pulled 1 bead in this swipe:
                        // Apply strong rubber-band resistance so it cannot accidentally scroll 3 to 5 beads!
                        if (singleBeadLock && hasPassedBeadInCurrentGesture) {
                            val resistanceDamping = 0.12f
                            coroutineScope.launch {
                                beadOffsetAnim.snapTo(beadOffsetAnim.value + (deltaX * resistanceDamping / threshold))
                            }
                            return@detectDragGestures
                        }

                        accumulatedDrag += deltaX
                        coroutineScope.launch {
                            beadOffsetAnim.snapTo(beadOffsetAnim.value + (deltaX / threshold))
                        }

                        if (abs(accumulatedDrag) >= threshold) {
                            onBeadPulled()
                            accumulatedDrag = 0f
                            if (singleBeadLock) {
                                hasPassedBeadInCurrentGesture = true
                            }
                        }
                    },
                    onDragEnd = {
                        // Spring snap to nearest bead position
                        val target = kotlin.math.round(beadOffsetAnim.value)
                        coroutineScope.launch {
                            beadOffsetAnim.animateTo(
                                targetValue = target,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        accumulatedDrag = 0f
                        hasPassedBeadInCurrentGesture = false
                    },
                    onDragCancel = {
                        val target = kotlin.math.round(beadOffsetAnim.value)
                        coroutineScope.launch {
                            beadOffsetAnim.animateTo(target)
                        }
                        accumulatedDrag = 0f
                        hasPassedBeadInCurrentGesture = false
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val centerY = height * 0.50f
            val centerX = width * 0.50f

            // LARGE BEADS: radius ~46f to 52f (~92-104px diameter)
            val beadRadius = (width * 0.105f).coerceIn(40f, 54f)
            val beadSpacing = beadRadius * 2.30f

            // 1. Draw horizontal braided silk rosary string with gentle natural sag
            val stringSag = 16f
            val stringPath = Path().apply {
                moveTo(0f, centerY - 6f)
                cubicTo(
                    width * 0.25f, centerY + stringSag,
                    width * 0.75f, centerY + stringSag,
                    width, centerY - 6f
                )
            }

            // String shadow
            drawPath(
                path = stringPath,
                color = Color.Black.copy(alpha = if (isNightMode) 0.6f else 0.18f),
                style = Stroke(width = 8.5f, cap = StrokeCap.Round)
            )

            // Braided silk string cord
            val cordColor = if (isNightMode) GoldPrimary.copy(alpha = 0.85f) else Color(0xFF8B6B38)
            drawPath(
                path = stringPath,
                color = cordColor,
                style = Stroke(width = 6.0f, cap = StrokeCap.Round)
            )

            // 2. Draw Center Counter Divider / Indicator Line
            drawCenterMarker(
                centerX = centerX,
                centerY = centerY + stringSag * 0.85f,
                beadRadius = beadRadius,
                isNightMode = isNightMode
            )

            // 3. Render Large Beads sliding across the screen
            val currentOffsetFraction = beadOffsetAnim.value
            val baseBeadIndex = currentOffsetFraction.toInt()
            val fractionalShift = (currentOffsetFraction - baseBeadIndex) * beadSpacing

            // Determine how many beads can span across screen
            val beadsHalfCount = (width / beadSpacing).toInt() + 2

            for (i in -beadsHalfCount..beadsHalfCount) {
                val beadIndex = baseBeadIndex + i
                val beadX = centerX + (i * beadSpacing) - fractionalShift

                if (beadX < -beadRadius * 2 || beadX > width + beadRadius * 2) continue

                val normalizedX = (beadX - centerX) / (width * 0.5f)
                val curveY = centerY + stringSag * (1f - (normalizedX * normalizedX * 0.6f).coerceIn(0f, 1f))

                val isMarker = (abs(beadIndex) % 11 == 0) && (beadIndex != 0)
                val distanceFromCenter = abs(beadX - centerX)
                val isHighlighted = distanceFromCenter < (beadSpacing * 0.45f)

                drawLarge3DBead(
                    center = Offset(beadX, curveY),
                    radius = if (isMarker) beadRadius * 1.08f else beadRadius,
                    material = material,
                    isMarker = isMarker,
                    isHighlighted = isHighlighted,
                    isNightMode = isNightMode
                )
            }
        }
    }
}

private fun DrawScope.drawLarge3DBead(
    center: Offset,
    radius: Float,
    material: BeadMaterial,
    isMarker: Boolean,
    isHighlighted: Boolean,
    isNightMode: Boolean
) {
    // 1. Soft Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = if (isNightMode) 0.70f else 0.30f),
        radius = radius * 1.10f,
        center = Offset(center.x + 3.5f, center.y + 6.0f)
    )

    // 2. Ambient Occlusion edge
    drawCircle(
        color = Color.Black.copy(alpha = 0.40f),
        radius = radius,
        center = center
    )

    // 3. Base Bead 3D Radial Sphere Gradient
    val baseColor = if (isMarker) GoldPrimary else material.baseColor
    val lightColor = if (isMarker) GoldLight else material.lightColor
    val darkColor = if (isMarker) GoldDark else material.darkColor

    val beadGradient = Brush.radialGradient(
        colors = listOf(
            lightColor,
            baseColor,
            darkColor,
            Color.Black.copy(alpha = 0.65f)
        ),
        center = Offset(center.x - radius * 0.34f, center.y - radius * 0.34f),
        radius = radius * 1.35f
    )

    drawCircle(
        brush = beadGradient,
        radius = radius,
        center = center
    )

    // 4. Bead Core Cord Hole
    drawCircle(
        color = Color.Black.copy(alpha = 0.55f),
        radius = radius * 0.14f,
        center = Offset(center.x - radius * 0.88f, center.y)
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.55f),
        radius = radius * 0.14f,
        center = Offset(center.x + radius * 0.88f, center.y)
    )

    // 5. Islamic Gold Filigree Band on Marker Beads
    if (isMarker) {
        drawCircle(
            color = GoldLight,
            radius = radius * 0.80f,
            center = center,
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = GoldPrimary,
            radius = radius * 0.45f,
            center = center,
            style = Stroke(width = 2.0f)
        )
    }

    // 6. Specular Glint Highlight
    val glintAlpha = if (material == BeadMaterial.ROYAL_JADE || material == BeadMaterial.PEARL) 0.90f else 0.70f
    drawCircle(
        color = Color.White.copy(alpha = glintAlpha),
        radius = radius * 0.24f,
        center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = radius * 0.08f,
        center = Offset(center.x - radius * 0.22f, center.y - radius * 0.44f)
    )

    // 7. Active Crossing Aura Glow when passing center marker
    if (isHighlighted) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GoldLight.copy(alpha = 0.60f),
                    GoldPrimary.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 2.1f
            ),
            radius = radius * 2.1f,
            center = center
        )
    }
}

private fun DrawScope.drawCenterMarker(
    centerX: Float,
    centerY: Float,
    beadRadius: Float,
    isNightMode: Boolean
) {
    val markerColor = if (isNightMode) GoldLight else GoldPrimary
    val glowColor = if (isNightMode) GoldPrimary.copy(alpha = 0.35f) else GoldLight.copy(alpha = 0.45f)

    val topY = centerY - beadRadius * 1.6f
    val bottomY = centerY + beadRadius * 1.6f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, Color.Transparent),
            center = Offset(centerX, centerY),
            radius = beadRadius * 1.8f
        ),
        radius = beadRadius * 1.8f,
        center = Offset(centerX, centerY)
    )

    val topArrow = Path().apply {
        moveTo(centerX, topY + 12f)
        lineTo(centerX - 8f, topY)
        lineTo(centerX + 8f, topY)
        close()
    }
    drawPath(path = topArrow, color = markerColor)

    val bottomArrow = Path().apply {
        moveTo(centerX, bottomY - 12f)
        lineTo(centerX - 8f, bottomY)
        lineTo(centerX + 8f, bottomY)
        close()
    }
    drawPath(path = bottomArrow, color = markerColor)

    drawLine(
        color = markerColor.copy(alpha = 0.4f),
        start = Offset(centerX, topY + 16f),
        end = Offset(centerX, bottomY - 16f),
        strokeWidth = 2.0f,
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
}
