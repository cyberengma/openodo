// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.money

import ca.cyberengma.openodo.core.units.Millilitres
import java.math.BigDecimal
import java.math.RoundingMode

data class UnitPrice(val milli: Long, val currency: String) {
    fun total(volume: Millilitres): Money {
        val digits = CurrencyMinorDigits.of(currency)
        val minor = BigDecimal(milli)
            .multiply(BigDecimal(volume.value))
            .multiply(BigDecimal(10).pow(digits))
            .divide(BigDecimal(1_000_000), 0, RoundingMode.HALF_EVEN)
            .toLong()
        return Money(minor, currency)
    }
}
