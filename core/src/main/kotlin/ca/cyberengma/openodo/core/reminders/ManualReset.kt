// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.units.Metres
import java.time.LocalDate

object ManualReset {
    fun apply(reminder: Reminder, date: LocalDate, odometer: Metres): Reminder =
        reminder.copy(anchorDate = date, anchorOdometer = odometer)
}
