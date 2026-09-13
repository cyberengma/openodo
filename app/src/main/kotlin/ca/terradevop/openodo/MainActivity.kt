// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.viewModels
import ca.terradevop.openodo.ui.theme.OpenodoTheme
import ca.terradevop.openodo.ui.OpenOdoApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: ca.terradevop.openodo.ui.AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenodoTheme(darkTheme = isSystemInDarkTheme()) { OpenOdoApp(appViewModel) }
        }
    }
}
