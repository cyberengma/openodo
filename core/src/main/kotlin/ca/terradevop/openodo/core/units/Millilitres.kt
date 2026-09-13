// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.units

import java.math.BigDecimal
import java.math.RoundingMode

private const val CONVERSION_SCALE = 3

private val MILLILITRES_PER_LITRE = BigDecimal(1_000)
private val MILLILITRES_PER_US_GALLON = BigDecimal("3785.411784")
private val MILLILITRES_PER_UK_GALLON = BigDecimal("4546.09")

@JvmInline
value class Millilitres(val value: Long) {
    fun toLitres(): BigDecimal = to(VolumeUnit.LITRES)

    fun to(unit: VolumeUnit): BigDecimal = when (unit) {
        VolumeUnit.MILLILITRES -> BigDecimal(value)
        VolumeUnit.LITRES -> divide(MILLILITRES_PER_LITRE)
        VolumeUnit.US_GALLONS -> divide(MILLILITRES_PER_US_GALLON)
        VolumeUnit.UK_GALLONS -> divide(MILLILITRES_PER_UK_GALLON)
    }

    private fun divide(perUnit: BigDecimal): BigDecimal =
        BigDecimal(value).divide(perUnit, CONVERSION_SCALE, RoundingMode.HALF_EVEN)

    companion object {
        fun litres(amount: BigDecimal): Millilitres =
            amount.multiply(MILLILITRES_PER_LITRE).setScale(0, RoundingMode.HALF_EVEN).toLong().let(::Millilitres)

        fun usGallons(amount: BigDecimal): Millilitres =
            amount.multiply(MILLILITRES_PER_US_GALLON).setScale(0, RoundingMode.HALF_EVEN).toLong().let(::Millilitres)

        fun ukGallons(amount: BigDecimal): Millilitres =
            amount.multiply(MILLILITRES_PER_UK_GALLON).setScale(0, RoundingMode.HALF_EVEN).toLong().let(::Millilitres)
    }
}
