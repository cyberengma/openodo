// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.ui

import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.core.units.EnergyUnit
import ca.cyberengma.openodo.core.units.Metres
import ca.cyberengma.openodo.core.units.VolumeUnit
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleValueFormatterTest {
    @Test fun `parses locale decimal separators exactly`() {
        assertEquals("1.5", VehicleValueFormatter.parseDecimal("1.5", Locale.US).toString())
        assertEquals("1.5", VehicleValueFormatter.parseDecimal("1,5", Locale.GERMANY).toString())
        assertNull(VehicleValueFormatter.parseDecimal("1.5junk", Locale.US))
    }

    @Test fun `parses vehicle distance units into canonical metres`() {
        assertEquals(10_000L, VehicleValueFormatter.parseDistance("10", DistanceUnit.KILOMETRES, Locale.US)?.value)
        assertEquals(16_093L, VehicleValueFormatter.parseDistance("10", DistanceUnit.MILES, Locale.US)?.value)
        assertEquals(10L, VehicleValueFormatter.parseDistance("10", DistanceUnit.METRES, Locale.US)?.value)
    }

    @Test fun `parses litres us gallons uk gallons and millilitres`() {
        assertEquals(10_000L, VehicleValueFormatter.parseVolume("10", VolumeUnit.LITRES, Locale.US)?.value)
        assertEquals(37_854L, VehicleValueFormatter.parseVolume("10", VolumeUnit.US_GALLONS, Locale.US)?.value)
        assertEquals(45_461L, VehicleValueFormatter.parseVolume("10", VolumeUnit.UK_GALLONS, Locale.US)?.value)
        assertEquals(10L, VehicleValueFormatter.parseVolume("10", VolumeUnit.MILLILITRES, Locale.US)?.value)
    }

    @Test fun `parses energy units`() {
        assertEquals(12_400L, VehicleValueFormatter.parseEnergy("12.4", EnergyUnit.KILOWATT_HOURS, Locale.US)?.value)
        assertEquals(12L, VehicleValueFormatter.parseEnergy("12", EnergyUnit.WATT_HOURS, Locale.US)?.value)
    }

    @Test fun `money handles zero two and three minor digits`() {
        assertEquals(Money(1_000, "JPY"), VehicleValueFormatter.parseMoney("1000", "JPY", Locale.US))
        assertEquals(Money(4_317, "USD"), VehicleValueFormatter.parseMoney("43.17", "USD", Locale.US))
        assertEquals(Money(1_234, "KWD"), VehicleValueFormatter.parseMoney("1.234", "KWD", Locale.US))
        assertEquals("43.17", VehicleValueFormatter.formatMoney(Money(4_317, "USD"), Locale.US))
    }

    @Test fun `formats canonical distance in active unit`() {
        assertEquals("10", VehicleValueFormatter.formatDistance(Metres(10_000), DistanceUnit.KILOMETRES, Locale.US))
        assertEquals("6.214", VehicleValueFormatter.formatDistance(Metres(10_000), DistanceUnit.MILES, Locale.US))
    }
}
