// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.reminders

import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.units.Metres
import java.time.LocalDate

object ManualReset {
    fun apply(reminder: Reminder, date: LocalDate, odometer: Metres): Reminder =
        reminder.copy(anchorDate = date, anchorOdometer = odometer)
}
