package com.diego_ramos.croche_api.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArtisanLightColorScheme = lightColorScheme(
    primary = CaramelPrimary,
    onPrimary = OnCaramelWhite,
    primaryContainer = SoftBeigeContainer,
    onPrimaryContainer = DarkCoffeePrimary,

    secondary = DarkCoffeePrimary,
    onSecondary = OnCaramelWhite,
    secondaryContainer = RoseBeigeSecondary,
    onSecondaryContainer = DarkText,

    tertiary = SoftPinkTertiary,
    onTertiary = DarkText,
    tertiaryContainer = Color(0xFFFBF0EF),
    onTertiaryContainer = DarkText,

    background = CreamBackground,
    onBackground = DarkText,

    surface = CardSurfaceWhite,
    onSurface = DarkText,
    surfaceVariant = SoftBeigeContainer,
    onSurfaceVariant = MediumTextVariant,

    outline = SubtleBorderColor,
    outlineVariant = VisibleDividerColor
)

private val ArtisanDarkColorScheme = darkColorScheme(
    primary = CaramelPrimary,
    onPrimary = OnCaramelWhite,
    primaryContainer = DarkCoffeePrimary,
    onPrimaryContainer = RoseBeigeSecondary,

    secondary = RoseBeigeSecondary,
    onSecondary = DarkText,

    background = Color(0xFF1E1815),
    onBackground = Color(0xFFF3EAE3),

    surface = Color(0xFF27201C),
    onSurface = Color(0xFFF3EAE3),
    surfaceVariant = Color(0xFF382E28),
    onSurfaceVariant = Color(0xFFD3C3B8),
    outline = Color(0xFF8C7161),
    outlineVariant = Color(0xFF6E5648)
)

@Composable
fun CrocheapiTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ArtisanDarkColorScheme else ArtisanLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
