// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.units

import java.math.BigDecimal
import java.math.RoundingMode

private const val CONVERSION_SCALE = 3

private val MILLIMETRES_PER_METRE = BigDecimal(1_000)
private val MILLIMETRES_PER_MILE = BigDecimal("1609344")

@JvmInline
value class Metres(val value: Long) {
    fun toKilometres(): Long = value / 1_000L

    fun to(unit: DistanceUnit): BigDecimal = when (unit) {
        DistanceUnit.METRES -> BigDecimal(value)
        DistanceUnit.KILOMETRES -> BigDecimal(value).divide(MILLIMETRES_PER_METRE, CONVERSION_SCALE, RoundingMode.HALF_EVEN)
        DistanceUnit.MILES -> BigDecimal(value).multiply(BigDecimal(1_000))
            .divide(MILLIMETRES_PER_MILE, CONVERSION_SCALE, RoundingMode.HALF_EVEN)
    }

    companion object {
        fun of(kilometres: Long): Metres = Metres(kilometres * 1_000L)

        fun kilometres(amount: BigDecimal): Metres =
            amount.multiply(MILLIMETRES_PER_METRE).setScale(0, RoundingMode.HALF_EVEN).toLong().let(::Metres)

        fun miles(amount: BigDecimal): Metres =
            amount.multiply(MILLIMETRES_PER_MILE).divide(BigDecimal(1_000), 0, RoundingMode.HALF_EVEN).toLong().let(::Metres)
    }
}
