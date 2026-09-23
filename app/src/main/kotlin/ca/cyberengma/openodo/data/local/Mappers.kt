// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.data.local

import ca.cyberengma.openodo.core.model.*
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.UnitPrice
import ca.cyberengma.openodo.core.units.*
import java.time.LocalDate

fun VehicleEntity.toCore() = Vehicle(id,name,make,model,year,vin,photoFileName,DistanceUnit.valueOf(distanceUnit),VolumeUnit.valueOf(volumeUnit),EnergyUnit.valueOf(energyUnit),currency,FuelType.valueOf(fuelType),manualOdometerMetres?.let(::Metres),manualOdometerAt,isArchived,notes,createdAt,updatedAt)
fun Vehicle.toEntity() = VehicleEntity(id,name,make,model,year,vin,photoFileName,distanceUnit.name,volumeUnit.name,energyUnit.name,currency,fuelType.name,manualOdometer?.value,manualOdometerAt,isArchived,notes,createdAt,updatedAt)

fun RecordTypeEntity.toCore() = RecordType(id,RecordCategory.valueOf(category),name,isDefault)
fun RecordType.toEntity() = RecordTypeEntity(id,category.name,name,isDefault)

fun FuelEntryEntity.toCore() = FuelEntry(id,vehicleId,LocalDate.parse(date),Metres(odometerMetres),FuelKind.valueOf(kind),volumeMillilitres?.let(::Millilitres),energyWattHours?.let(::WattHours),unitPriceMilli?.let { UnitPrice(it,unitPriceCurrency!!) },Money(totalCostMinor,totalCostCurrency),fuelLabel,fullTank,missedPreviousFillUp,stationName,receiptFileName,notes,createdAt,updatedAt)
fun FuelEntry.toEntity() = FuelEntryEntity(id,vehicleId,date.toString(),odometer.value,kind.name,volume?.value,energy?.value,unitPrice?.milli,unitPrice?.currency,totalCost.minor,totalCost.currency,fuelLabel,fullTank,missedPreviousFillUp,stationName,receiptFileName,notes,createdAt,updatedAt)

fun ExpenseRecordEntity.toCore() = ExpenseRecord(id,vehicleId,RecordCategory.valueOf(category),LocalDate.parse(date),Metres(odometerMetres),description,PerformedBy.valueOf(performedBy),shopName,warrantyUntil?.let(LocalDate::parse),receiptFileName,notes,emptyList(),createdAt,updatedAt)
fun ExpenseRecord.toEntity() = ExpenseRecordEntity(id,vehicleId,category.name,date.toString(),odometer.value,description,performedBy.name,shopName,warrantyUntil?.toString(),receiptFileName,notes,createdAt,updatedAt)

fun ExpenseLineItemEntity.toCore() = ExpenseLineItem(typeId, Money(costMinor, costCurrency))
fun ExpenseLineItem.toEntity(expenseRecordId: Long) = ExpenseLineItemEntity(0, expenseRecordId, typeId, cost.minor, cost.currency)

fun ReminderEntity.toCore() = Reminder(id,vehicleId,typeId,intervalDistanceMetres?.let(::Metres),intervalMonths,anchorDate?.let(LocalDate::parse),anchorOdometerMetres?.let(::Metres),active)
fun Reminder.toEntity() = ReminderEntity(id,vehicleId,typeId,intervalDistance?.value,intervalMonths,anchorDate?.toString(),anchorOdometer?.value,active)
