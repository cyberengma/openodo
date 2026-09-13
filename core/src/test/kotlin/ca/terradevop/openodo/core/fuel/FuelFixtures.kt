// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.fuel

import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.FuelKind
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.UnitPrice
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import ca.terradevop.openodo.core.units.WattHours
import java.time.LocalDate

internal fun fuel(
    id: Long,
    date: LocalDate,
    odometerKm: Long,
    volumeMl: Long? = 10_000,
    energyWh: Long? = null,
    fullTank: Boolean = false,
    missed: Boolean = false,
    cost: Long = 1_000,
    currency: String = "CAD",
    label: String = "regular",
    unitPrice: Long? = 1_599,
): FuelEntry = FuelEntry(
    id = id,
    vehicleId = 1,
    date = date,
    odometer = Metres(odometerKm * 1_000),
    kind = if (energyWh == null) FuelKind.LIQUID else FuelKind.ELECTRIC,
    volume = volumeMl?.let(::Millilitres),
    energy = energyWh?.let(::WattHours),
    unitPrice = unitPrice?.let { UnitPrice(it, currency) },
    totalCost = Money(cost, currency),
    fuelLabel = label,
    fullTank = fullTank,
    missedPreviousFillUp = missed,
    stationName = null,
    receiptFileName = null,
    notes = null,
    createdAt = id,
    updatedAt = id,
)

internal fun spans(result: SpanBuildResult): List<ConsumptionSpan> =
    (result as SpanBuildResult.Spans).values
