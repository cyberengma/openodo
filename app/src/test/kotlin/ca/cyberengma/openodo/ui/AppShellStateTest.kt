// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.ui

import ca.cyberengma.openodo.core.model.Vehicle
import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.core.units.EnergyUnit
import ca.cyberengma.openodo.core.units.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class AppShellStateTest {
    @Test
    fun `empty shell has no active vehicle`() {
        val state = ShellState()
        assertEquals(null, state.activeVehicleId)
        assertEquals(0, state.vehicles.size)
    }

    @Test
    fun `vehicle configurations remain independent`() {
        val metric = vehicle(1, "Metric", DistanceUnit.KILOMETRES, VolumeUnit.LITRES, "CAD")
        val imperial = vehicle(2, "Imperial", DistanceUnit.MILES, VolumeUnit.US_GALLONS, "USD")
        val state = ShellState(listOf(metric, imperial), activeVehicleId = imperial.id)
        val active = state.vehicles.first { it.id == state.activeVehicleId }
        assertEquals(DistanceUnit.MILES, active.distanceUnit)
        assertEquals(VolumeUnit.US_GALLONS, active.volumeUnit)
        assertEquals("USD", active.currency)
        assertEquals(DistanceUnit.KILOMETRES, state.vehicles.first().distanceUnit)
    }

    @Test
    fun `vehicle unit values are suitable for accessible labels`() {
        val item = vehicle(1, "Daily", DistanceUnit.KILOMETRES, VolumeUnit.LITRES, "CAD")
        val label = "${item.name}, ${item.distanceUnit.name}, ${item.volumeUnit.name}, ${item.currency}"
        assertEquals("Daily, KILOMETRES, LITRES, CAD", label)
    }

    private fun vehicle(id: Long, name: String, distance: DistanceUnit, volume: VolumeUnit, currency: String) = Vehicle(
        id = id, name = name, make = "Make", model = "Model", year = 2024,
        vin = null, photoFileName = null, distanceUnit = distance, volumeUnit = volume,
        energyUnit = EnergyUnit.KILOWATT_HOURS, currency = currency,
        manualOdometer = null, manualOdometerAt = null, isArchived = false,
        notes = null, createdAt = 0, updatedAt = 0,
    )
}
