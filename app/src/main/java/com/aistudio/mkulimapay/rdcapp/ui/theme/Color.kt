package com.aistudio.mkulimapay.rdcapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Brand Palette (Mkulima Pay - DRC Agricultural Microcredit)
val ForestGreen = Color(0xFF1B5E20)
val ForestGreenDark = Color(0xFF0D3B12)
val ForestGreenLight = Color(0xFF4C8C4A)
val MintCreamSurface = Color(0xFFF6FBF3)
val MintContainer = Color(0xFFD7EED6)
val SoftSageContainer = Color(0xFFEAF4E6)

val HarvestGold = Color(0xFFF9A825)
val HarvestGoldDark = Color(0xFFC17900)
val HarvestGoldContainer = Color(0xFFFFF3CD)
val OnHarvestGoldContainer = Color(0xFF3E2700)

val EarthBrown = Color(0xFF6D4C41)
val EarthBrownLight = Color(0xFF9C786C)
val EarthContainer = Color(0xFFEFEBE9)
val OnEarthContainer = Color(0xFF2D1B14)

val ErrorRed = Color(0xFFBA1A1A)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val SuccessGreen = Color(0xFF2E7D32)
val SuccessContainerLight = Color(0xFFDCEFD8)
val WarningOrange = Color(0xFFED6C02)
val WarningContainerLight = Color(0xFFFFE6CC)

// Dark Theme Tokens
val DarkBackground = Color(0xFF0E150F)
val DarkSurface = Color(0xFF141D15)
val DarkSurfaceVariant = Color(0xFF1F2B20)
val DarkPrimary = Color(0xFF81C784)
val DarkOnPrimary = Color(0xFF00390A)
val DarkPrimaryContainer = Color(0xFF1B5E20)
val DarkOnPrimaryContainer = Color(0xFFC8E6C9)
val DarkSecondary = Color(0xFFFFCA28)
val DarkOnSecondary = Color(0xFF3E2700)
val DarkSecondaryContainer = Color(0xFF5A3D00)
val DarkOnSecondaryContainer = Color(0xFFFFE082)
val DarkTertiary = Color(0xFFBCAAA4)
val DarkOnTertiary = Color(0xFF36211A)
val DarkTertiaryContainer = Color(0xFF4E342E)
val DarkOnTertiaryContainer = Color(0xFFD7CCC8)
val DarkError = Color(0xFFFFB4AB)
val DarkErrorContainer = Color(0xFF93000A)
val DarkSuccessContainer = Color(0xFF1B4D20)
val DarkWarningContainer = Color(0xFF5C2B00)

@Immutable
data class MkulimaSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val goldAccent: Color,
    val cardElevated: Color
)

val LightSemanticColors = MkulimaSemanticColors(
    success = SuccessGreen,
    onSuccess = Color.White,
    successContainer = SuccessContainerLight,
    onSuccessContainer = ForestGreenDark,
    warning = WarningOrange,
    onWarning = Color.White,
    warningContainer = WarningContainerLight,
    onWarningContainer = Color(0xFF4A2000),
    goldAccent = HarvestGold,
    cardElevated = Color(0xFFFFFFFF)
)

val DarkSemanticColors = MkulimaSemanticColors(
    success = Color(0xFF81C784),
    onSuccess = Color(0xFF00390A),
    successContainer = DarkSuccessContainer,
    onSuccessContainer = Color(0xFFC8E6C9),
    warning = Color(0xFFFFB74D),
    onWarning = Color(0xFF421C00),
    warningContainer = DarkWarningContainer,
    onWarningContainer = Color(0xFFFFE0B2),
    goldAccent = DarkSecondary,
    cardElevated = Color(0xFF1B261C)
)

val LocalMkulimaColors = staticCompositionLocalOf { LightSemanticColors }
