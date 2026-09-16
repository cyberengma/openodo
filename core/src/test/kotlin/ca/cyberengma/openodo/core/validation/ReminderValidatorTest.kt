// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.validation

import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.units.Metres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReminderValidatorTest {
    @Test
    fun `distance interval accepted`() {
        assertTrue(Validators.reminder(reminder(distance = 10_000_000)) is ValidationResult.Ok)
    }

    @Test
    fun `months interval accepted`() {
        assertTrue(Validators.reminder(reminder(months = 12)) is ValidationResult.Ok)
    }

    @Test
    fun `both positive intervals accepted`() {
        assertTrue(Validators.reminder(reminder(distance = 10_000_000, months = 6)) is ValidationResult.Ok)
    }

    @Test
    fun `no interval rejected`() {
        val result = Validators.reminder(reminder())
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(listOf(ValidationErrorCode.NO_INTERVAL), (result as ValidationResult.Error).codes)
    }

    @Test
    fun `zero distance interval rejected`() {
        val result = Validators.reminder(reminder(distance = 0))
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(listOf(ValidationErrorCode.NON_POSITIVE_INTERVAL), (result as ValidationResult.Error).codes)
    }

    @Test
    fun `negative months rejected`() {
        val result = Validators.reminder(reminder(months = -3))
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(listOf(ValidationErrorCode.NON_POSITIVE_INTERVAL), (result as ValidationResult.Error).codes)
    }

    @Test
    fun `zero months rejected`() {
        val result = Validators.reminder(reminder(distance = 5_000_000, months = 0))
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(listOf(ValidationErrorCode.NON_POSITIVE_INTERVAL), (result as ValidationResult.Error).codes)
    }

    private fun reminder(distance: Long? = null, months: Int? = null) = Reminder(
        vehicleId = 1L,
        typeId = 1L,
        intervalDistance = distance?.let(::Metres),
        intervalMonths = months,
        anchorDate = LocalDate.of(2026, 1, 15),
        anchorOdometer = Metres(120_000_000),
        active = true,
    )
}
