// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.reminders

import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.units.Metres
import java.time.LocalDate

data class ReminderSummary(
    val reminders: List<ReminderStatusEntry>,
    val counts: Map<ReminderState, Int>,
) {
    data class ReminderStatusEntry(val reminder: Reminder, val status: ReminderStatus)

    companion object {
        fun forVehicle(
            reminders: List<Reminder>,
            currentOdometer: Metres?,
            today: LocalDate,
            currentOdometerDate: LocalDate? = null,
        ): ReminderSummary {
            val entries = reminders.map {
                ReminderStatusEntry(
                    it,
                    DueCalculator.evaluate(it, currentOdometer, today, currentOdometerDate),
                )
            }.sortedWith(
                compareByDescending<ReminderStatusEntry> { urgencyRank(it.status.state) }
                    .thenBy { urgencyRemaining(it.status) }
                    .thenBy { it.status.nextDueDate ?: LocalDate.MAX }
                    .thenBy { it.reminder.id },
            )
            val counts = ReminderState.entries.associateWith { state -> entries.count { it.status.state == state } }
            return ReminderSummary(entries, counts)
        }

        private fun urgencyRank(state: ReminderState): Int = when (state) {
            ReminderState.OVERDUE -> 4
            ReminderState.DUE_SOON -> 3
            ReminderState.OK -> 2
            ReminderState.UNANCHORED -> 1
            ReminderState.INACTIVE -> 0
        }

        private fun urgencyRemaining(status: ReminderStatus): Long = when (status.state) {
            ReminderState.OVERDUE -> minOf(status.remainingDays ?: Long.MAX_VALUE, status.remainingDistance?.value ?: Long.MAX_VALUE)
            else -> minOf(status.remainingDays ?: Long.MAX_VALUE, status.remainingDistance?.value ?: Long.MAX_VALUE)
        }
    }
}
