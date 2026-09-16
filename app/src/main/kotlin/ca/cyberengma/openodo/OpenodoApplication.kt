// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ca.cyberengma.openodo.work.ReminderWorkScheduler

@HiltAndroidApp
class OpenodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runCatching { ReminderWorkScheduler.schedule(this) }
    }
}
