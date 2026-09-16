// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ca.cyberengma.openodo.ui.ThemeMode
import ca.cyberengma.openodo.ui.theme.OpenodoTheme
import ca.cyberengma.openodo.ui.OpenOdoApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: ca.cyberengma.openodo.ui.AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by appViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            OpenodoTheme(darkTheme = darkTheme) { OpenOdoApp(appViewModel) }
        }
    }
}
