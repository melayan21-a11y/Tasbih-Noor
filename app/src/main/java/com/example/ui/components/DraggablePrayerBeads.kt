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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
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
import kotlin.math.cos
import kotlin.math.sin

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
    val accentColor: Color,
    val isTextured: Boolean = false
) {
    OLIVE_WOOD(
        displayName = "Madinah Olive Wood",
        baseColor = OliveWoodBase,
        lightColor = OliveWoodLight,
        darkColor = OliveWoodDark,
        accentColor = GoldPrimary
    ),
    TIGER_EYE(
        displayName = "Tiger's Eye Agate",
        baseColor = Color(0xFF8C531B),
        lightColor = Color(0xFFE5A836),
        darkColor = Color(0xFF381905),
        accentColor = GoldLight,
        isTextured = true
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
    ),
    BLACK_ONYX(
        displayName = "Makkah Black Onyx",
        baseColor = Color(0xFF1D2027),
        lightColor = Color(0xFF5D6678),
        darkColor = Color(0xFF090A0D),
        accentColor = GoldPrimary
    ),
    AMBER_HONEY(
        displayName = "Baltic Honey Amber",
        baseColor = Color(0xFFB85D14),
        lightColor = Color(0xFFF6B042),
        darkColor = Color(0xFF5A2704),
        accentColor = Color(0xFFFFE599),
        isTextured = true
    )
}

enum class BeadDesign(
    val title: String,
    val subtitle: String
) {
    CLASSIC_SMOOTH(
        title = "Classic Smooth",
        subtitle = "Pure natural grain & mirror luster"
    ),
    MAMLUK_GOLD_BAND(
        title = "Mamluk Gold Band",
        subtitle = "Inlaid brass meridian filigree belt"
    ),
    RUB_AL_HIZB(
        title = "Rub al-Hizb Star",
        subtitle = "Carved 8-pointed Islamic star motif"
    ),
    ANDALUSIAN_LATTICE(
        title = "Andalusian Lattice",
        subtitle = "Fine diamond geometric trellis engraving"
    ),
    CRESCENT_INLAY(
        title = "Hilal & Star Inlay",
        subtitle = "Embossed crescent moon & star emblem"
    )
}

@Composable
fun BeadCraftPreview(
    material: BeadMaterial,
    design: BeadDesign,
    isNightMode: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val radius = (size.minDimension * 0.42f).coerceAtLeast(8f)
        drawLarge3DBead(
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = radius,
            material = material,
            design = design,
            isMarker = false,
            isHighlighted = false,
            isNightMode = isNightMode
        )
    }
}

