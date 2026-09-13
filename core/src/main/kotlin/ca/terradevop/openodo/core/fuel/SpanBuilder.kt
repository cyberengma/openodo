// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.FuelKind
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.MoneyResult
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import ca.terradevop.openodo.core.units.WattHours

object SpanBuilder {
    fun build(entries: List<FuelEntry>): SpanBuildResult {
        val spans = mutableListOf<ConsumptionSpan>()
        for (kind in FuelKind.entries) {
            val ordered = entries.filter { it.kind == kind }.sortedWith(
                compareBy<FuelEntry> { it.date }.thenBy { it.odometer.value }.thenBy { it.id },
            )
            var opener: FuelEntry? = null
            val inside = mutableListOf<FuelEntry>()
            for (entry in ordered) {
                if (opener == null) {
                    opener = entry
                    continue
                }
                inside += entry
                if (entry.fullTank) {
                    val result = makeSpan(opener, inside)
                    when (result) {
                        is SpanBuildResult.MixedCurrency -> return result
                        is SpanBuildResult.Spans -> spans += result.values
                    }
                    opener = entry
                    inside.clear()
                }
            }
        }
        return SpanBuildResult.Spans(spans.sortedWith(compareBy { it.endDate }))
    }

    private fun makeSpan(opener: FuelEntry, entries: List<FuelEntry>): SpanBuildResult {
        val closer = entries.last()
        val distance = Metres(closer.odometer.value - opener.odometer.value)
        val volume = entries.mapNotNull { it.volume }.sumOf { it.value }.let(::Millilitres)
            .takeIf { opener.kind == FuelKind.LIQUID }
        val energy = entries.mapNotNull { it.energy }.sumOf { it.value }.let(::WattHours)
            .takeIf { opener.kind == FuelKind.ELECTRIC }
        val costs = entries.map { it.totalCost }
        val cost = when (val result = Money.sum(costs)) {
            is MoneyResult.Success -> result.money
            is MoneyResult.MixedCurrency -> return SpanBuildResult.MixedCurrency(result.left, result.right)
        }
        val reason = when {
            opener.missedPreviousFillUp || entries.any { it.missedPreviousFillUp } -> FuelSpanReason.MISSED_FILL_UP
            distance.value <= 0 -> FuelSpanReason.NEGATIVE_DISTANCE
            (volume?.value ?: energy?.value ?: 0L) <= 0L -> FuelSpanReason.ZERO_VOLUME
            else -> null
        }
        val valid = reason == null
        val mlPer100km = if (valid && volume != null) volume.value * 100_000L / distance.value else null
        val whPerKm = if (valid && energy != null) energy.value * 1_000L / distance.value else null
        return SpanBuildResult.Spans(
            listOf(
                ConsumptionSpan(
                    startEntryId = opener.id,
                    endEntryId = closer.id,
                    startDate = opener.date,
                    endDate = closer.date,
                    distance = distance,
                    volume = volume,
                    energy = energy,
                    cost = cost,
                    mlPer100km = mlPer100km,
                    whPerKm = whPerKm,
                    valid = valid,
                    reason = reason,
                ),
            ),
        )
    }
}
