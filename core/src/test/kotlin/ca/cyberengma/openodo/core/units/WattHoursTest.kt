// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.units

import java.math.BigDecimal
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class WattHoursTest {
    @Test
    fun `watt hours to kilowatt hours`() {
        assertEquals(BigDecimal("1.500"), WattHours(1_500).to(EnergyUnit.KILOWATT_HOURS))
        assertEquals(BigDecimal("0.001"), WattHours(1).to(EnergyUnit.KILOWATT_HOURS))
    }

    @Test
    fun `watt hours identity`() {
        assertEquals(BigDecimal("123"), WattHours(123).to(EnergyUnit.WATT_HOURS))
    }

    @Test
    fun `kilowatt hours factory`() {
        assertEquals(WattHours(1_500), WattHours.kilowattHours(BigDecimal("1.5")))
        assertEquals(WattHours(0), WattHours.kilowattHours(BigDecimal("0")))
    }

    @Test
    fun `round trips across ten thousand random values`() {
        val random = Random(42)
        repeat(10_000) {
            val value = random.nextLong(1L, 200_000L)
            val kwh = WattHours(value).to(EnergyUnit.KILOWATT_HOURS)
            assertEquals(value, WattHours.kilowattHours(kwh).value)
        }
    }
}
