// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.CurrentOdometer
import ca.cyberengma.openodo.core.model.CurrentOdometerResult
import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.model.RecordedOdometer
import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.model.Vehicle
import java.time.LocalDate

data class DueReminder(val reminder: Reminder, val status: ReminderStatus)

/**
 * Selects the active reminders that are currently due (overdue or due soon)
 * for a single vehicle, given its fuel/expense history used to derive the
 * current odometer.
 */
object DueReminders {
    fun of(
        vehicle: Vehicle,
        reminders: List<Reminder>,
        fuels: List<FuelEntry>,
        expenses: List<ExpenseRecord>,
        today: LocalDate,
    ): List<DueReminder> {
        val readings = (fuels.map { RecordedOdometer(it.date, it.odometer) } + expenses.map { RecordedOdometer(it.date, it.odometer) })
            .filter { it.odometer.value >= 0 }
        val currentOdometer = when (val result = CurrentOdometer.of(vehicle, readings)) {
            is CurrentOdometerResult.Value -> result.odometer
            CurrentOdometerResult.Empty -> null
        }
        return reminders
            .filter { it.active }
            .mapNotNull { reminder ->
                val status = DueCalculator.evaluate(reminder, currentOdometer, today)
                if (status.state == ReminderState.OVERDUE || status.state == ReminderState.DUE_SOON) {
                    DueReminder(reminder, status)
                } else {
                    null
                }
            }
    }
}