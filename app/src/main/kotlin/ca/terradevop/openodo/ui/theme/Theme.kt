// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF2F6870)
private val LightColors = lightColorScheme(primary = Teal, secondary = Color(0xFFE3ECEC), background = Color(0xFFF7F8F7))
private val DarkColors = darkColorScheme(primary = Color(0xFF8FCBD0), secondary = Color(0xFF23494E))

@Composable
fun OpenodoTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
