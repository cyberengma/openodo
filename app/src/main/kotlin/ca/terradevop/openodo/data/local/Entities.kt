// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val vin: String?,
    val photoFileName: String?,
    val distanceUnit: String,
    val volumeUnit: String,
    val energyUnit: String,
    val currency: String,
    val manualOdometerMetres: Long?,
    val manualOdometerAt: Long?,
    val isArchived: Boolean,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "record_types")
data class RecordTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val name: String,
    val isDefault: Boolean,
)

@Entity(tableName = "fuel_entries")
data class FuelEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: String,
    val odometerMetres: Long,
    val kind: String,
    val volumeMillilitres: Long?,
    val energyWattHours: Long?,
    val unitPriceMilli: Long?,
    val unitPriceCurrency: String?,
    val totalCostMinor: Long,
    val totalCostCurrency: String,
    val fuelLabel: String,
    val fullTank: Boolean,
    val missedPreviousFillUp: Boolean,
    val stationName: String?,
    val receiptFileName: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "expense_records")
data class ExpenseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val typeId: Long,
    val date: String,
    val odometerMetres: Long,
    val title: String,
    val description: String?,
    val costMinor: Long,
    val costCurrency: String,
    val performedBy: String,
    val shopName: String?,
    val warrantyUntil: String?,
    val receiptFileName: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val typeId: Long,
    val intervalDistanceMetres: Long?,
    val intervalMonths: Int?,
    val anchorDate: String?,
    val anchorOdometerMetres: Long?,
    val active: Boolean,
)
