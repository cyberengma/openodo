// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.units

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class MetresTest {
    @Test
    fun `toKilometres returns whole kilometres`() {
        assertEquals(1_599L, Metres(1_599_000L).toKilometres())
    }

    @Test
    fun `of round trips through toKilometres`() {
        assertEquals(42_000L, Metres.of(42).toKilometres() * 1_000L)
        assertEquals(Metres(42_000_000L), Metres.of(42_000))
    }

    @Test
    fun `to kilometres decimal`() {
        assertEquals(BigDecimal("1.599"), Metres(1_599).to(DistanceUnit.KILOMETRES))
        assertEquals(BigDecimal("0.001"), Metres(1).to(DistanceUnit.KILOMETRES))
    }

    @Test
    fun `negative metres convert`() {
        assertEquals(BigDecimal("-0.500"), Metres(-500).to(DistanceUnit.KILOMETRES))
    }

    @Test
    fun `rounds at mile scale`() {
        assertEquals(BigDecimal("0.500"), Metres(805).to(DistanceUnit.MILES))
        assertEquals(BigDecimal("0.999"), Metres(1_608).to(DistanceUnit.MILES))
    }

    @Test
    fun `exact miles`() {
        assertEquals(BigDecimal("1.000"), Metres(1_609).to(DistanceUnit.MILES))
        assertEquals(BigDecimal("1000.000"), Metres(1_609_344).to(DistanceUnit.MILES))
    }

    @Test
    fun `miles factory`() {
        assertEquals(Metres(1_609), Metres.miles(BigDecimal("1")))
    }
}
