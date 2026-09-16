// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.validation

import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.PerformedBy
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.Metres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExpenseRecordValidatorTest {
    @Test
    fun `positive cost accepted`() {
        assertTrue(Validators.expenseRecord(record(cost = 49_990)) is ValidationResult.Ok)
    }

    @Test
    fun `zero cost accepted`() {
        assertTrue(Validators.expenseRecord(record(cost = 0)) is ValidationResult.Ok)
    }

    @Test
    fun `negative cost rejected`() {
        val result = Validators.expenseRecord(record(cost = -1))
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(listOf(ValidationErrorCode.NEGATIVE_COST), (result as ValidationResult.Error).codes)
    }

    private fun record(cost: Long) = ExpenseRecord(
        vehicleId = 1L,
        typeId = 12L,
        date = LocalDate.of(2026, 7, 4),
        odometer = Metres(180_000_000),
        title = "oil change",
        description = null,
        cost = Money(cost, "CAD"),
        performedBy = PerformedBy.SHOP,
        shopName = null,
        warrantyUntil = null,
        receiptFileName = null,
        notes = null,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
