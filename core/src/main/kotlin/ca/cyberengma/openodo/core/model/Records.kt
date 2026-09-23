// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.MoneyResult
import ca.cyberengma.openodo.core.units.Metres
import java.time.LocalDate

data class ExpenseLineItem(
    val typeId: Long,
    val cost: Money,
)

data class ExpenseRecord(
    val id: Long = 0,
    val vehicleId: Long,
    val category: RecordCategory,
    val date: LocalDate,
    val odometer: Metres,
    val description: String?,
    val performedBy: PerformedBy,
    val shopName: String?,
    val warrantyUntil: LocalDate?,
    val receiptFileName: String?,
    val notes: String?,
    val lineItems: List<ExpenseLineItem>,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val totalCost: Money
        get() = lineItems.fold(Money(0, lineItems.firstOrNull()?.cost?.currency ?: "")) { acc, item ->
            when (val sum = acc.plus(item.cost)) {
                is MoneyResult.Success -> sum.money
                is MoneyResult.MixedCurrency -> acc
            }
        }
}

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
