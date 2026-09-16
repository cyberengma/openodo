// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sleek "OpenOdo Mobile" palette — see docs/design/visual-system.md
private val Teal = Color(0xFF2F6870)
private val TealLight = Color(0xFF8FCBD0)
private val OffWhite = Color(0xFFF7F8F7)
private val Charcoal = Color(0xFF1B1D1C)
private val SecondarySurface = Color(0xFFE3ECEC)
private val SecondaryForeground = Color(0xFF23494E)
private val MutedSurface = Color(0xFFE7EAE8)
private val MutedForeground = Color(0xFF687371)
private val AccentSurface = Color(0xFFDCEBED)
private val AccentForeground = Color(0xFF24535A)
private val Destructive = Color(0xFFB3261E)
private val Border = Color(0xFFD8DEDC)
private val InputSurface = Color(0xFFF0F3F1)
private val Ring = Color(0xFF5B9298)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = AccentSurface,
    onPrimaryContainer = AccentForeground,
    secondary = SecondarySurface,
    onSecondary = SecondaryForeground,
    secondaryContainer = SecondarySurface,
    onSecondaryContainer = SecondaryForeground,
    tertiary = Color(0xFFB98235),
    background = OffWhite,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = MutedSurface,
    onSurfaceVariant = MutedForeground,
    outline = Border,
    outlineVariant = Border,
    error = Destructive,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF23494E),
    onPrimaryContainer = AccentSurface,
    secondary = Color(0xFF23494E),
    onSecondary = SecondarySurface,
    background = Color(0xFF101313),
    onBackground = Color(0xFFE0E3E2),
    surface = Color(0xFF181C1C),
    onSurface = Color(0xFFE0E3E2),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C7),
    outline = Color(0xFF899391),
    error = Color(0xFFF2B8B5),
)

private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private val Typography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

@Composable
fun OpenodoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = Shapes,
        typography = Typography,
        content = content,
    )
}
