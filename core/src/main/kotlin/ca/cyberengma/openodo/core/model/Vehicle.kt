// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.core.units.EnergyUnit
import ca.cyberengma.openodo.core.units.Metres
import ca.cyberengma.openodo.core.units.VolumeUnit

data class Vehicle(
    val id: Long = 0,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val vin: String?,
    val photoFileName: String?,
    val distanceUnit: DistanceUnit,
    val volumeUnit: VolumeUnit,
    val energyUnit: EnergyUnit,
    val currency: String,
    val manualOdometer: Metres?,
    val manualOdometerAt: Long?,
    val isArchived: Boolean,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class RecordType(
    val id: Long = 0,
    val category: RecordCategory,
    val name: String,
    val isDefault: Boolean,
)
