// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceStatsTest {
    private val d = LocalDate.of(2026, 1, 1)

    @Test fun `groups prices by fuel label`() {
        val result = PriceStats.of(listOf(
            fuel(1, d, 10_000, label = "regular", unitPrice = 1_500),
            fuel(2, d.plusDays(1), 10_100, label = "regular", unitPrice = 1_700),
            fuel(3, d.plusDays(2), 10_200, label = "premium", unitPrice = 1_900),
        ))
        assertEquals(listOf("premium", "regular"), result.map { it.label })
        assertEquals(1_500L, result[1].minimum)
        assertEquals(1_700L, result[1].maximum)
        assertEquals(BigDecimal("1600.000000"), result[1].average)
    }

    @Test fun `date range excludes outside entries`() {
        val result = PriceStats.of(listOf(
            fuel(1, d, 10_000, unitPrice = 1_500),
            fuel(2, d.plusDays(10), 10_100, unitPrice = 1_900),
        ), from = d, to = d.plusDays(1))
        assertEquals(1, result.size)
        assertEquals(1_500L, result.single().minimum)
    }

    @Test fun `entries without prices are ignored`() {
        assertEquals(0, PriceStats.of(listOf(fuel(1, d, 10_000, unitPrice = null))).size)
    }

    @Test fun `empty input is empty`() = assertEquals(emptyList<PriceSummary>(), PriceStats.of(emptyList()))
}
