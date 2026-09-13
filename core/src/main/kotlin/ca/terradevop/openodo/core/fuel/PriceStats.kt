// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import ca.terradevop.openodo.core.model.FuelEntry
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class PriceSummary(
    val label: String,
    val minimum: Long,
    val maximum: Long,
    val average: BigDecimal,
)

object PriceStats {
    fun of(entries: List<FuelEntry>, from: LocalDate? = null, to: LocalDate? = null): List<PriceSummary> =
        entries.asSequence()
            .filter { it.unitPrice != null }
            .filter { from == null || !it.date.isBefore(from) }
            .filter { to == null || !it.date.isAfter(to) }
            .groupBy { it.fuelLabel }
            .map { (label, values) ->
                val prices = values.map { it.unitPrice!!.milli }
                PriceSummary(
                    label = label,
                    minimum = prices.min(),
                    maximum = prices.max(),
                    average = BigDecimal(prices.sum()).divide(BigDecimal(prices.size), 6, RoundingMode.HALF_EVEN),
                )
            }
            .sortedBy { it.label }
}
