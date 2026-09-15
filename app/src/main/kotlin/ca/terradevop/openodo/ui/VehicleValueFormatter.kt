// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui

import ca.terradevop.openodo.core.money.CurrencyMinorDigits
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.UnitPrice
import ca.terradevop.openodo.core.units.DistanceUnit
import ca.terradevop.openodo.core.units.EnergyUnit
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import ca.terradevop.openodo.core.units.VolumeUnit
import ca.terradevop.openodo.core.units.WattHours
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

object VehicleValueFormatter {
    fun parseDecimal(text: String, locale: Locale): BigDecimal? {
        val source = text.trim()
        if (source.isEmpty()) return null
        val format = NumberFormat.getNumberInstance(locale) as? DecimalFormat ?: return null
        format.isParseBigDecimal = true
        val position = ParsePosition(0)
        val parsed = format.parse(source, position) as? BigDecimal
        return parsed?.takeIf { position.index == source.length }
    }

    fun parseDistance(text: String, unit: DistanceUnit, locale: Locale): Metres? =
        parseDecimal(text, locale)?.let { amount ->
            when (unit) {
                DistanceUnit.METRES -> amount.roundLong()?.let(::Metres)
                DistanceUnit.KILOMETRES -> amount.safe { Metres.kilometres(it) }
                DistanceUnit.MILES -> amount.safe { Metres.miles(it) }
            }
        }

    fun formatDistance(value: Metres, unit: DistanceUnit, locale: Locale): String =
        formatDecimal(value.to(unit), locale, 3)

    fun parseVolume(text: String, unit: VolumeUnit, locale: Locale): Millilitres? =
        parseDecimal(text, locale)?.let { amount ->
            when (unit) {
                VolumeUnit.MILLILITRES -> amount.roundLong()?.let(::Millilitres)
                VolumeUnit.LITRES -> amount.safe { Millilitres.litres(it) }
                VolumeUnit.US_GALLONS -> amount.safe { Millilitres.usGallons(it) }
                VolumeUnit.UK_GALLONS -> amount.safe { Millilitres.ukGallons(it) }
            }
        }

    fun formatVolume(value: Millilitres, unit: VolumeUnit, locale: Locale): String =
        formatDecimal(value.to(unit), locale, 3)

    fun parseEnergy(text: String, unit: EnergyUnit, locale: Locale): WattHours? =
        parseDecimal(text, locale)?.let { amount ->
            when (unit) {
                EnergyUnit.WATT_HOURS -> amount.roundLong()?.let(::WattHours)
                EnergyUnit.KILOWATT_HOURS -> amount.safe { WattHours.kilowattHours(it) }
            }
        }

    fun formatEnergy(value: WattHours, unit: EnergyUnit, locale: Locale): String =
        formatDecimal(value.to(unit), locale, 3)

    fun parseMoney(text: String, currency: String, locale: Locale): Money? {
        val digits = CurrencyMinorDigits.of(currency)
        val minor = parseDecimal(text, locale)
            ?.movePointRight(digits)
            ?.setScale(0, RoundingMode.HALF_EVEN)
            ?.toLongExactOrNull()
            ?: return null
        return Money(minor, currency.uppercase(Locale.ROOT))
    }

    fun formatMoney(value: Money, locale: Locale): String {
        val digits = CurrencyMinorDigits.of(value.currency)
        return formatDecimal(BigDecimal(value.minor).movePointLeft(digits), locale, digits, digits)
    }

    fun parseUnitPrice(text: String, currency: String, locale: Locale): UnitPrice? {
        val milli = parseDecimal(text, locale)
            ?.movePointRight(3)
            ?.setScale(0, RoundingMode.HALF_EVEN)
            ?.toLongExactOrNull()
            ?: return null
        return UnitPrice(milli, currency.uppercase(Locale.ROOT))
    }

    fun formatUnitPrice(value: UnitPrice, locale: Locale): String =
        formatDecimal(BigDecimal(value.milli).movePointLeft(3), locale, 3)

    fun formatDecimal(value: BigDecimal, locale: Locale, maximumFractionDigits: Int, minimumFractionDigits: Int = 0): String =
        NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = true
            this.maximumFractionDigits = maximumFractionDigits
            this.minimumFractionDigits = minimumFractionDigits
            roundingMode = RoundingMode.HALF_EVEN
        }.format(value)

    private fun BigDecimal.roundLong(): Long? =
        setScale(0, RoundingMode.HALF_EVEN).toLongExactOrNull()

    private fun BigDecimal.toLongExactOrNull(): Long? =
        runCatching { longValueExact() }.getOrNull()

    private inline fun <T> BigDecimal.safe(block: (BigDecimal) -> T): T? =
        runCatching { block(this) }.getOrNull()
}
