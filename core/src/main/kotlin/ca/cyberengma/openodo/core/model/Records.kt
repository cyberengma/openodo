// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.Metres
import java.time.LocalDate

data class ExpenseRecord(
    val id: Long = 0,
    val vehicleId: Long,
    val typeId: Long,
    val date: LocalDate,
    val odometer: Metres,
    val title: String,
    val description: String?,
    val cost: Money,
    val performedBy: PerformedBy,
    val shopName: String?,
    val warrantyUntil: LocalDate?,
    val receiptFileName: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class Reminder(
    val id: Long = 0,
    val vehicleId: Long,
    val typeId: Long,
    val intervalDistance: Metres?,
    val intervalMonths: Int?,
    val anchorDate: LocalDate?,
    val anchorOdometer: Metres?,
    val active: Boolean,
)
