// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.money

import ca.cyberengma.openodo.core.units.Millilitres
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitPriceTest {
    @Test
    fun `total in canadian dollars`() {
        val total = UnitPrice(1_599, "CAD").total(Millilitres(555))
        assertEquals(Money(89, "CAD"), total)
    }

    @Test
    fun `total exact`() {
        assertEquals(Money(50, "EUR"), UnitPrice(2_000, "EUR").total(Millilitres(250)))
    }

    @Test
    fun `zero digit currency jpy`() {
        assertEquals(Money(2, "JPY"), UnitPrice(12_345, "JPY").total(Millilitres(123)))
    }

    @Test
    fun `three digit currency kwd rounds half even`() {
        assertEquals(Money(6_172, "KWD"), UnitPrice(12_345, "KWD").total(Millilitres(500)))
    }

    @Test
    fun `zero volume totals zero`() {
        assertEquals(Money(0, "CAD"), UnitPrice(1_599, "CAD").total(Millilitres(0)))
    }
}
