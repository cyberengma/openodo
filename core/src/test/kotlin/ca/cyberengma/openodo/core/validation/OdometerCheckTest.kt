// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.validation

import ca.cyberengma.openodo.core.units.Metres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OdometerCheckTest {
    @Test
    fun `first reading without previous max is ok`() {
        assertTrue(Validators.odometer(null, Metres(100_000_000)) is ValidationResult.Ok)
    }

    @Test
    fun `increasing odometer is ok`() {
        assertTrue(Validators.odometer(Metres(100_000_000), Metres(101_000_000)) is ValidationResult.Ok)
    }

    @Test
    fun `equal odometer is ok`() {
        assertTrue(Validators.odometer(Metres(100_000_000), Metres(100_000_000)) is ValidationResult.Ok)
    }

    @Test
    fun `decreased odometer is a warning`() {
        val result = Validators.odometer(Metres(100_000_000), Metres(99_999_000))
        assertTrue("expected Warning, got $result", result is ValidationResult.Warning)
        assertEquals(
            listOf(ValidationErrorCode.ODOMETER_DECREASED),
            (result as ValidationResult.Warning).codes,
        )
    }

    @Test
    fun `large decrease is still only a warning`() {
        val result = Validators.odometer(Metres(500_000_000), Metres(1))
        assertTrue("expected Warning, got $result", result is ValidationResult.Warning)
        assertEquals(listOf(ValidationErrorCode.ODOMETER_DECREASED), (result as ValidationResult.Warning).codes)
    }
}
