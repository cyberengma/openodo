// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.model.FuelKind
import ca.cyberengma.openodo.core.model.FuelType
import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.model.Vehicle
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.core.units.EnergyUnit
import ca.cyberengma.openodo.core.units.Metres
import ca.cyberengma.openodo.core.units.Millilitres
import ca.cyberengma.openodo.core.units.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DueRemindersTest {
    private fun vehicle() = Vehicle(
        name = "Car", make = "Make", model = "Model", year = 2020, vin = null, photoFileName = null,
        distanceUnit = DistanceUnit.KILOMETRES, volumeUnit = VolumeUnit.LITRES, energyUnit = EnergyUnit.KILOWATT_HOURS,
        currency = "USD", fuelType = FuelType.FUEL, manualOdometer = null, manualOdometerAt = null,
        isArchived = false, notes = null, createdAt = 0, updatedAt = 0,
    )

    private fun fuel(odometer: Long) = FuelEntry(
        vehicleId = 1, date = TODAY, odometer = Metres(odometer), kind = FuelKind.LIQUID,
        volume = Millilitres(10_000), energy = null, unitPrice = null, totalCost = Money(1_000, "CAD"),
        fuelLabel = "R", fullTank = true, missedPreviousFillUp = false, stationName = null,
        receiptFileName = null, notes = null, createdAt = 0, updatedAt = 0,
    )

    @Test fun `overdue reminder is returned`() {
        val due = DueReminders.of(vehicle(), listOf(reminder(id = 1, distance = 10_000_000)), listOf(fuel(115_000_000)), emptyList(), TODAY)
        assertEquals(1, due.size)
        assertEquals(ReminderState.OVERDUE, due.first().status.state)
    }

    @Test fun `due soon reminder is returned`() {
        val due = DueReminders.of(vehicle(), listOf(reminder(id = 1, distance = 10_000_000)), listOf(fuel(109_500_000)), emptyList(), TODAY)
        assertEquals(1, due.size)
        assertEquals(ReminderState.DUE_SOON, due.first().status.state)
    }

    @Test fun `not-yet-due reminder is omitted`() {
        val due = DueReminders.of(vehicle(), listOf(reminder(id = 1, distance = 10_000_000)), listOf(fuel(101_000_000)), emptyList(), TODAY)
        assertTrue(due.isEmpty())
    }

    @Test fun `inactive reminder is omitted`() {
        val due = DueReminders.of(vehicle(), listOf(reminder(id = 1, distance = 10_000_000, active = false)), listOf(fuel(115_000_000)), emptyList(), TODAY)
        assertTrue(due.isEmpty())
    }

    @Test fun `unanchored reminder is omitted`() {
        val unanchored = reminder(id = 1, distance = 10_000_000, anchorMetres = null)
        val due = DueReminders.of(vehicle(), listOf(unanchored), listOf(fuel(115_000_000)), emptyList(), TODAY)
        assertTrue(due.isEmpty())
    }
}