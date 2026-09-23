// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.core.units.EnergyUnit
import ca.cyberengma.openodo.core.units.Metres
import ca.cyberengma.openodo.core.units.Millilitres
import ca.cyberengma.openodo.core.units.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ModelsTest {
    private val today = LocalDate.of(2026, 9, 13)

    @Test
    fun `vehicle keeps fields and defaults unsaved id`() {
        val vehicle = Vehicle(
            name = "Pickup",
            make = "Ford",
            model = "F-250",
            year = 2019,
            vin = null,
            photoFileName = null,
            distanceUnit = DistanceUnit.KILOMETRES,
            volumeUnit = VolumeUnit.LITRES,
            energyUnit = EnergyUnit.KILOWATT_HOURS,
            currency = "CAD",
            fuelType = FuelType.FUEL,
            manualOdometer = null,
            manualOdometerAt = null,
            isArchived = false,
            notes = null,
            createdAt = 1L,
            updatedAt = 1L,
        )
        assertEquals(0L, vehicle.id)
        assertEquals("Pickup", vehicle.name)
    }

    @Test
    fun `equal fuel entries are equal`() {
        val entry = fuelEntry()
        assertEquals(entry, entry.copy(notes = null))
        assertTrue(entry != fuelEntry(notes = "receipt scanned"))
    }

    @Test
    fun `reminder without anchor stays unanchored`() {
        val reminder = Reminder(
            vehicleId = 1L,
            typeId = 2L,
            intervalDistance = Metres(10_000_000),
            intervalMonths = null,
            anchorDate = null,
            anchorOdometer = null,
            active = true,
        )
        assertTrue(reminder.anchorDate == null && reminder.anchorOdometer == null)
    }

    private fun fuelEntry(notes: String? = null) = FuelEntry(
        vehicleId = 1L,
        date = today,
        odometer = Metres(159_000_000),
        kind = FuelKind.LIQUID,
        volume = Millilitres(60_000),
        energy = null,
        unitPrice = null,
        totalCost = Money(100_000, "CAD"),
        fuelLabel = "regular",
        fullTank = true,
        missedPreviousFillUp = false,
        stationName = null,
        receiptFileName = null,
        notes = notes,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
