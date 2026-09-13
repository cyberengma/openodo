// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderWorkScheduler {
    private const val WORK_NAME = "openodo-reminder-check"
    const val WORK_NAME_FOR_TESTS = WORK_NAME

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
