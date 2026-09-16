// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.ui

import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.units.Metres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderShellStateTest {
    @Test
    fun `unanchored reminder is representable`() {
        val reminder = Reminder(1, 1, 1, Metres(10_000), 6, null, null, true)
        assertTrue(reminder.anchorDate == null)
    }

    @Test
    fun `reminder configuration preserves both intervals`() {
        val reminder = Reminder(1, 1, 1, Metres(10_000), 6, null, null, true)
        assertEquals(10_000L, reminder.intervalDistance!!.value)
        assertEquals(6, reminder.intervalMonths)
    }
}
