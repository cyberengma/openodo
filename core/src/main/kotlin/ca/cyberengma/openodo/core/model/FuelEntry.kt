// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.UnitPrice
import ca.cyberengma.openodo.core.units.Metres
import ca.cyberengma.openodo.core.units.Millilitres
import ca.cyberengma.openodo.core.units.WattHours
import java.time.LocalDate

data class FuelEntry(
    val id: Long = 0,
    val vehicleId: Long,
    val date: LocalDate,
    val odometer: Metres,
    val kind: FuelKind,
    val volume: Millilitres?,
    val energy: WattHours?,
    val unitPrice: UnitPrice?,
    val totalCost: Money,
    val fuelLabel: String,
    val fullTank: Boolean,
    val missedPreviousFillUp: Boolean,
    val stationName: String?,
    val receiptFileName: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
