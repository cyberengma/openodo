// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.units.Metres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSummaryTest {
    @Test fun `summary counts every state`() {
        val reminders = listOf(
            reminder(id = 1, months = 1, anchorDate = TODAY.minusMonths(2)),
            reminder(id = 2, months = 12, anchorDate = null),
            reminder(id = 3, active = false),
            reminder(id = 4, distance = 100_000_000, months = 12),
        )
        val summary = ReminderSummary.forVehicle(reminders, Metres(100_000_000), TODAY)
        assertEquals(1, summary.counts[ReminderState.OVERDUE])
        assertEquals(1, summary.counts[ReminderState.UNANCHORED])
        assertEquals(1, summary.counts[ReminderState.INACTIVE])
        assertEquals(1, summary.counts[ReminderState.OK])
    }

    @Test fun `overdue sorts before due soon and okay`() {
        val reminders = listOf(
            reminder(id = 1, months = 1, anchorDate = TODAY.minusMonths(2)),
            reminder(id = 2, months = 1, anchorDate = TODAY.minusDays(27)),
            reminder(id = 3, months = 12),
        )
        val ids = ReminderSummary.forVehicle(reminders, null, TODAY).reminders.map { it.reminder.id }
        assertEquals(listOf(1L, 2L, 3L), ids)
    }

    @Test fun `due soon orders by remaining amount`() {
        val reminders = listOf(
            reminder(id = 1, months = 1, anchorDate = TODAY.minusDays(27)),
            reminder(id = 2, months = 1, anchorDate = TODAY.minusDays(29)),
        )
        val ids = ReminderSummary.forVehicle(reminders, null, TODAY).reminders.map { it.reminder.id }
        assertEquals(listOf(2L, 1L), ids)
    }

    @Test fun `summary has deterministic id tie break`() {
        val reminders = listOf(
            reminder(id = 20, months = 12),
            reminder(id = 10, months = 12),
        )
        val ids = ReminderSummary.forVehicle(reminders, Metres(100_000_000), TODAY).reminders.map { it.reminder.id }
        assertTrue(ids.indexOf(10L) < ids.indexOf(20L))
    }

    @Test fun `summary accepts empty input`() {
        val summary = ReminderSummary.forVehicle(emptyList(), null, TODAY)
        assertTrue(summary.reminders.isEmpty())
        assertEquals(0, summary.counts.values.sum())
    }

    @Test fun `summary passes stale odometer context`() {
        val summary = ReminderSummary.forVehicle(
            listOf(reminder(distance = 100_000_000)), Metres(100_000_000), TODAY,
            currentOdometerDate = TODAY.minusDays(91),
        )
        assertTrue(summary.reminders.single().status.staleOdometer)
    }
}
