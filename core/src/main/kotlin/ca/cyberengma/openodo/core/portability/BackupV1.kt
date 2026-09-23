// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.portability

import ca.cyberengma.openodo.core.model.*
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.UnitPrice
import ca.cyberengma.openodo.core.units.*
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object BackupV1 {
    @Serializable private data class File(val schemaVersion: Int = 1, val exportedAt: Long, val appVersion: String, val vehicles: List<VehicleDto> = emptyList(), val recordTypes: List<RecordTypeDto> = emptyList(), val fuelEntries: List<FuelDto> = emptyList(), val expenseRecords: List<ExpenseDto> = emptyList(), val reminders: List<ReminderDto> = emptyList(), val photos: List<String> = emptyList())
    @Serializable private data class VehicleDto(val id: Long, val name: String, val make: String, val model: String, val year: Int, val vin: String?, val photoFileName: String?, val distanceUnit: String, val volumeUnit: String, val energyUnit: String, val currency: String, val fuelType: String = "FUEL", val manualOdometer: Long?, val manualOdometerAt: Long?, val isArchived: Boolean, val notes: String?, val createdAt: Long, val updatedAt: Long)
    @Serializable private data class RecordTypeDto(val id: Long, val category: String, val name: String, val isDefault: Boolean)
    @Serializable private data class FuelDto(val id: Long, val vehicleId: Long, val date: String, val odometer: Long, val kind: String, val volume: Long?, val energy: Long?, val unitPriceMilli: Long?, val unitPriceCurrency: String?, val totalCostMinor: Long, val totalCostCurrency: String, val fuelLabel: String, val fullTank: Boolean, val missedPreviousFillUp: Boolean, val stationName: String?, val receiptFileName: String?, val notes: String?, val createdAt: Long, val updatedAt: Long)
    @Serializable private data class ExpenseLineItemDto(val typeId: Long, val costMinor: Long, val costCurrency: String)
    @Serializable private data class ExpenseDto(val id: Long, val vehicleId: Long, val typeId: Long? = null, val category: String = "SERVICE", val date: String, val odometer: Long, val title: String? = null, val description: String?, val costMinor: Long? = null, val costCurrency: String? = null, val performedBy: String, val shopName: String?, val warrantyUntil: String?, val receiptFileName: String?, val notes: String?, val lineItems: List<ExpenseLineItemDto> = emptyList(), val createdAt: Long, val updatedAt: Long)
    @Serializable private data class ReminderDto(val id: Long, val vehicleId: Long, val typeId: Long, val intervalDistance: Long?, val intervalMonths: Int?, val anchorDate: String?, val anchorOdometer: Long?, val active: Boolean)

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = true }

    fun write(domain: PortabilityDomain, exportedAt: Long, appVersion: String): String = json.encodeToString(
        File(1, exportedAt, appVersion, domain.vehicles.map { VehicleDto(it.id,it.name,it.make,it.model,it.year,it.vin,it.photoFileName,it.distanceUnit.name,it.volumeUnit.name,it.energyUnit.name,it.currency,it.fuelType.name,it.manualOdometer?.value,it.manualOdometerAt,it.isArchived,it.notes,it.createdAt,it.updatedAt) }, domain.recordTypes.map { RecordTypeDto(it.id,it.category.name,it.name,it.isDefault) }, domain.fuelEntries.map { FuelDto(it.id,it.vehicleId,it.date.toString(),it.odometer.value,it.kind.name,it.volume?.value,it.energy?.value,it.unitPrice?.milli,it.unitPrice?.currency,it.totalCost.minor,it.totalCost.currency,it.fuelLabel,it.fullTank,it.missedPreviousFillUp,it.stationName,it.receiptFileName,it.notes,it.createdAt,it.updatedAt) }, domain.expenseRecords.map { ExpenseDto(id = it.id, vehicleId = it.vehicleId, category = it.category.name, date = it.date.toString(), odometer = it.odometer.value, description = it.description, performedBy = it.performedBy.name, shopName = it.shopName, warrantyUntil = it.warrantyUntil?.toString(), receiptFileName = it.receiptFileName, notes = it.notes, lineItems = it.lineItems.map { li -> ExpenseLineItemDto(li.typeId, li.cost.minor, li.cost.currency) }, createdAt = it.createdAt, updatedAt = it.updatedAt) }, domain.reminders.map { ReminderDto(it.id,it.vehicleId,it.typeId,it.intervalDistance?.value,it.intervalMonths,it.anchorDate?.toString(),it.anchorOdometer?.value,it.active) }, domain.photos),
    )

    fun read(source: String): Result<PortabilityDomain> = runCatching {
        val file = json.decodeFromString<File>(source)
        require(file.schemaVersion == 1) { "unsupported schema:${file.schemaVersion}" }
        PortabilityDomain(file.vehicles.map { Vehicle(it.id,it.name,it.make,it.model,it.year,it.vin,it.photoFileName,DistanceUnit.valueOf(it.distanceUnit),VolumeUnit.valueOf(it.volumeUnit),EnergyUnit.valueOf(it.energyUnit),it.currency,FuelType.valueOf(it.fuelType),it.manualOdometer?.let(::Metres),it.manualOdometerAt,it.isArchived,it.notes,it.createdAt,it.updatedAt) }, file.recordTypes.map { RecordType(it.id,RecordCategory.valueOf(it.category),it.name,it.isDefault) }, file.fuelEntries.map { FuelEntry(it.id,it.vehicleId,LocalDate.parse(it.date),Metres(it.odometer),FuelKind.valueOf(it.kind),it.volume?.let(::Millilitres),it.energy?.let(::WattHours),it.unitPriceMilli?.let { m -> UnitPrice(m,it.unitPriceCurrency!!) },Money(it.totalCostMinor,it.totalCostCurrency),it.fuelLabel,it.fullTank,it.missedPreviousFillUp,it.stationName,it.receiptFileName,it.notes,it.createdAt,it.updatedAt) }, file.expenseRecords.map { dto ->
            val items = if (dto.lineItems.isNotEmpty()) dto.lineItems.map { ExpenseLineItem(it.typeId, Money(it.costMinor, it.costCurrency)) }
                else listOfNotNull(dto.typeId?.let { typeId -> dto.costMinor?.let { minor -> ExpenseLineItem(typeId, Money(minor, dto.costCurrency ?: "")) } })
            ExpenseRecord(dto.id,dto.vehicleId,RecordCategory.valueOf(dto.category),LocalDate.parse(dto.date),Metres(dto.odometer),dto.description,PerformedBy.valueOf(dto.performedBy),dto.shopName,dto.warrantyUntil?.let(LocalDate::parse),dto.receiptFileName,dto.notes,items,dto.createdAt,dto.updatedAt)
        }, file.reminders.map { Reminder(it.id,it.vehicleId,it.typeId,it.intervalDistance?.let(::Metres),it.intervalMonths,it.anchorDate?.let(LocalDate::parse),it.anchorOdometer?.let(::Metres),it.active) }, file.photos)
    }.recoverCatching { error -> throw IllegalArgumentException(error.message ?: "invalid backup", error) }
}
