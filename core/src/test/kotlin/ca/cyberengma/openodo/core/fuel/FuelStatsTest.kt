// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.fuel

import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.Millilitres
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FuelStatsTest {
    private val clock = Clock.fixed(LocalDate.of(2026, 3, 1).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
    private val d = LocalDate.of(2026, 1, 1)

    @Test fun `empty history returns empty`() =
        assertEquals(FuelStatsResult.Empty, FuelStats.of(emptyList(), clock))

    @Test fun `totals include every entry`() {
        val result = stats(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true, cost = 5_000),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 50_000, fullTank = true, cost = 5_000),
        ))
        assertEquals(Millilitres(100_000), result.totalVolume)
        assertEquals(Money(10_000, "CAD"), result.totalCost)
    }

    @Test fun `average is volume weighted`() {
        val result = stats(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 50_000, fullTank = true),
            fuel(3, d.plusDays(2), 11_100, volumeMl = 10_000),
            fuel(4, d.plusDays(3), 12_000, volumeMl = 10_000, fullTank = true),
        ))
        assertEquals(3_500L, result.averageMlPer100Km)
    }

    @Test fun `best and worst spans`() {
        val result = stats(listOf(
            fuel(1, d, 10_000, volumeMl = 90_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 90_000, fullTank = true),
            fuel(3, d.plusDays(2), 11_000, volumeMl = 70_000, fullTank = true),
            fuel(4, d.plusDays(3), 12_000, volumeMl = 70_000, fullTank = true),
        ))
        assertEquals(7_000L, result.best!!.mlPer100km)
        assertEquals(9_000L, result.worst!!.mlPer100km)
    }

    @Test fun `cost per kilometre is milli minor units`() {
        val result = stats(listOf(
            fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true, cost = 5_000),
            fuel(2, d.plusDays(1), 11_000, volumeMl = 50_000, fullTank = true, cost = 5_000),
        ))
        assertEquals(10_000L, result.costPerKmMilli)
    }

    @Test fun `last fill up and distance since`() {
        val result = stats(listOf(
            fuel(1, d, 10_000, fullTank = true),
            fuel(2, d.plusDays(1), 11_000, fullTank = true),
            fuel(3, d.plusDays(2), 11_500),
        ))
        assertEquals(3L, result.lastFillUp.id)
        assertEquals(0L, result.distanceSinceLastFillUp.value)
    }

    @Test fun `monthly buckets include requested empty month`() {
        val result = stats(
            listOf(fuel(1, d, 10_000, volumeMl = 50_000, fullTank = true)),
            YearMonth.of(2026, 1),
            YearMonth.of(2026, 3),
        )
        assertEquals(listOf(YearMonth.of(2026, 1), YearMonth.of(2026, 2), YearMonth.of(2026, 3)), result.monthBuckets.map { it.month })
        assertEquals(Money(0, "CAD"), result.monthBuckets[1].cost)
    }

    @Test fun `mixed currency is a stats error`() {
        val result = FuelStats.of(listOf(
            fuel(1, d, 10_000, currency = "CAD", fullTank = true),
            fuel(2, d.plusDays(1), 11_000, currency = "USD", fullTank = true),
        ), clock)
        assertTrue(result is FuelStatsResult.Error)
    }

    private fun stats(entries: List<ca.cyberengma.openodo.core.model.FuelEntry>, from: YearMonth? = null, to: YearMonth? = null): FuelStatsResult.Stats =
        FuelStats.of(entries, clock, from, to) as FuelStatsResult.Stats
}
