// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.work

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderCheckWorkerTest {
    @Test
    fun `worker name is stable for unique local scheduling`() {
        assertEquals("openodo-reminder-check", ReminderWorkScheduler.WORK_NAME_FOR_TESTS)
    }
}
