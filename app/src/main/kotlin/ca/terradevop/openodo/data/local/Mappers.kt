// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.data.local

import ca.terradevop.openodo.core.model.*
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.UnitPrice
import ca.terradevop.openodo.core.units.*
import java.time.LocalDate

fun VehicleEntity.toCore() = Vehicle(id,name,make,model,year,vin,photoFileName,DistanceUnit.valueOf(distanceUnit),VolumeUnit.valueOf(volumeUnit),EnergyUnit.valueOf(energyUnit),currency,manualOdometerMetres?.let(::Metres),manualOdometerAt,isArchived,notes,createdAt,updatedAt)
fun Vehicle.toEntity() = VehicleEntity(id,name,make,model,year,vin,photoFileName,distanceUnit.name,volumeUnit.name,energyUnit.name,currency,manualOdometer?.value,manualOdometerAt,isArchived,notes,createdAt,updatedAt)

fun RecordTypeEntity.toCore() = RecordType(id,RecordCategory.valueOf(category),name,isDefault)
fun RecordType.toEntity() = RecordTypeEntity(id,category.name,name,isDefault)

fun FuelEntryEntity.toCore() = FuelEntry(id,vehicleId,LocalDate.parse(date),Metres(odometerMetres),FuelKind.valueOf(kind),volumeMillilitres?.let(::Millilitres),energyWattHours?.let(::WattHours),unitPriceMilli?.let { UnitPrice(it,unitPriceCurrency!!) },Money(totalCostMinor,totalCostCurrency),fuelLabel,fullTank,missedPreviousFillUp,stationName,receiptFileName,notes,createdAt,updatedAt)
fun FuelEntry.toEntity() = FuelEntryEntity(id,vehicleId,date.toString(),odometer.value,kind.name,volume?.value,energy?.value,unitPrice?.milli,unitPrice?.currency,totalCost.minor,totalCost.currency,fuelLabel,fullTank,missedPreviousFillUp,stationName,receiptFileName,notes,createdAt,updatedAt)

fun ExpenseRecordEntity.toCore() = ExpenseRecord(id,vehicleId,typeId,LocalDate.parse(date),Metres(odometerMetres),title,description,Money(costMinor,costCurrency),PerformedBy.valueOf(performedBy),shopName,warrantyUntil?.let(LocalDate::parse),receiptFileName,notes,createdAt,updatedAt)
fun ExpenseRecord.toEntity() = ExpenseRecordEntity(id,vehicleId,typeId,date.toString(),odometer.value,title,description,cost.minor,cost.currency,performedBy.name,shopName,warrantyUntil?.toString(),receiptFileName,notes,createdAt,updatedAt)

fun ReminderEntity.toCore() = Reminder(id,vehicleId,typeId,intervalDistanceMetres?.let(::Metres),intervalMonths,anchorDate?.let(LocalDate::parse),anchorOdometerMetres?.let(::Metres),active)
fun Reminder.toEntity() = ReminderEntity(id,vehicleId,typeId,intervalDistance?.value,intervalMonths,anchorDate?.toString(),anchorOdometer?.value,active)
