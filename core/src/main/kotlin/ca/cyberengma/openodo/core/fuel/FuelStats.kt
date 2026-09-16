// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.fuel

import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.MoneyResult
import ca.cyberengma.openodo.core.units.Metres
import ca.cyberengma.openodo.core.units.Millilitres
import ca.cyberengma.openodo.core.units.WattHours
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

data class MonthBucket(
    val month: YearMonth,
    val cost: Money,
    val volume: Millilitres?,
    val energy: WattHours?,
)

sealed class FuelStatsResult {
    data object Empty : FuelStatsResult()

    data class Error(val left: String, val right: String) : FuelStatsResult()

    data class Stats(
        val totalVolume: Millilitres,
        val totalEnergy: WattHours,
        val totalCost: Money,
        val averageMlPer100Km: Long?,
        val best: ConsumptionSpan?,
        val worst: ConsumptionSpan?,
        val costPerKmMilli: Long?,
        val lastFillUp: FuelEntry,
        val distanceSinceLastFillUp: Metres,
        val monthBuckets: List<MonthBucket>,
        val spans: List<ConsumptionSpan>,
    ) : FuelStatsResult()
}

object FuelStats {
    fun of(
        entries: List<FuelEntry>,
        clock: Clock,
        from: YearMonth? = null,
        to: YearMonth? = null,
    ): FuelStatsResult {
        if (entries.isEmpty()) return FuelStatsResult.Empty
        val cost = when (val result = Money.sum(entries.map { it.totalCost })) {
            is MoneyResult.Success -> result.money
            is MoneyResult.MixedCurrency -> return FuelStatsResult.Error(result.left, result.right)
        }
        val spans = when (val result = SpanBuilder.build(entries)) {
            is SpanBuildResult.MixedCurrency -> return FuelStatsResult.Error(result.left, result.right)
            is SpanBuildResult.Spans -> result.values
        }
        val validSpans = spans.filter { it.valid }
        val liquidSpans = validSpans.filter { it.mlPer100km != null && it.volume != null }
        val totalVolume = entries.sumOf { it.volume?.value ?: 0L }.let(::Millilitres)
        val totalEnergy = entries.sumOf { it.energy?.value ?: 0L }.let(::WattHours)
        val totalDistance = liquidSpans.sumOf { it.distance.value }
        val spanVolume = liquidSpans.sumOf { it.volume!!.value }
        val average = if (totalDistance > 0L) {
            (spanVolume * 100_000L / totalDistance)
        } else {
            null
        }
        val latest = entries.maxWith(compareBy<FuelEntry> { it.date }.thenBy { it.odometer.value }.thenBy { it.id })
        val maxOdometer = entries.maxOf { it.odometer.value }
        val distanceSince = Metres((maxOdometer - latest.odometer.value).coerceAtLeast(0L))
        val costPerKmMilli = if (totalDistance > 0L) cost.minor * 1_000L / (totalDistance / 1_000L).coerceAtLeast(1L) else null
        val buckets = monthBuckets(entries, from, to)
        @Suppress("UNUSED_PARAMETER")
        val ignoredClock = clock
        return FuelStatsResult.Stats(
            totalVolume = totalVolume,
            totalEnergy = totalEnergy,
            totalCost = cost,
            averageMlPer100Km = average,
            best = liquidSpans.minByOrNull { it.mlPer100km!! },
            worst = liquidSpans.maxByOrNull { it.mlPer100km!! },
            costPerKmMilli = costPerKmMilli,
            lastFillUp = latest,
            distanceSinceLastFillUp = distanceSince,
            monthBuckets = buckets,
            spans = spans,
        )
    }

    private fun monthBuckets(entries: List<FuelEntry>, from: YearMonth?, to: YearMonth?): List<MonthBucket> {
        val first = from ?: entries.minOf { YearMonth.from(it.date) }
        val last = to ?: entries.maxOf { YearMonth.from(it.date) }
        if (first > last) return emptyList()
        val currency = entries.first().totalCost.currency
        val result = mutableListOf<MonthBucket>()
        var month = first
        while (month <= last) {
            val inMonth = entries.filter { YearMonth.from(it.date) == month }
            result += MonthBucket(
                month = month,
                cost = if (inMonth.isEmpty()) {
                    Money(0, currency)
                } else when (val sum = Money.sum(inMonth.map { it.totalCost })) {
                    is MoneyResult.Success -> sum.money
                    is MoneyResult.MixedCurrency -> Money(0, currency)
                },
                volume = inMonth.sumOf { it.volume?.value ?: 0L }.let(::Millilitres).takeIf { inMonth.any { e -> e.volume != null } },
                energy = inMonth.sumOf { it.energy?.value ?: 0L }.let(::WattHours).takeIf { inMonth.any { e -> e.energy != null } },
            )
            month = month.plusMonths(1)
        }
        return result
    }
}
