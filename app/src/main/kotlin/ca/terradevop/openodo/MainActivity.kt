// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ca.terradevop.openodo.ui.ThemeMode
import ca.terradevop.openodo.ui.theme.OpenodoTheme
import ca.terradevop.openodo.ui.OpenOdoApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: ca.terradevop.openodo.ui.AppViewModel by viewModels()

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
