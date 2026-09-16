// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.units

import java.math.BigDecimal
import java.math.RoundingMode

private const val CONVERSION_SCALE = 3

private val WH_PER_KWH = BigDecimal(1_000)

@JvmInline
value class WattHours(val value: Long) {
    fun to(unit: EnergyUnit): BigDecimal = when (unit) {
        EnergyUnit.WATT_HOURS -> BigDecimal(value)
        EnergyUnit.KILOWATT_HOURS -> BigDecimal(value).divide(WH_PER_KWH, CONVERSION_SCALE, RoundingMode.HALF_EVEN)
    }

    companion object {
        fun kilowattHours(amount: BigDecimal): WattHours =
            amount.multiply(WH_PER_KWH).setScale(0, RoundingMode.HALF_EVEN).toLong().let(::WattHours)
    }
}
