// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class EconomyTest {
    @Test fun `litres per hundred kilometres scale one`() =
        assertEquals(BigDecimal("5.6"), Economy.litresPer100Km(5_555, 1))

    @Test fun `litres per hundred kilometres scale two`() =
        assertEquals(BigDecimal("5.56"), Economy.litresPer100Km(5_555, 2))

    @Test fun `kilometres per litre`() =
        assertEquals(BigDecimal("18.00"), Economy.kmPerLitre(5_555, 2))

    @Test fun `us mpg`() =
        assertEquals(BigDecimal("42.00"), Economy.mpgUs(5_600, 2))

    @Test fun `uk mpg`() =
        assertEquals(BigDecimal("50.44"), Economy.mpgUk(5_600, 2))

    @Test fun `kilowatt hours per hundred kilometres`() =
        assertEquals(BigDecimal("20.0"), Economy.kilowattHoursPer100Km(200, 1))

    @Test fun `miles per kilowatt hour`() =
        assertEquals(BigDecimal("3.11"), Economy.milesPerKilowattHour(200, 2))

    @Test fun `zero denominator returns scaled zero`() {
        assertEquals(BigDecimal("0.00"), Economy.kmPerLitre(0, 2))
        assertEquals(BigDecimal("0.000"), Economy.mpgUs(0, 3))
    }
}
