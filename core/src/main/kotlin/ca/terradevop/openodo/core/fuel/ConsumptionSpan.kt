// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import ca.terradevop.openodo.core.units.WattHours
import java.time.LocalDate

enum class FuelSpanReason {
    MISSED_FILL_UP,
    NEGATIVE_DISTANCE,
    ZERO_VOLUME,
}

data class ConsumptionSpan(
    val startEntryId: Long,
    val endEntryId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val distance: Metres,
    val volume: Millilitres?,
    val energy: WattHours?,
    val cost: Money,
    val mlPer100km: Long?,
    val whPerKm: Long?,
    val valid: Boolean,
    val reason: FuelSpanReason?,
)

sealed class SpanBuildResult {
    data class Spans(val values: List<ConsumptionSpan>) : SpanBuildResult()

    data class MixedCurrency(val left: String, val right: String) : SpanBuildResult()
}
