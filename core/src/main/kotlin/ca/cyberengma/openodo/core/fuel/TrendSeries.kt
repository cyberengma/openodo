// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.fuel

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class TrendPoint(
    val date: LocalDate,
    val value: Long,
    val movingAverage: BigDecimal,
)

object TrendSeries {
    fun consumption(spans: List<ConsumptionSpan>, window: Int = 5): List<TrendPoint> {
        require(window > 0) { "window must be positive" }
        val valid = spans.filter { it.valid && (it.mlPer100km != null || it.whPerKm != null) }
            .sortedBy { it.endDate }
        return valid.mapIndexed { index, span ->
            val values = valid.subList(maxOf(0, index - window + 1), index + 1)
                .map { it.mlPer100km ?: it.whPerKm!! }
            TrendPoint(
                date = span.endDate,
                value = span.mlPer100km ?: span.whPerKm!!,
                movingAverage = BigDecimal.valueOf(values.sum()).divide(BigDecimal(values.size), 6, RoundingMode.HALF_EVEN),
            )
        }
    }
}
