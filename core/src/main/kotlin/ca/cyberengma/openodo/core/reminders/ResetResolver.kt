// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.Reminder

object ResetResolver {
    fun onRecordLogged(reminders: List<Reminder>, record: ExpenseRecord): List<Reminder> =
        reminders.map { reminder ->
            if (reminder.vehicleId != record.vehicleId || reminder.typeId != record.typeId) {
                reminder
            } else if (isNewer(record.date, record.odometer.value, reminder.anchorDate, reminder.anchorOdometer?.value)) {
                reminder.copy(anchorDate = record.date, anchorOdometer = record.odometer)
            } else {
                reminder
            }
        }

    fun onRecordDeleted(
        reminders: List<Reminder>,
        deletedRecord: ExpenseRecord,
        remainingRecordsOfType: List<ExpenseRecord>,
    ): List<Reminder> {
        val newest = remainingRecordsOfType
            .filter { it.vehicleId == deletedRecord.vehicleId && it.typeId == deletedRecord.typeId }
            .maxWithOrNull(compareBy<ExpenseRecord> { it.date }.thenBy { it.odometer.value }.thenBy { it.id })
        return reminders.map { reminder ->
            if (reminder.vehicleId != deletedRecord.vehicleId || reminder.typeId != deletedRecord.typeId) {
                reminder
            } else {
                reminder.copy(
                    anchorDate = newest?.date,
                    anchorOdometer = newest?.odometer,
                )
            }
        }
    }

    private fun isNewer(
        date: java.time.LocalDate,
        odometer: Long,
        anchorDate: java.time.LocalDate?,
        anchorOdometer: Long?,
    ): Boolean = anchorDate == null || anchorOdometer == null ||
        date > anchorDate || (date == anchorDate && odometer > anchorOdometer)
}
