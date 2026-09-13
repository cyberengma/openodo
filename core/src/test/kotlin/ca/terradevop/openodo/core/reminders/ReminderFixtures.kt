// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.reminders

import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.PerformedBy
import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.units.Metres
import java.time.LocalDate

internal val TODAY: LocalDate = LocalDate.of(2026, 6, 15)

internal fun reminder(
    id: Long = 1,
    distance: Long? = null,
    months: Int? = null,
    anchorDate: LocalDate? = TODAY.minusMonths(1),
    anchorMetres: Long? = 100_000_000,
    active: Boolean = true,
): Reminder = Reminder(
    id = id,
    vehicleId = 1,
    typeId = 10,
    intervalDistance = distance?.let(::Metres),
    intervalMonths = months,
    anchorDate = anchorDate,
    anchorOdometer = anchorMetres?.let(::Metres),
    active = active,
)

internal fun record(
    id: Long,
    date: LocalDate,
    odometer: Long,
    vehicleId: Long = 1,
    typeId: Long = 10,
) = ExpenseRecord(
    id = id,
    vehicleId = vehicleId,
    typeId = typeId,
    date = date,
    odometer = Metres(odometer),
    title = "service",
    description = null,
    cost = Money(100, "CAD"),
    performedBy = PerformedBy.SELF,
    shopName = null,
    warrantyUntil = null,
    receiptFileName = null,
    notes = null,
    createdAt = id,
    updatedAt = id,
)
