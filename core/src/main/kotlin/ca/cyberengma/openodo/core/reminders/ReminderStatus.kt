// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.units.Metres
import java.time.LocalDate

enum class ReminderState {
    OK,
    DUE_SOON,
    OVERDUE,
    INACTIVE,
    UNANCHORED,
}

enum class ReminderTrigger {
    DATE,
    DISTANCE,
    BOTH,
    NONE,
}

data class ReminderStatus(
    val state: ReminderState,
    val nextDueDate: LocalDate?,
    val nextDueOdometer: Metres?,
    val remainingDays: Long?,
    val remainingDistance: Metres?,
    val triggeredBy: ReminderTrigger,
    val staleOdometer: Boolean,
)
