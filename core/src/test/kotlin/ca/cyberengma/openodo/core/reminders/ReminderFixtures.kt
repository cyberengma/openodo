// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.ExpenseLineItem
import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.PerformedBy
import ca.cyberengma.openodo.core.model.RecordCategory
import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.Metres
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
    category = RecordCategory.SERVICE,
    date = date,
    odometer = Metres(odometer),
    description = null,
    performedBy = PerformedBy.SELF,
    shopName = null,
    warrantyUntil = null,
    receiptFileName = null,
    notes = null,
    lineItems = listOf(ExpenseLineItem(typeId, Money(100, "CAD"))),
    createdAt = id,
    updatedAt = id,
)
