package com.aistudio.mkulimapay.rdcapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aistudio.mkulimapay.rdcapp.model.ThemePreference

private val MkulimaLightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = ForestGreenDark,
    secondary = HarvestGold,
    onSecondary = Color(0xFF261900),
    secondaryContainer = HarvestGoldContainer,
    onSecondaryContainer = OnHarvestGoldContainer,
    tertiary = EarthBrown,
    onTertiary = Color.White,
    tertiaryContainer = EarthContainer,
    onTertiaryContainer = OnEarthContainer,
    background = MintCreamSurface,
    onBackground = Color(0xFF141D14),
    surface = MintCreamSurface,
    onSurface = Color(0xFF141D14),
    surfaceVariant = SoftSageContainer,
    onSurfaceVariant = Color(0xFF3E4B3E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F7EC),
    surfaceContainer = Color(0xFFE9F2E4),
    surfaceContainerHigh = Color(0xFFE1ECDC),
    outline = Color(0xFF6F7D6E),
    outlineVariant = Color(0xFFBFCBB9),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val MkulimaDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = Color(0xFFE3ECE2),
    surface = DarkSurface,
    onSurface = Color(0xFFE3ECE2),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBDCBB9),
    surfaceContainerLowest = Color(0xFF0A100B),
    surfaceContainerLow = Color(0xFF172118),
    surfaceContainer = Color(0xFF1B261C),
    surfaceContainerHigh = Color(0xFF233025),
    outline = Color(0xFF889686),
    outlineVariant = Color(0xFF3C4A3C),
    error = DarkError,
    onError = Color(0xFF690005),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

val MkulimaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MkulimaPayTheme(
    themePreference: ThemePreference = ThemePreference.AUTO,
    largeText: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.AUTO -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) MkulimaDarkColorScheme else MkulimaLightColorScheme
    val semanticColors = if (isDark) DarkSemanticColors else LightSemanticColors
    val typography = createMkulimaTypography(largeText)

    CompositionLocalProvider(LocalMkulimaColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = MkulimaShapes,
            content = content
        )
    }
}
