// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.fuel

import java.math.BigDecimal
import java.math.RoundingMode

object Economy {
    fun litresPer100Km(mlPer100Km: Long, scale: Int): BigDecimal =
        divide(BigDecimal(mlPer100Km), BigDecimal(1_000), scale)

    fun kmPerLitre(mlPer100Km: Long, scale: Int): BigDecimal =
        divide(BigDecimal(100_000), BigDecimal(mlPer100Km), scale)

    fun mpgUs(mlPer100Km: Long, scale: Int): BigDecimal =
        divide(BigDecimal("235.214583"), litresPer100Km(mlPer100Km, 12), scale)

    fun mpgUk(mlPer100Km: Long, scale: Int): BigDecimal =
        divide(BigDecimal("282.481054"), litresPer100Km(mlPer100Km, 12), scale)

    fun kilowattHoursPer100Km(whPerKm: Long, scale: Int): BigDecimal =
        divide(BigDecimal(whPerKm), BigDecimal(10), scale)

    fun milesPerKilowattHour(whPerKm: Long, scale: Int): BigDecimal =
        divide(BigDecimal("621.37119"), BigDecimal(whPerKm), scale)

    private fun divide(numerator: BigDecimal, denominator: BigDecimal, scale: Int): BigDecimal {
        require(scale >= 0) { "scale must not be negative" }
        if (denominator.signum() == 0) return BigDecimal.ZERO.setScale(scale)
        return numerator.divide(denominator, scale, RoundingMode.HALF_EVEN)
    }
}
