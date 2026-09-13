// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.money

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyMinorDigitsTest {
    @Test
    fun `defaults to two minor digits`() {
        assertEquals(2, CurrencyMinorDigits.of("CAD"))
        assertEquals(2, CurrencyMinorDigits.of("USD"))
        assertEquals(2, CurrencyMinorDigits.of("EUR"))
        assertEquals(2, CurrencyMinorDigits.of("XYZ"))
    }

    @Test
    fun `known exceptions`() {
        assertEquals(0, CurrencyMinorDigits.of("JPY"))
        assertEquals(0, CurrencyMinorDigits.of("KRW"))
        assertEquals(3, CurrencyMinorDigits.of("KWD"))
        assertEquals(3, CurrencyMinorDigits.of("BHD"))
    }
}
