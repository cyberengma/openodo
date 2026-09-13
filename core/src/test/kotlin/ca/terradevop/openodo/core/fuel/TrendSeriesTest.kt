// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import java.time.LocalDate
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendSeriesTest {
    private fun span(id: Long, value: Long, date: LocalDate) = ConsumptionSpan(
        startEntryId = id, endEntryId = id + 1, startDate = date.minusDays(1), endDate = date,
        distance = Metres(1_000_000), volume = Millilitres(value / 100 * 1_000), energy = null,
        cost = Money(1, "CAD"), mlPer100km = value, whPerKm = null, valid = true, reason = null,
    )

    @Test fun `series is chronological`() {
        val result = TrendSeries.consumption(listOf(span(2, 6_000, LocalDate.of(2026, 2, 1)), span(1, 5_000, LocalDate.of(2026, 1, 1))))
        assertEquals(listOf(5_000L, 6_000L), result.map { it.value })
    }

    @Test fun `moving average uses preceding values`() {
        val result = TrendSeries.consumption(listOf(
            span(1, 5_000, LocalDate.of(2026, 1, 1)),
            span(2, 7_000, LocalDate.of(2026, 2, 1)),
            span(3, 9_000, LocalDate.of(2026, 3, 1)),
        ), window = 2)
        assertEquals(BigDecimal("8000.000000"), result.last().movingAverage)
    }

    @Test fun `invalid spans are excluded`() {
        val invalid = span(1, 5_000, LocalDate.of(2026, 1, 1)).copy(valid = false)
        assertTrue(TrendSeries.consumption(listOf(invalid)).isEmpty())
    }

    @Test fun `default window is five`() {
        val result = TrendSeries.consumption((1L..6L).map { span(it, it * 1_000, LocalDate.of(2026, 1, it.toInt())) })
        assertEquals(BigDecimal("4000.000000"), result.last().movingAverage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero window is rejected`() {
        TrendSeries.consumption(emptyList(), window = 0)
    }
}