@Composable
fun DraggablePrayerBeads(
    currentCount: Int,
    targetCount: Int,
    material: BeadMaterial,
    design: BeadDesign = BeadDesign.CLASSIC_SMOOTH,
    tension: BeadTension,
    singleBeadLock: Boolean,
    isNightMode: Boolean,
    cordColor: Color = GoldDark,
    themeAccent: Color = GoldPrimary,
    themeAccentLight: Color = GoldLight,
    onBeadPulled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // Cumulative continuous bead position offset
    val beadOffsetAnim = remember { Animatable(0f) }
    // Pendulum swing angle (in degrees) for the Karkousha silk tassel
    val tasselAngleAnim = remember { Animatable(0f) }
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }
    var hasPassedBeadInCurrentGesture by remember { mutableStateOf(false) }

    val threshold = tension.dragDistancePx

    fun triggerTasselSwing(impulseDeg: Float) {
        coroutineScope.launch {
            val clamped = impulseDeg.coerceIn(-22f, 22f)
            tasselAngleAnim.snapTo(clamped)
            tasselAngleAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.34f,
                    stiffness = 110f
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Single tap advances exactly one bead smoothly and gently swings the tassel
                        coroutineScope.launch {
                            beadOffsetAnim.animateTo(
                                targetValue = beadOffsetAnim.value + 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        triggerTasselSwing(11f)
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

                        // Gently tilt tassel during drag
                        val dragTilt = (tasselAngleAnim.value * 0.72f + (deltaX * 0.28f)).coerceIn(-22f, 22f)
                        coroutineScope.launch {
                            tasselAngleAnim.snapTo(dragTilt)
                        }

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
                        // Spring snap to nearest bead position & let Karkousha tassel swing naturally back to rest
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
                        coroutineScope.launch {
                            tasselAngleAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.32f,
                                    stiffness = 105f
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
                        coroutineScope.launch {
                            tasselAngleAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 0.35f,
                                    stiffness = 120f
                                )
                            )
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

            // Position the bead strand slightly above vertical midpoint so the Karkousha tassel hangs gracefully underneath
            val beadRadius = (width * 0.105f).coerceIn(38f, 52f)
            val beadSpacing = beadRadius * 2.30f
            val centerY = (height * 0.39f).coerceAtLeast(beadRadius * 1.48f)
            val centerX = width * 0.50f

            // 1. Braided silk rosary string with natural catenary sag
            val stringSag = 16f
            val stringPath = Path().apply {
                moveTo(0f, centerY - 6f)
                cubicTo(
                    width * 0.25f, centerY + stringSag,
                    width * 0.75f, centerY + stringSag,
                    width, centerY - 6f
                )
            }

            // Soft cord drop shadow
            drawPath(
                path = stringPath,
                color = Color.Black.copy(alpha = if (isNightMode) 0.65f else 0.20f),
                style = Stroke(width = 10.0f, cap = StrokeCap.Round)
            )

            // Base woven silk cord tinted to the active sanctuary theme
            val cordDark = cordColor
            val cordBright = themeAccentLight
            drawPath(
                path = stringPath,
                color = cordDark,
                style = Stroke(width = 6.8f, cap = StrokeCap.Round)
            )

            // Subtle helical braid weave highlights along the cord
            drawPath(
                path = stringPath,
                color = cordBright.copy(alpha = 0.85f),
                style = Stroke(
                    width = 3.2f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 9f), phase = 0f)
                )
            )

            // 2. Center Counter Divider & Swinging Karkousha Tassel below the clasp
            val centerCordY = centerY + stringSag * 0.85f
            drawCenterMarkerAndTassel(
                centerX = centerX,
                centerY = centerCordY,
                beadRadius = beadRadius,
                canvasHeight = height,
                tasselAngleDeg = tasselAngleAnim.value,
                material = material,
                isNightMode = isNightMode,
                markerColorTheme = themeAccent,
                markerLightTheme = themeAccentLight
            )

            // 3. Render Large 3D Beads sliding across the cord
            val currentOffsetFraction = beadOffsetAnim.value
            val baseBeadIndex = currentOffsetFraction.toInt()
            val fractionalShift = (currentOffsetFraction - baseBeadIndex) * beadSpacing

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
                    design = design,
                    isMarker = isMarker,
                    isHighlighted = isHighlighted,
                    isNightMode = isNightMode,
                    highlightAccent = themeAccent,
                    highlightAccentLight = themeAccentLight
                )
            }
        }
    }
}

