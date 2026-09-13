// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.units

import java.math.BigDecimal
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MillilitresTest {
    @Test
    fun `millilitres to litres`() {
        assertEquals(BigDecimal("0.555"), Millilitres(555).to(VolumeUnit.LITRES))
        assertEquals(BigDecimal("12.345"), Millilitres(12_345).to(VolumeUnit.LITRES))
    }

    @Test
    fun `millilitres to litres negative`() {
        assertEquals(BigDecimal("-0.555"), Millilitres(-555).to(VolumeUnit.LITRES))
    }

    @Test
    fun `millilitres to litres zero`() {
        assertEquals(BigDecimal("0.000"), Millilitres(0).to(VolumeUnit.LITRES))
    }

    @Test
    fun `exact us gallon`() {
        assertEquals(BigDecimal("1.000"), Millilitres(3_785).to(VolumeUnit.US_GALLONS))
        assertEquals(BigDecimal("2.000"), Millilitres(7_571).to(VolumeUnit.US_GALLONS))
    }

    @Test
    fun `uk gallon rounds half even`() {
        assertEquals(BigDecimal("1.000"), Millilitres(4_546).to(VolumeUnit.UK_GALLONS))
        assertEquals(BigDecimal("10.000"), Millilitres(45_460).to(VolumeUnit.UK_GALLONS))
    }

    @Test
    fun `litres factory`() {
        assertEquals(Millilitres(555), Millilitres.litres(BigDecimal("0.555")))
        assertEquals(Millilitres(0), Millilitres.litres(BigDecimal("0")))
    }

    @Test
    fun `us gallon factory`() {
        assertEquals(Millilitres(3_785), Millilitres.usGallons(BigDecimal("1")))
    }

    @Test
    fun `round trips litres across ten thousand random values`() {
        val random = Random(42)
        repeat(10_000) {
            val value = random.nextLong(-500_000L, 500_000L)
            val litres = Millilitres(value).to(VolumeUnit.LITRES)
            val back = Millilitres.litres(litres).value
            assertTrue(
                "|$value - $back| <= 1",
                kotlin.math.abs(value - back) <= 1L,
            )
        }
    }

    @Test
    fun `round trips us gallons across ten thousand random values`() {
        val random = Random(42)
        repeat(10_000) {
            val value = random.nextLong(1L, 400_000L)
            val gallons = Millilitres(value).to(VolumeUnit.US_GALLONS)
            val back = Millilitres.usGallons(gallons).value
            assertTrue("|$value - $back| <= 5", kotlin.math.abs(value - back) <= 5L)
        }
    }
}
