// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.data

import androidx.room.withTransaction
import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.Vehicle
import ca.terradevop.openodo.core.portability.ImportResult
import ca.terradevop.openodo.core.portability.PortabilityDomain
import ca.terradevop.openodo.core.reminders.ResetResolver
import ca.terradevop.openodo.data.local.ExpenseRecordDao
import ca.terradevop.openodo.data.local.FuelEntryDao
import ca.terradevop.openodo.data.local.OpenOdoDatabase
import ca.terradevop.openodo.data.local.RecordTypeDao
import ca.terradevop.openodo.data.local.ReminderDao
import ca.terradevop.openodo.data.local.VehicleDao
import ca.terradevop.openodo.data.local.toCore
import ca.terradevop.openodo.data.local.toEntity
import javax.inject.Inject

class DomainTransactionCoordinator @Inject constructor(
    private val database: OpenOdoDatabase,
    private val vehicles: VehicleDao,
    private val recordTypes: RecordTypeDao,
    private val fuels: FuelEntryDao,
    private val expenses: ExpenseRecordDao,
    private val reminders: ReminderDao,
) {
    suspend fun replaceAll(domain: PortabilityDomain) = database.withTransaction {
        reminders.deleteAll()
        expenses.deleteAll()
        fuels.deleteAll()
        recordTypes.deleteAll()
        vehicles.deleteAll()
        domain.vehicles.forEach { vehicles.insert(it.toEntity()) }
        domain.recordTypes.forEach { recordTypes.insert(it.toEntity()) }
        domain.fuelEntries.forEach { fuels.insert(it.toEntity()) }
        domain.expenseRecords.forEach { expenses.insert(it.toEntity()) }
        domain.reminders.forEach { reminders.insert(it.toEntity()) }
    }

    suspend fun applyImport(result: ImportResult): Long = database.withTransaction {
        val sourceVehicle = requireNotNull(result.domain.vehicles.singleOrNull()) {
            "Import must contain exactly one vehicle"
        }
        val newVehicleId = vehicles.insert(sourceVehicle.copy(id = 0).toEntity())
        val typeIds = result.domain.recordTypes.associate { type ->
            type.id to recordTypes.insert(type.copy(id = 0).toEntity())
        }
        result.domain.fuelEntries.forEach { entry ->
            fuels.insert(entry.copy(id = 0, vehicleId = newVehicleId).toEntity())
        }
        result.domain.expenseRecords.forEach { record ->
            expenses.insert(
                record.copy(
                    id = 0,
                    vehicleId = newVehicleId,
                    typeId = typeIds[record.typeId] ?: record.typeId,
                ).toEntity(),
            )
        }
        newVehicleId
    }

    suspend fun saveExpense(record: ExpenseRecord): Long = database.withTransaction {
        val entity = record.toEntity()
        val previous = entity.id.takeIf { it != 0L }?.let { expenses.findById(it)?.toCore() }
        val id = if (entity.id == 0L) expenses.insert(entity) else {
            expenses.update(entity)
            entity.id
        }
        val saved = record.copy(id = id)
        var current = reminders.findForVehicle(record.vehicleId).map { it.toCore() }
        if (previous != null) {
            val remaining = expenses.findForVehicleAndType(previous.vehicleId, previous.typeId).map { it.toCore() }
            current = ResetResolver.onRecordDeleted(current, previous, remaining)
        }
        ResetResolver.onRecordLogged(current, saved).forEach { reminders.update(it.toEntity()) }
        id
    }

    suspend fun deleteExpense(record: ExpenseRecord) = database.withTransaction {
        expenses.delete(record.toEntity())
        val remaining = expenses.findForVehicleAndType(record.vehicleId, record.typeId).map { it.toCore() }
        val current = reminders.findForVehicle(record.vehicleId).map { it.toCore() }
        ResetResolver.onRecordDeleted(current, record, remaining).forEach { reminders.update(it.toEntity()) }
    }

    suspend fun deleteVehicle(vehicle: Vehicle) = database.withTransaction {
        reminders.deleteForVehicle(vehicle.id)
        expenses.deleteForVehicle(vehicle.id)
        fuels.deleteForVehicle(vehicle.id)
        vehicles.delete(vehicle.toEntity())
    }
}
