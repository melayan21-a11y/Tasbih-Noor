package com.example.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.R

enum class AppThemeStyle(
    val title: String,
    val subtitle: String,
    val isDark: Boolean,
    @DrawableRes val backgroundRes: Int,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBorder: Color,
    val accent: Color,
    val accentLight: Color,
    val secondaryAccent: Color,
    val onPrimaryColor: Color,
    val primaryContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cordColor: Color,
    val backgroundArtAlpha: Float
) {
    MIDNIGHT_EMERALD(
        title = "Midnight Emerald",
        subtitle = "Masjid lantern green & gold",
        isDark = true,
        backgroundRes = R.drawable.bg_islamic_night,
        background = NightBackground,
        surface = Color(0xFF09241C),
        surfaceVariant = NightSurfaceVariant,
        cardBorder = NightCardBorder,
        accent = GoldPrimary,
        accentLight = GoldLight,
        secondaryAccent = EmeraldLight,
        onPrimaryColor = NightBackground,
        primaryContainer = EmeraldMedium,
        textPrimary = NightTextPrimary,
        textSecondary = NightTextSecondary,
        cordColor = GoldDark,
        backgroundArtAlpha = 0.72f
    ),
    RADIANT_IVORY(
        title = "Daylight Ivory",
        subtitle = "Warm parchment & brass",
        isDark = false,
        backgroundRes = R.drawable.bg_islamic_day,
        background = LightBackground,
        surface = Color(0xFFFFFDF8),
        surfaceVariant = LightSurfaceVariant,
        cardBorder = LightCardBorder,
        accent = EmeraldPrimary,
        accentLight = DayGoldAccent,
        secondaryAccent = DayGoldAccent,
        onPrimaryColor = Color.White,
        primaryContainer = EmeraldContainer,
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        cordColor = Color(0xFF6B5027),
        backgroundArtAlpha = 0.68f
    ),
    KAABA_KISWAH(
        title = "Kiswah Silk & Gold",
        subtitle = "Obsidian silk damask & gold",
        isDark = true,
        backgroundRes = R.drawable.img_theme_kiswah,
        background = Color(0xFF070709),
        surface = Color(0xFF121216),
        surfaceVariant = Color(0xFF1D1B17),
        cardBorder = Color(0xFF4A3B18),
        accent = Color(0xFFE6BA4A),
        accentLight = Color(0xFFFFE599),
        secondaryAccent = Color(0xFFD4A037),
        onPrimaryColor = Color(0xFF08080A),
        primaryContainer = Color(0xFF2E240D),
        textPrimary = Color(0xFFFCF8ED),
        textSecondary = Color(0xFFD0C29E),
        cordColor = Color(0xFF9C7622),
        backgroundArtAlpha = 0.78f
    ),
    ANDALUSIAN_NIGHT(
        title = "Alhambra Indigo",
        subtitle = "Sapphire arches & lantern gold",
        isDark = true,
        backgroundRes = R.drawable.img_theme_andalusian,
        background = Color(0xFF060B1D),
        surface = Color(0xFF0E1733),
        surfaceVariant = Color(0xFF18264E),
        cardBorder = Color(0xFF334982),
        accent = Color(0xFFEBC263),
        accentLight = Color(0xFFF9E6A7),
        secondaryAccent = Color(0xFF7B9FE8),
        onPrimaryColor = Color(0xFF060B1D),
        primaryContainer = Color(0xFF1F3366),
        textPrimary = Color(0xFFF2F5FF),
        textSecondary = Color(0xFFABC0EB),
        cordColor = Color(0xFF6C87BD),
        backgroundArtAlpha = 0.78f
    ),
    OTTOMAN_IZNIK(
        title = "Ottoman Iznik",
        subtitle = "Turquoise tilework & bronze",
        isDark = true,
        backgroundRes = R.drawable.img_theme_iznik,
        background = Color(0xFF051A20),
        surface = Color(0xFF0B2932),
        surfaceVariant = Color(0xFF123C47),
        cardBorder = Color(0xFF275F6D),
        accent = Color(0xFFF0A658),
        accentLight = Color(0xFFFFDFB5),
        secondaryAccent = Color(0xFF4DB6AC),
        onPrimaryColor = Color(0xFF051A20),
        primaryContainer = Color(0xFF174A54),
        textPrimary = Color(0xFFF0FBFC),
        textSecondary = Color(0xFFA2D3DA),
        cordColor = Color(0xFFC27938),
        backgroundArtAlpha = 0.76f
    ),
    MADINAH_MARBLE(
        title = "Madinah Marble",
        subtitle = "Travertine colonnades & copper",
        isDark = false,
        backgroundRes = R.drawable.img_theme_madinah,
        background = Color(0xFFFAF3EA),
        surface = Color(0xFFFFFCF7),
        surfaceVariant = Color(0xFFF0E3D3),
        cardBorder = Color(0xFFD7BC9F),
        accent = Color(0xFF845132),
        accentLight = Color(0xFFB57852),
        secondaryAccent = Color(0xFFC08457),
        onPrimaryColor = Color.White,
        primaryContainer = Color(0xFFEEDCC8),
        textPrimary = Color(0xFF2F1E14),
        textSecondary = Color(0xFF755744),
        cordColor = Color(0xFF845132),
        backgroundArtAlpha = 0.64f
    )
}

val LocalTasbihPalette = staticCompositionLocalOf { AppThemeStyle.MIDNIGHT_EMERALD }

@Composable
fun TasbihTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.MIDNIGHT_EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeStyle.isDark) {
        darkColorScheme(
            primary = themeStyle.accent,
            onPrimary = themeStyle.onPrimaryColor,
            primaryContainer = themeStyle.primaryContainer,
            onPrimaryContainer = themeStyle.accentLight,
            secondary = themeStyle.secondaryAccent,
            onSecondary = Color.White,
            secondaryContainer = themeStyle.surfaceVariant,
            onSecondaryContainer = themeStyle.textPrimary,
            tertiary = themeStyle.accentLight,
            onTertiary = themeStyle.onPrimaryColor,
            background = themeStyle.background,
            onBackground = themeStyle.textPrimary,
            surface = themeStyle.surface,
            onSurface = themeStyle.textPrimary,
            surfaceVariant = themeStyle.surfaceVariant,
            onSurfaceVariant = themeStyle.textSecondary,
            outline = themeStyle.cardBorder
        )
    } else {
        lightColorScheme(
            primary = themeStyle.accent,
            onPrimary = themeStyle.onPrimaryColor,
            primaryContainer = themeStyle.primaryContainer,
            onPrimaryContainer = themeStyle.textPrimary,
            secondary = themeStyle.secondaryAccent,
            onSecondary = Color.White,
            secondaryContainer = themeStyle.surfaceVariant,
            onSecondaryContainer = themeStyle.textPrimary,
            tertiary = themeStyle.accentLight,
            onTertiary = Color.White,
            background = themeStyle.background,
            onBackground = themeStyle.textPrimary,
            surface = themeStyle.surface,
            onSurface = themeStyle.textPrimary,
            surfaceVariant = themeStyle.surfaceVariant,
            onSurfaceVariant = themeStyle.textSecondary,
            outline = themeStyle.cardBorder
        )
    }

    CompositionLocalProvider(LocalTasbihPalette provides themeStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
