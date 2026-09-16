// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.units.Metres
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DueCalculator {
    private const val STALE_DAYS = 90L
    private const val DISTANCE_SOON_CAP_METRES = 500_000L

    /**
     * Month arithmetic deliberately uses [LocalDate.plusMonths]: for
     * example, January 31 plus one month becomes February 28 or 29.
     */
    fun evaluate(
        reminder: Reminder,
        currentOdometer: Metres?,
        today: LocalDate,
        currentOdometerDate: LocalDate? = null,
    ): ReminderStatus {
        if (!reminder.active) return inactive()
        if (reminder.anchorDate == null || reminder.anchorOdometer == null) return unanchored()

        val dueDate = reminder.intervalMonths?.let { reminder.anchorDate.plusMonths(it.toLong()) }
        val dueDistance = reminder.intervalDistance?.let {
            Metres(reminder.anchorOdometer.value + it.value)
        }
        val remainingDays = dueDate?.let { ChronoUnit.DAYS.between(today, it) }
        val remainingDistance = dueDistance?.let { currentOdometer?.let { now -> Metres(it.value - now.value) } }
        val dateState = dueDate?.let {
            axisState(remainingDays!!, dateThreshold(reminder.anchorDate, it))
        }
        val distanceState = if (dueDistance != null && currentOdometer != null) {
            axisState(remainingDistance!!.value, distanceThreshold(reminder.intervalDistance.value))
        } else {
            null
        }
        val state = listOfNotNull(dateState, distanceState).maxByOrNull { it.first.rank }?.first
            ?: ReminderState.OK
        val triggeredBy = when {
            dateState?.second == true && distanceState?.second == true -> ReminderTrigger.BOTH
            dateState?.second == true -> ReminderTrigger.DATE
            distanceState?.second == true -> ReminderTrigger.DISTANCE
            else -> ReminderTrigger.NONE
        }
        val stale = currentOdometerDate?.let { ChronoUnit.DAYS.between(it, today) > STALE_DAYS } ?: false
        return ReminderStatus(
            state = state,
            nextDueDate = dueDate,
            nextDueOdometer = dueDistance,
            remainingDays = remainingDays,
            remainingDistance = remainingDistance,
            triggeredBy = triggeredBy,
            staleOdometer = stale,
        )
    }

    private fun axisState(remaining: Long, soonThreshold: Long): Pair<ReminderState, Boolean> = when {
        remaining < 0 -> ReminderState.OVERDUE to true
        remaining == 0L -> ReminderState.OVERDUE to true
        remaining <= soonThreshold -> ReminderState.DUE_SOON to false
        else -> ReminderState.OK to false
    }

    private fun dateThreshold(anchor: LocalDate, due: LocalDate): Long =
        minOf(ChronoUnit.DAYS.between(anchor, due) * 10L / 100L, 30L)

    private fun distanceThreshold(distance: Long): Long =
        minOf(distance / 10L, DISTANCE_SOON_CAP_METRES)

    private fun inactive() = ReminderStatus(
        state = ReminderState.INACTIVE,
        nextDueDate = null,
        nextDueOdometer = null,
        remainingDays = null,
        remainingDistance = null,
        triggeredBy = ReminderTrigger.NONE,
        staleOdometer = false,
    )

    private fun unanchored() = ReminderStatus(
        state = ReminderState.UNANCHORED,
        nextDueDate = null,
        nextDueOdometer = null,
        remainingDays = null,
        remainingDistance = null,
        triggeredBy = ReminderTrigger.NONE,
        staleOdometer = false,
    )

    private val ReminderState.rank: Int
        get() = when (this) {
            ReminderState.OK -> 0
            ReminderState.DUE_SOON -> 1
            ReminderState.OVERDUE -> 2
            ReminderState.INACTIVE, ReminderState.UNANCHORED -> -1
        }
}
