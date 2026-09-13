// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import ca.terradevop.openodo.core.model.FuelKind
import ca.terradevop.openodo.core.units.Metres
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpanBuilderTest {
    private val d = LocalDate.of(2026, 1, 1)

    @Test fun `empty history has no spans`() = assertTrue(spans(SpanBuilder.build(emptyList())).isEmpty())

    @Test fun `single full tank opens but does not close`() {
        val result = SpanBuilder.build(listOf(fuel(1, d, 10_000, fullTank = true)))
        assertTrue(spans(result).isEmpty())
    }

    @Test fun `two full tanks produce one span`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 50_000, fullTank = true),
        )))
        assertEquals(1, result.size)
        assertEquals(Metres(1_000_000), result.single().distance)
        assertEquals(50_000L, result.single().volume!!.value)
        assertEquals(5_000L, result.single().mlPer100km)
        assertTrue(result.single().valid)
    }

    @Test fun `partial fills are included in span`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true),
            fuel(2, d.plusDays(1), 10_500, volumeMl = 20_000),
            fuel(3, d.plusDays(2), 11_000, volumeMl = 30_000, fullTank = true),
        )))
        assertEquals(50_000L, result.single().volume!!.value)
        assertEquals(5_000L, result.single().mlPer100km)
    }

    @Test fun `missed fill in middle invalidates span`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, fullTank = true),
            fuel(2, d.plusDays(1), 10_500, missed = true),
            fuel(3, d.plusDays(2), 11_000, fullTank = true),
        ))).single()
        assertFalse(span.valid)
        assertEquals(FuelSpanReason.MISSED_FILL_UP, span.reason)
    }

    @Test fun `missed fill on closing entry invalidates span`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, fullTank = true, missed = true),
        ))).single()
        assertFalse(span.valid)
        assertEquals(FuelSpanReason.MISSED_FILL_UP, span.reason)
    }

    @Test fun `missed flag on opener invalidates its closing span`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, fullTank = true, missed = true),
            fuel(2, d.plusDays(1), 11_000, fullTank = true),
            fuel(3, d.plusDays(2), 12_000, fullTank = true),
        )))
        assertFalse(result.first().valid)
        assertTrue(result.last().valid)
    }

    @Test fun `zero volume is invalid`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = 0, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 0, fullTank = true),
        ))).single()
        assertFalse(span.valid)
        assertEquals(FuelSpanReason.ZERO_VOLUME, span.reason)
        assertNull(span.mlPer100km)
    }

    @Test fun `backward odometer is negative distance`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(1, d, 11_000, fullTank = true),
            fuel(2, d.plusDays(1), 10_000, fullTank = true),
        ))).single()
        assertFalse(span.valid)
        assertEquals(FuelSpanReason.NEGATIVE_DISTANCE, span.reason)
    }

    @Test fun `entries sort by date then odometer then id`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(3, d.plusDays(2), 12_000, fullTank = true),
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 50_000, fullTank = true),
        )))
        assertEquals(listOf(1L, 2L), result.map { it.startEntryId })
    }

    @Test fun `electric and liquid histories are separate`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true),
            fuel(2, d.plusDays(1), 10_500, energyWh = 20_000, fullTank = true),
            fuel(3, d.plusDays(2), 11_000, volumeMl = 50_000, fullTank = true),
            fuel(4, d.plusDays(3), 11_500, energyWh = 20_000, fullTank = true),
        )))
        assertEquals(2, result.size)
        assertEquals(1, result.count { it.volume != null })
        assertEquals(1, result.count { it.energy != null })
    }

    @Test fun `electric spans compute watt hours per kilometre`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = null, energyWh = 20_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = null, energyWh = 20_000, fullTank = true),
        ))).single()
        assertEquals(20L, span.whPerKm)
        assertNull(span.mlPer100km)
    }

    @Test fun `mixed currencies return typed result`() {
        val result = SpanBuilder.build(listOf(
            fuel(1, d, 10_000, currency = "CAD", fullTank = true),
            fuel(2, d.plusDays(1), 10_500, currency = "CAD"),
            fuel(3, d.plusDays(2), 11_000, currency = "USD", fullTank = true),
        ))
        assertTrue(result is SpanBuildResult.MixedCurrency)
    }

    @Test fun `span carries dates and entry ids`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(7, d, 10_000, fullTank = true),
            fuel(9, d.plusDays(4), 11_000, fullTank = true),
        ))).single()
        assertEquals(7L, span.startEntryId)
        assertEquals(9L, span.endEntryId)
        assertEquals(d, span.startDate)
        assertEquals(d.plusDays(4), span.endDate)
    }

    @Test fun `same date ties use odometer and id`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(3, d, 10_000, fullTank = true),
            fuel(1, d, 11_000, fullTank = true),
            fuel(2, d, 12_000, fullTank = true),
        )))
        assertEquals(listOf(3L, 1L), result.map { it.startEntryId })
    }

    @Test fun `only completed spans are emitted`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, fullTank = true),
            fuel(2, d.plusDays(1), 10_500),
        )))
        assertTrue(result.isEmpty())
    }

    @Test fun `liquid kind is selected by volume`() {
        val span = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = 12_000, energyWh = null, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 12_000, energyWh = null, fullTank = true),
        ))).single()
        assertEquals(FuelKind.LIQUID, if (span.volume != null) FuelKind.LIQUID else FuelKind.ELECTRIC)
    }

    @Test fun `six entry worked example produces nine and seven litres per hundred`() {
        val result = spans(SpanBuilder.build(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true),
            fuel(2, d.plusDays(4), 10_300, volumeMl = 30_000),
            fuel(3, d.plusDays(8), 10_500, volumeMl = 20_000),
            fuel(4, d.plusDays(14), 11_000, volumeMl = 40_000, fullTank = true),
            fuel(5, d.plusDays(19), 11_400, volumeMl = 25_000),
            fuel(6, d.plusDays(31), 12_000, volumeMl = 45_000, fullTank = true),
        )))
        assertEquals(listOf(9_000L, 7_000L), result.map { it.mlPer100km })
        assertEquals(90_000L, result[0].volume!!.value)
        assertEquals(70_000L, result[1].volume!!.value)
    }

    @Test fun `ten thousand entry synthetic history completes correctly`() {
        val entries = (0L..10_000L).map { index ->
            fuel(
                id = index,
                date = d.plusDays(index),
                odometerKm = 10_000 + index,
                volumeMl = 10_000,
                fullTank = index == 0L || index % 10L == 0L,
            )
        }
        val started = System.nanoTime()
        val result = spans(SpanBuilder.build(entries))
        val elapsedMillis = (System.nanoTime() - started) / 1_000_000
        println("10,001 fuel entries processed in ${elapsedMillis}ms")
        assertEquals(1_000, result.size)
        assertTrue(result.all { it.valid })
    }
}
