// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.reminders

import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.units.Metres
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DueCalculatorTest {
    @Test fun `inactive reminder is inactive`() {
        assertEquals(ReminderState.INACTIVE, DueCalculator.evaluate(reminder(active = false), null, TODAY).state)
    }

    @Test fun `missing date anchor is unanchored`() {
        assertEquals(ReminderState.UNANCHORED, DueCalculator.evaluate(reminder(anchorDate = null), Metres(100_000_000), TODAY).state)
    }

    @Test fun `missing odometer anchor is unanchored`() {
        assertEquals(ReminderState.UNANCHORED, DueCalculator.evaluate(reminder(anchorMetres = null), Metres(100_000_000), TODAY).state)
    }

    @Test fun `date only is okay before threshold`() {
        val status = DueCalculator.evaluate(reminder(months = 12, anchorDate = TODAY.minusMonths(1)), null, TODAY)
        assertEquals(ReminderState.OK, status.state)
        assertEquals(ReminderTrigger.NONE, status.triggeredBy)
    }

    @Test fun `date only due soon`() {
        val status = DueCalculator.evaluate(reminder(months = 1, anchorDate = TODAY.minusDays(28)), null, TODAY)
        assertEquals(ReminderState.DUE_SOON, status.state)
        assertEquals(ReminderTrigger.NONE, status.triggeredBy)
    }

    @Test fun `date only due today`() {
        val status = DueCalculator.evaluate(reminder(months = 1, anchorDate = TODAY.minusMonths(1)), null, TODAY)
        assertEquals(ReminderState.OVERDUE, status.state)
        assertEquals(ReminderTrigger.DATE, status.triggeredBy)
        assertEquals(0L, status.remainingDays)
    }

    @Test fun `date only overdue`() {
        val status = DueCalculator.evaluate(reminder(months = 1, anchorDate = TODAY.minusMonths(2)), null, TODAY)
        assertEquals(ReminderState.OVERDUE, status.state)
        assertEquals(ReminderTrigger.DATE, status.triggeredBy)
        assertTrue(status.remainingDays!! < 0)
    }

    @Test fun `distance only okay`() {
        val status = DueCalculator.evaluate(reminder(distance = 10_000_000), Metres(100_100_000), TODAY)
        assertEquals(ReminderState.OK, status.state)
        assertEquals(ReminderTrigger.NONE, status.triggeredBy)
    }

    @Test fun `distance only due soon at five hundred kilometre cap`() {
        val status = DueCalculator.evaluate(reminder(distance = 60_000_000), Metres(159_500_000), TODAY)
        assertEquals(ReminderState.DUE_SOON, status.state)
        assertEquals(500_000L, status.remainingDistance!!.value)
    }

    @Test fun `distance only due at odometer`() {
        val status = DueCalculator.evaluate(reminder(distance = 10_000_000), Metres(110_000_000), TODAY)
        assertEquals(ReminderState.OVERDUE, status.state)
        assertEquals(ReminderTrigger.DISTANCE, status.triggeredBy)
    }

    @Test fun `distance only overdue`() {
        val status = DueCalculator.evaluate(reminder(distance = 10_000_000), Metres(120_000_000), TODAY)
        assertEquals(ReminderState.OVERDUE, status.state)
        assertEquals(ReminderTrigger.DISTANCE, status.triggeredBy)
        assertTrue(status.remainingDistance!!.value < 0)
    }

    @Test fun `both axes date triggers first`() {
        val status = DueCalculator.evaluate(
            reminder(distance = 100_000_000, months = 1, anchorDate = TODAY.minusMonths(1)),
            Metres(100_100_000), TODAY,
        )
        assertEquals(ReminderState.OVERDUE, status.state)
        assertEquals(ReminderTrigger.DATE, status.triggeredBy)
    }

    @Test fun `both axes distance triggers first`() {
        val status = DueCalculator.evaluate(
            reminder(distance = 10_000_000, months = 12, anchorDate = TODAY.minusMonths(1)),
            Metres(110_000_000), TODAY,
        )
        assertEquals(ReminderState.OVERDUE, status.state)
        assertEquals(ReminderTrigger.DISTANCE, status.triggeredBy)
    }

    @Test fun `both axes trigger together`() {
        val status = DueCalculator.evaluate(
            reminder(distance = 10_000_000, months = 1, anchorDate = TODAY.minusMonths(1)),
            Metres(110_000_000), TODAY,
        )
        assertEquals(ReminderTrigger.BOTH, status.triggeredBy)
    }

    @Test fun `tomorrow is not overdue`() {
        val status = DueCalculator.evaluate(reminder(months = 1, anchorDate = TODAY.minusDays(29)), null, TODAY)
        assertTrue(status.remainingDays!! > 0)
        assertFalse(status.state == ReminderState.OVERDUE)
    }

    @Test fun `jan thirty first uses java month arithmetic`() {
        val anchor = LocalDate.of(2024, 1, 31)
        val status = DueCalculator.evaluate(reminder(months = 1, anchorDate = anchor), null, LocalDate.of(2024, 2, 28))
        assertEquals(LocalDate.of(2024, 2, 29), status.nextDueDate)
        assertEquals(1L, status.remainingDays)
    }

    @Test fun `leap year february is respected`() {
        val anchor = LocalDate.of(2024, 2, 29)
        val status = DueCalculator.evaluate(reminder(months = 12, anchorDate = anchor), null, LocalDate.of(2025, 2, 28))
        assertEquals(LocalDate.of(2025, 2, 28), status.nextDueDate)
        assertEquals(ReminderState.OVERDUE, status.state)
    }

    @Test fun `twelve months caps date threshold at thirty days`() {
        val anchor = TODAY.plusDays(29).minusMonths(12)
        val status = DueCalculator.evaluate(reminder(months = 12, anchorDate = anchor), null, TODAY)
        assertEquals(ReminderState.DUE_SOON, status.state)
        assertEquals(29L, status.remainingDays)
    }

    @Test fun `stale odometer is flagged without changing state`() {
        val status = DueCalculator.evaluate(
            reminder(distance = 10_000_000), Metres(100_100_000), TODAY,
            currentOdometerDate = TODAY.minusDays(91),
        )
        assertTrue(status.staleOdometer)
        assertEquals(ReminderState.OK, status.state)
    }

    @Test fun `ninety day odometer is not stale`() {
        val status = DueCalculator.evaluate(
            reminder(distance = 10_000_000), Metres(100_100_000), TODAY,
            currentOdometerDate = TODAY.minusDays(90),
        )
        assertFalse(status.staleOdometer)
    }

    @Test fun `no odometer leaves distance axis unresolved`() {
        val status = DueCalculator.evaluate(reminder(distance = 10_000_000), null, TODAY)
        assertEquals(ReminderState.OK, status.state)
        assertEquals(null, status.remainingDistance)
    }

    @Test fun `next date and odometer are exposed`() {
        val status = DueCalculator.evaluate(reminder(distance = 10_000_000, months = 6), Metres(100_100_000), TODAY)
        assertEquals(TODAY.plusMonths(5), status.nextDueDate)
        assertEquals(Metres(110_000_000), status.nextDueOdometer)
    }
}
