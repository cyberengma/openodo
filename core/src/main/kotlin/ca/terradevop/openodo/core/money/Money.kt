// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.money

import ca.terradevop.openodo.core.units.Millilitres
import java.math.BigDecimal
import java.math.RoundingMode

sealed class MoneyResult {
    data class Success(val money: Money) : MoneyResult()

    data class MixedCurrency(val left: String, val right: String) : MoneyResult()
}

data class Money(val minor: Long, val currency: String) {
    fun plus(other: Money): MoneyResult = combine(other, true)

    fun minus(other: Money): MoneyResult = combine(other, false)

    fun times(factor: Int): Money = Money(minor * factor, currency)

    private fun combine(other: Money, add: Boolean): MoneyResult {
        if (currency != other.currency) {
            return MoneyResult.MixedCurrency(currency, other.currency)
        }
        val delta = other.minor * if (add) 1L else -1L
        return MoneyResult.Success(Money(minor + delta, currency))
    }

    companion object {
        fun sum(moneys: List<Money>): MoneyResult {
            if (moneys.isEmpty()) {
                return MoneyResult.Success(Money(0L, ""))
            }
            var total: Money? = null
            for (money in moneys) {
                if (total == null) {
                    total = money
                } else if (money.currency != total.currency) {
                    return MoneyResult.MixedCurrency(total.currency, money.currency)
                } else {
                    total = money.plus(total).let { result ->
                        when (result) {
                            is MoneyResult.Success -> result.money
                            is MoneyResult.MixedCurrency -> return result
                        }
                    }
                }
            }
            return MoneyResult.Success(total!!)
        }
    }
}