private fun DrawScope.drawLarge3DBead(
    center: Offset,
    radius: Float,
    material: BeadMaterial,
    design: BeadDesign = BeadDesign.CLASSIC_SMOOTH,
    isMarker: Boolean,
    isHighlighted: Boolean,
    isNightMode: Boolean,
    highlightAccent: Color = GoldPrimary,
    highlightAccentLight: Color = GoldLight
) {
    // 1. Soft multi-layer Drop Shadow for tactile 3D lift off canvas
    drawCircle(
        color = Color.Black.copy(alpha = if (isNightMode) 0.55f else 0.16f),
        radius = radius * 1.16f,
        center = Offset(center.x + 4f, center.y + 8f)
    )
    drawCircle(
        color = Color.Black.copy(alpha = if (isNightMode) 0.65f else 0.26f),
        radius = radius * 1.04f,
        center = Offset(center.x + 2.5f, center.y + 4.5f)
    )

    // 2. Metallic Gold Ferrules (end-caps) flanking Marker Imamah beads on the cord
    if (isMarker) {
        val capOffset = radius * 0.98f
        val leftCap = Path().apply {
            moveTo(center.x - capOffset - radius * 0.22f, center.y - radius * 0.17f)
            lineTo(center.x - capOffset + radius * 0.10f, center.y - radius * 0.30f)
            lineTo(center.x - capOffset + radius * 0.10f, center.y + radius * 0.30f)
            lineTo(center.x - capOffset - radius * 0.22f, center.y + radius * 0.17f)
            close()
        }
        val rightCap = Path().apply {
            moveTo(center.x + capOffset + radius * 0.22f, center.y - radius * 0.17f)
            lineTo(center.x + capOffset - radius * 0.10f, center.y - radius * 0.30f)
            lineTo(center.x + capOffset - radius * 0.10f, center.y + radius * 0.30f)
            lineTo(center.x + capOffset + radius * 0.22f, center.y + radius * 0.17f)
            close()
        }
        val capBrush = Brush.verticalGradient(
            colors = listOf(GoldLight, GoldPrimary, GoldDark),
            startY = center.y - radius * 0.30f,
            endY = center.y + radius * 0.30f
        )
        drawPath(path = leftCap, brush = capBrush)
        drawPath(path = rightCap, brush = capBrush)
    }

    // 3. Ambient Occlusion edge
    drawCircle(
        color = Color.Black.copy(alpha = 0.40f),
        radius = radius,
        center = center
    )

    // 4. Base Bead 3D Radial Sphere Gradient
    val baseColor = if (isMarker) GoldPrimary else material.baseColor
    val lightColor = if (isMarker) GoldLight else material.lightColor
    val darkColor = if (isMarker) GoldDark else material.darkColor

    val beadGradient = Brush.radialGradient(
        colors = listOf(
            lightColor,
            baseColor,
            darkColor,
            Color.Black.copy(alpha = 0.68f)
        ),
        center = Offset(center.x - radius * 0.34f, center.y - radius * 0.34f),
        radius = radius * 1.35f
    )

    drawCircle(
        brush = beadGradient,
        radius = radius,
        center = center
    )

    // 4b. Material-Specific Mineral & Artisan Textures
    val beadClip = Path().apply {
        addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
    }

    if (!isMarker) {
        when (material) {
            BeadMaterial.TIGER_EYE -> {
                clipPath(beadClip) {
                    val silkBand = Path().apply {
                        moveTo(center.x - radius * 1.2f, center.y - radius * 0.45f)
                        cubicTo(
                            center.x - radius * 0.3f, center.y - radius * 0.6f,
                            center.x + radius * 0.4f, center.y - radius * 0.1f,
                            center.x + radius * 1.2f, center.y - radius * 0.25f
                        )
                        lineTo(center.x + radius * 1.2f, center.y + radius * 0.35f)
                        cubicTo(
                            center.x + radius * 0.4f, center.y + radius * 0.5f,
                            center.x - radius * 0.3f, center.y + radius * 0.05f,
                            center.x - radius * 1.2f, center.y + radius * 0.2f
                        )
                        close()
                    }
                    drawPath(
                        path = silkBand,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE5A638).copy(alpha = 0.85f),
                                Color(0xFFFFEE93).copy(alpha = 0.95f),
                                Color(0xFF9E5716).copy(alpha = 0.70f)
                            ),
                            start = Offset(center.x - radius, center.y - radius * 0.5f),
                            end = Offset(center.x + radius, center.y + radius * 0.5f)
                        )
                    )

                    val veinStreak = Path().apply {
                        moveTo(center.x - radius * 1.2f, center.y + radius * 0.25f)
                        cubicTo(
                            center.x - radius * 0.2f, center.y + radius * 0.15f,
                            center.x + radius * 0.5f, center.y + radius * 0.65f,
                            center.x + radius * 1.2f, center.y + radius * 0.5f
                        )
                        lineTo(center.x + radius * 1.2f, center.y + radius * 0.75f)
                        cubicTo(
                            center.x + radius * 0.5f, center.y + radius * 0.9f,
                            center.x - radius * 0.2f, center.y + radius * 0.4f,
                            center.x - radius * 1.2f, center.y + radius * 0.5f
                        )
                        close()
                    }
                    drawPath(
                        path = veinStreak,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2C1304).copy(alpha = 0.85f),
                                Color(0xFF4A2308).copy(alpha = 0.75f),
                                Color(0xFF220D02).copy(alpha = 0.90f)
                            ),
                            start = Offset(center.x - radius, center.y),
                            end = Offset(center.x + radius, center.y + radius)
                        )
                    )

                    drawLine(
                        color = Color(0xFFFFF3C4).copy(alpha = 0.55f),
                        start = Offset(center.x - radius * 0.85f, center.y - radius * 0.12f),
                        end = Offset(center.x + radius * 0.85f, center.y + radius * 0.08f),
                        strokeWidth = 2.0f
                    )
                    drawLine(
                        color = Color(0xFF381A05).copy(alpha = 0.60f),
                        start = Offset(center.x - radius * 0.9f, center.y + radius * 0.18f),
                        end = Offset(center.x + radius * 0.8f, center.y + radius * 0.38f),
                        strokeWidth = 2.5f
                    )
                }
            }
            BeadMaterial.OLIVE_WOOD -> {
                clipPath(beadClip) {
                    drawCircle(
                        color = Color(0xFF4E2A0E).copy(alpha = 0.24f),
                        radius = radius * 0.74f,
                        center = Offset(center.x - radius * 0.25f, center.y),
                        style = Stroke(width = 1.6f)
                    )
                    drawCircle(
                        color = Color(0xFF4E2A0E).copy(alpha = 0.18f),
                        radius = radius * 0.46f,
                        center = Offset(center.x - radius * 0.25f, center.y),
                        style = Stroke(width = 1.4f)
                    )
                }
            }
            BeadMaterial.LAPIS_LAZULI -> {
                clipPath(beadClip) {
                    // Subtle golden pyrite mineral flecks inside deep ultramarine stone
                    drawCircle(
                        color = GoldLight.copy(alpha = 0.65f),
                        radius = radius * 0.06f,
                        center = Offset(center.x + radius * 0.28f, center.y - radius * 0.18f)
                    )
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.55f),
                        radius = radius * 0.045f,
                        center = Offset(center.x - radius * 0.16f, center.y + radius * 0.32f)
                    )
                    drawCircle(
                        color = GoldLight.copy(alpha = 0.45f),
                        radius = radius * 0.035f,
                        center = Offset(center.x + radius * 0.10f, center.y + radius * 0.14f)
                    )
                }
            }
            BeadMaterial.ROYAL_JADE -> {
                clipPath(beadClip) {
                    // Translucent inner jade luminescence
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF66BB6A).copy(alpha = 0.32f), Color.Transparent),
                            center = Offset(center.x + radius * 0.22f, center.y + radius * 0.24f),
                            radius = radius * 0.75f
                        ),
                        radius = radius * 0.75f,
                        center = Offset(center.x + radius * 0.22f, center.y + radius * 0.24f)
                    )
                }
            }
            BeadMaterial.PEARL -> {
                clipPath(beadClip) {
                    // Iridescent rose-gold nacre sheen
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF0F5).copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(center.x + radius * 0.25f, center.y + radius * 0.25f),
                            radius = radius * 0.70f
                        ),
                        radius = radius * 0.70f,
                        center = Offset(center.x + radius * 0.25f, center.y + radius * 0.25f)
                    )
                }
            }
            BeadMaterial.BLACK_ONYX -> {
                clipPath(beadClip) {
                    // Deep mirror obsidian cool reflection crescent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF90A4AE).copy(alpha = 0.24f), Color.Transparent),
                            center = Offset(center.x + radius * 0.24f, center.y + radius * 0.24f),
                            radius = radius * 0.65f
                        ),
                        radius = radius * 0.65f,
                        center = Offset(center.x + radius * 0.24f, center.y + radius * 0.24f)
                    )
                }
            }
            BeadMaterial.AMBER_HONEY -> {
                clipPath(beadClip) {
                    // Warm translucent amber resin glow + tiny golden mineral inclusion
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.42f), Color.Transparent),
                            center = Offset(center.x + radius * 0.18f, center.y + radius * 0.18f),
                            radius = radius * 0.72f
                        ),
                        radius = radius * 0.72f,
                        center = Offset(center.x + radius * 0.18f, center.y + radius * 0.18f)
                    )
                    drawCircle(
                        color = Color(0xFF4A1D03).copy(alpha = 0.42f),
                        radius = radius * 0.06f,
                        center = Offset(center.x - radius * 0.22f, center.y + radius * 0.16f)
                    )
                }
            }
        }

        // 4c. Custom Islamic Bead Engraving & Filigree Craftsmanship
        drawIslamicBeadDesign(
            center = center,
            radius = radius,
            beadClip = beadClip,
            material = material,
            design = design
        )
    }

    // 5. Subsurface Rim Light Crescent (bottom-right bounced studio reflection)
    clipPath(beadClip) {
        drawCircle(
            color = lightColor.copy(alpha = if (isNightMode) 0.28f else 0.34f),
            radius = radius * 0.94f,
            center = Offset(center.x + radius * 0.06f, center.y + radius * 0.06f),
            style = Stroke(width = 2.4f)
        )
    }

    // 6. Bead Core Cord Hole
    drawCircle(
        color = Color.Black.copy(alpha = 0.58f),
        radius = radius * 0.13f,
        center = Offset(center.x - radius * 0.88f, center.y)
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.58f),
        radius = radius * 0.13f,
        center = Offset(center.x + radius * 0.88f, center.y)
    )

    // 7. Islamic Gold Filigree Band on Marker Beads
    if (isMarker) {
        drawCircle(
            color = GoldLight,
            radius = radius * 0.78f,
            center = center,
            style = Stroke(width = 3.0f)
        )
        drawCircle(
            color = GoldDark.copy(alpha = 0.85f),
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = 1.2f)
        )
        drawCircle(
            color = GoldLight.copy(alpha = 0.9f),
            radius = radius * 0.42f,
            center = center,
            style = Stroke(width = 2.0f)
        )
    }

    // 8. Crisp Dual Specular Glint Highlight
    val glintAlpha = if (material == BeadMaterial.ROYAL_JADE || material == BeadMaterial.PEARL || isMarker) 0.92f else 0.74f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = glintAlpha), Color.Transparent),
            center = Offset(center.x - radius * 0.36f, center.y - radius * 0.36f),
            radius = radius * 0.32f
        ),
        radius = radius * 0.32f,
        center = Offset(center.x - radius * 0.36f, center.y - radius * 0.36f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.88f),
        radius = radius * 0.085f,
        center = Offset(center.x - radius * 0.24f, center.y - radius * 0.42f)
    )

    // 9. Active Crossing Aura Glow when passing center marker
    if (isHighlighted) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    highlightAccentLight.copy(alpha = 0.58f),
                    highlightAccent.copy(alpha = 0.24f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 2.15f
            ),
            radius = radius * 2.15f,
            center = center
        )
    }
}

