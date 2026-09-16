// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun `plus same currency`() {
        val result = Money(1_599, "CAD").plus(Money(401, "CAD"))
        assertEquals(MoneyResult.Success(Money(2_000, "CAD")), result)
    }

    @Test
    fun `minus same currency`() {
        val result = Money(2_000, "CAD").minus(Money(1_599, "CAD"))
        assertEquals(MoneyResult.Success(Money(401, "CAD")), result)
    }

    @Test
    fun `minus yields negative for credit`() {
        val result = Money(100, "EUR").minus(Money(250, "EUR"))
        assertEquals(MoneyResult.Success(Money(-150, "EUR")), result)
    }

    @Test
    fun `times scales minor units`() {
        assertEquals(Money(4_500, "CAD"), Money(1_500, "CAD").times(3))
        assertEquals(Money(0, "CAD"), Money(1_500, "CAD").times(0))
        assertEquals(Money(-3_000, "CAD"), Money(1_500, "CAD").times(-2))
    }

    @Test
    fun `sum of same currency`() {
        val result = Money.sum(listOf(Money(100, "USD"), Money(200, "USD"), Money(300, "USD")))
        assertEquals(MoneyResult.Success(Money(600, "USD")), result)
    }

    @Test
    fun `sum single`() {
        assertEquals(MoneyResult.Success(Money(50, "USD")), Money.sum(listOf(Money(50, "USD"))))
    }

    @Test
    fun `sum empty is zero`() {
        assertEquals(MoneyResult.Success(Money(0L, "")), Money.sum(emptyList()))
    }

    @Test
    fun `mixed currency plus returns typed error`() {
        val result = Money(100, "EUR").plus(Money(100, "USD"))
        assertTrue("expected typed error, got $result", result is MoneyResult.MixedCurrency)
        assertEquals(MoneyResult.MixedCurrency("EUR", "USD"), result)
    }

    @Test
    fun `mixed currency inside sum returns typed error`() {
        val result = Money.sum(listOf(Money(1, "CAD"), Money(2, "CAD"), Money(1, "EUR")))
        assertEquals(MoneyResult.MixedCurrency("CAD", "EUR"), result)
    }
}
