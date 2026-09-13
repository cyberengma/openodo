// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.data

import ca.terradevop.openodo.core.model.Vehicle
import ca.terradevop.openodo.core.units.DistanceUnit
import ca.terradevop.openodo.core.units.EnergyUnit
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.VolumeUnit

internal fun vehicle(
    id: Long = 0,
    name: String = "Metric Car",
    distance: DistanceUnit = DistanceUnit.KILOMETRES,
    volume: VolumeUnit = VolumeUnit.LITRES,
    energy: EnergyUnit = EnergyUnit.KILOWATT_HOURS,
    currency: String = "CAD",
    archived: Boolean = false,
) = Vehicle(
    id = id,
    name = name,
    make = "Make",
    model = "Model",
    year = 2024,
    vin = "VIN-1",
    photoFileName = "photo.jpg",
    distanceUnit = distance,
    volumeUnit = volume,
    energyUnit = energy,
    currency = currency,
    manualOdometer = Metres(12_345),
    manualOdometerAt = 100,
    isArchived = archived,
    notes = "notes",
    createdAt = 1,
    updatedAt = 2,
)