private fun DrawScope.drawCenterMarkerAndTassel(
    centerX: Float,
    centerY: Float,
    beadRadius: Float,
    canvasHeight: Float,
    tasselAngleDeg: Float,
    material: BeadMaterial,
    isNightMode: Boolean,
    markerColorTheme: Color = GoldPrimary,
    markerLightTheme: Color = GoldLight
) {
    val markerColor = if (isNightMode) markerLightTheme else markerColorTheme
    val glowColor = if (isNightMode) markerColorTheme.copy(alpha = 0.35f) else markerLightTheme.copy(alpha = 0.45f)

    val topY = centerY - beadRadius * 1.48f
    val bottomY = centerY + beadRadius * 1.28f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, Color.Transparent),
            center = Offset(centerX, centerY),
            radius = beadRadius * 1.85f
        ),
        radius = beadRadius * 1.85f,
        center = Offset(centerX, centerY)
    )

    val topArrow = Path().apply {
        moveTo(centerX, topY + 11f)
        lineTo(centerX - 8f, topY)
        lineTo(centerX + 8f, topY)
        close()
    }
    drawPath(path = topArrow, color = markerColor)

    val bottomArrow = Path().apply {
        moveTo(centerX, bottomY - 11f)
        lineTo(centerX - 7f, bottomY)
        lineTo(centerX + 7f, bottomY)
        close()
    }
    drawPath(path = bottomArrow, color = markerColor)

    drawLine(
        color = markerColor.copy(alpha = 0.35f),
        start = Offset(centerX, topY + 15f),
        end = Offset(centerX, bottomY - 14f),
        strokeWidth = 2.0f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    )

    // Swinging Islamic Karkousha Tassel anchored smoothly beneath the center divider
    val availableBelow = (canvasHeight - bottomY - 10f).coerceAtLeast(36f)
    val tasselTotalLen = availableBelow.coerceIn(48f, beadRadius * 1.95f)
    val pivot = Offset(centerX, bottomY + 3f)

    // Silk strand colors harmonized with bead material and lighting mode
    val silkBase = when (material) {
        BeadMaterial.ROYAL_JADE -> if (isNightMode) EmeraldPrimary else EmeraldDeep
        BeadMaterial.LAPIS_LAZULI -> Color(0xFF1F4E8C)
        BeadMaterial.TIGER_EYE -> Color(0xFF8C4618)
        BeadMaterial.PEARL -> if (isNightMode) GoldPrimary else Color(0xFF9E8A63)
        BeadMaterial.OLIVE_WOOD -> if (isNightMode) Color(0xFF1E6E58) else EmeraldDeep
        BeadMaterial.BLACK_ONYX -> if (isNightMode) GoldDark else Color(0xFF262A34)
        BeadMaterial.AMBER_HONEY -> Color(0xFF9E4B0E)
    }
    val silkHighlight = when (material) {
        BeadMaterial.ROYAL_JADE -> Color(0xFF45A888)
        BeadMaterial.LAPIS_LAZULI -> Color(0xFF4F81C7)
        BeadMaterial.TIGER_EYE -> Color(0xFFD4883B)
        BeadMaterial.PEARL -> GoldLight
        BeadMaterial.OLIVE_WOOD -> GoldPrimary
        BeadMaterial.BLACK_ONYX -> GoldLight
        BeadMaterial.AMBER_HONEY -> Color(0xFFF6B042)
    }

    rotate(degrees = tasselAngleDeg, pivot = pivot) {
        // Ornate gold clasp ring
        drawCircle(
            color = GoldPrimary,
            radius = 5.5f,
            center = pivot,
            style = Stroke(width = 2.6f)
        )

        // Conical braided gold knot cap (head of Karkousha)
        val capTopY = pivot.y + 5f
        val capHeight = (tasselTotalLen * 0.25f).coerceIn(12f, 22f)
        val capBottomY = capTopY + capHeight
        val capTopHalfW = 5.5f
        val capBottomHalfW = 11.5f

        val knotCapPath = Path().apply {
            moveTo(centerX - capTopHalfW, capTopY)
            lineTo(centerX + capTopHalfW, capTopY)
            lineTo(centerX + capBottomHalfW, capBottomY)
            lineTo(centerX - capBottomHalfW, capBottomY)
            close()
        }
        drawPath(
            path = knotCapPath,
            brush = Brush.horizontalGradient(
                colors = listOf(GoldDark, GoldLight, GoldPrimary, GoldDark),
                startX = centerX - capBottomHalfW,
                endX = centerX + capBottomHalfW
            )
        )

        // Gold collar ring at base of knot cap
        drawLine(
            color = GoldLight,
            start = Offset(centerX - capBottomHalfW - 1f, capBottomY),
            end = Offset(centerX + capBottomHalfW + 1f, capBottomY),
            strokeWidth = 2.6f,
            cap = StrokeCap.Round
        )

        // Flowing silk fringe strands fanning out gently
        val fringeTopY = capBottomY + 2f
        val fringeBottomY = pivot.y + tasselTotalLen
        val strandCount = 9
        for (s in 0 until strandCount) {
            val fraction = (s / (strandCount - 1).toFloat()) - 0.5f // -0.5..+0.5
            val startX = centerX + fraction * (capBottomHalfW * 1.65f)
            val endX = centerX + fraction * (capBottomHalfW * 3.1f)
            val curveCtrlX = centerX + fraction * (capBottomHalfW * 2.1f)
            val strandEndY = fringeBottomY - abs(fraction) * 6f

            val strandPath = Path().apply {
                moveTo(startX, fringeTopY)
                quadraticTo(
                    curveCtrlX,
                    (fringeTopY + strandEndY) * 0.55f,
                    endX,
                    strandEndY
                )
            }
            val isAccentStrand = s % 2 == 1
            drawPath(
                path = strandPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isAccentStrand) silkHighlight else silkBase,
                        silkBase.copy(alpha = 0.85f),
                        (if (isAccentStrand) GoldLight else silkHighlight).copy(alpha = 0.72f)
                    ),
                    startY = fringeTopY,
                    endY = strandEndY
                ),
                style = Stroke(width = if (isAccentStrand) 2.2f else 2.8f, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawIslamicBeadDesign(
    center: Offset,
    radius: Float,
    beadClip: Path,
    material: BeadMaterial,
    design: BeadDesign
) {
    if (design == BeadDesign.CLASSIC_SMOOTH) return

    val inlayColor = if (material == BeadMaterial.PEARL) Color(0xFFAA7C22) else GoldLight
    val trimColor = if (material == BeadMaterial.PEARL) Color(0xFF6B4D12) else GoldPrimary
    val grooveColor = Color.Black.copy(alpha = 0.45f)

    clipPath(beadClip) {
        when (design) {
            BeadDesign.CLASSIC_SMOOTH -> Unit

            BeadDesign.MAMLUK_GOLD_BAND -> {
                // Vertical equatorial inlaid brass/gold filigree belt with recessed border grooves
                val bandHalfW = (radius * 0.14f).coerceAtLeast(2.2f)
                drawRect(
                    color = grooveColor,
                    topLeft = Offset(center.x - bandHalfW - 1.4f, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(bandHalfW * 2f + 2.8f, radius * 2f)
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(trimColor.copy(alpha = 0.72f), inlayColor, trimColor.copy(alpha = 0.65f)),
                        startY = center.y - radius,
                        endY = center.y + radius
                    ),
                    topLeft = Offset(center.x - bandHalfW, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(bandHalfW * 2f, radius * 2f)
                )
                // Fine beaded filigree dots down the center of the band
                for (step in -3..3) {
                    val dotY = center.y + step * (radius * 0.24f)
                    drawCircle(
                        color = grooveColor.copy(alpha = 0.55f),
                        radius = (radius * 0.045f).coerceAtLeast(1.1f),
                        center = Offset(center.x + 0.5f, dotY + 0.5f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = (radius * 0.04f).coerceAtLeast(1.0f),
                        center = Offset(center.x, dotY)
                    )
                }
            }

            BeadDesign.RUB_AL_HIZB -> {
                // Engraved 8-pointed Rub al-Hizb (two overlapping rotated squares + center medallion)
                val sqHalf = radius * 0.42f
                val strokeW = (radius * 0.055f).coerceAtLeast(1.3f)

                fun drawSquare(angleDeg: Float, color: Color, offsetPx: Float = 0f, width: Float) {
                    rotate(degrees = angleDeg, pivot = Offset(center.x + offsetPx, center.y + offsetPx)) {
                        drawRect(
                            color = color,
                            topLeft = Offset(center.x - sqHalf + offsetPx, center.y - sqHalf + offsetPx),
                            size = androidx.compose.ui.geometry.Size(sqHalf * 2f, sqHalf * 2f),
                            style = Stroke(width = width)
                        )
                    }
                }

                // Shadow groove then bright gold inlay
                drawSquare(0f, grooveColor, offsetPx = 1.0f, width = strokeW + 0.8f)
                drawSquare(45f, grooveColor, offsetPx = 1.0f, width = strokeW + 0.8f)
                drawSquare(0f, inlayColor.copy(alpha = 0.90f), width = strokeW)
                drawSquare(45f, inlayColor.copy(alpha = 0.90f), width = strokeW)

                // Inner center star pearl
                drawCircle(
                    color = trimColor.copy(alpha = 0.9f),
                    radius = radius * 0.12f,
                    center = center,
                    style = Stroke(width = strokeW * 0.85f)
                )
                drawCircle(
                    color = inlayColor,
                    radius = radius * 0.05f,
                    center = center
                )
            }

            BeadDesign.ANDALUSIAN_LATTICE -> {
                // Moroccan diamond cross-hatch trellis carved across the bead face
                val strokeW = (radius * 0.042f).coerceAtLeast(1.1f)
                val step = radius * 0.42f
                for (k in -2..2) {
                    val shift = k * step
                    drawLine(
                        color = grooveColor,
                        start = Offset(center.x - radius + shift, center.y - radius + 1f),
                        end = Offset(center.x + radius + shift, center.y + radius + 1f),
                        strokeWidth = strokeW + 0.6f
                    )
                    drawLine(
                        color = inlayColor.copy(alpha = 0.62f),
                        start = Offset(center.x - radius + shift, center.y - radius),
                        end = Offset(center.x + radius + shift, center.y + radius),
                        strokeWidth = strokeW
                    )
                    drawLine(
                        color = grooveColor,
                        start = Offset(center.x + radius + shift, center.y - radius + 1f),
                        end = Offset(center.x - radius + shift, center.y + radius + 1f),
                        strokeWidth = strokeW + 0.6f
                    )
                    drawLine(
                        color = inlayColor.copy(alpha = 0.62f),
                        start = Offset(center.x + radius + shift, center.y - radius),
                        end = Offset(center.x - radius + shift, center.y + radius),
                        strokeWidth = strokeW
                    )
                }
                // Delicate border halo around lattice
                drawCircle(
                    color = trimColor.copy(alpha = 0.72f),
                    radius = radius * 0.78f,
                    center = center,
                    style = Stroke(width = strokeW * 1.1f)
                )
            }

            BeadDesign.CRESCENT_INLAY -> {
                // Crescent Moon (Hilal) + tiny accompanying star embossed in metallic gold
                val outerR = radius * 0.34f
                val moonCenter = Offset(center.x - radius * 0.04f, center.y + radius * 0.02f)
                val outerMoon = Path().apply {
                    addOval(
                        Rect(
                            moonCenter.x - outerR,
                            moonCenter.y - outerR,
                            moonCenter.x + outerR,
                            moonCenter.y + outerR
                        )
                    )
                }
                val cutR = outerR * 0.80f
                val cutCenter = Offset(moonCenter.x + outerR * 0.36f, moonCenter.y - outerR * 0.18f)
                val innerCut = Path().apply {
                    addOval(
                        Rect(
                            cutCenter.x - cutR,
                            cutCenter.y - cutR,
                            cutCenter.x + cutR,
                            cutCenter.y + cutR
                        )
                    )
                }
                val crescentPath = Path.combine(
                    operation = androidx.compose.ui.graphics.PathOperation.Difference,
                    path1 = outerMoon,
                    path2 = innerCut
                )

                // Soft engraved shadow under crescent
                drawPath(
                    path = crescentPath,
                    color = grooveColor
                )
                drawPath(
                    path = crescentPath,
                    brush = Brush.linearGradient(
                        colors = listOf(inlayColor, trimColor),
                        start = Offset(moonCenter.x - outerR, moonCenter.y - outerR),
                        end = Offset(moonCenter.x + outerR, moonCenter.y + outerR)
                    )
                )

                // Accompanying 4-point star next to crescent
                val starCenter = Offset(center.x + radius * 0.24f, center.y - radius * 0.12f)
                val starR = (radius * 0.11f).coerceAtLeast(2.2f)
                val starPath = Path().apply {
                    for (p in 0 until 8) {
                        val r = if (p % 2 == 0) starR else starR * 0.38f
                        val ang = Math.toRadians((p * 45.0) - 90.0)
                        val px = starCenter.x + (cos(ang) * r).toFloat()
                        val py = starCenter.y + (sin(ang) * r).toFloat()
                        if (p == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(path = starPath, color = inlayColor)
            }
        }
    }
}

