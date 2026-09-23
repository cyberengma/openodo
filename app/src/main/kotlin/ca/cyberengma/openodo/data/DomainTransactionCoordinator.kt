// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.data

import androidx.room.withTransaction
import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.Vehicle
import ca.cyberengma.openodo.core.portability.ImportResult
import ca.cyberengma.openodo.core.portability.PortabilityDomain
import ca.cyberengma.openodo.core.reminders.ResetResolver
import ca.cyberengma.openodo.data.local.ExpenseLineItemDao
import ca.cyberengma.openodo.data.local.ExpenseLineItemEntity
import ca.cyberengma.openodo.data.local.ExpenseRecordDao
import ca.cyberengma.openodo.data.local.FuelEntryDao
import ca.cyberengma.openodo.data.local.OpenOdoDatabase
import ca.cyberengma.openodo.data.local.RecordTypeDao
import ca.cyberengma.openodo.data.local.ReminderDao
import ca.cyberengma.openodo.data.local.VehicleDao
import ca.cyberengma.openodo.data.local.toCore
import ca.cyberengma.openodo.data.local.toEntity
import javax.inject.Inject

class DomainTransactionCoordinator @Inject constructor(
    private val database: OpenOdoDatabase,
    private val vehicles: VehicleDao,
    private val recordTypes: RecordTypeDao,
    private val fuels: FuelEntryDao,
    private val expenses: ExpenseRecordDao,
    private val lineItems: ExpenseLineItemDao,
    private val reminders: ReminderDao,
) {
    suspend fun replaceAll(domain: PortabilityDomain) = database.withTransaction {
        reminders.deleteAll()
        expenses.deleteAll()
        lineItems.deleteAll()
        fuels.deleteAll()
        recordTypes.deleteAll()
        vehicles.deleteAll()
        domain.vehicles.forEach { vehicles.insert(it.toEntity()) }
        domain.recordTypes.forEach { recordTypes.insert(it.toEntity()) }
        domain.fuelEntries.forEach { fuels.insert(it.toEntity()) }
        domain.expenseRecords.forEach { record ->
            val id = expenses.insert(record.copy(id = 0).toEntity())
            lineItems.insertAll(record.lineItems.map { it.toEntity(id) })
        }
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
            val remapped = record.lineItems.map { li -> li.copy(typeId = typeIds[li.typeId] ?: li.typeId) }
            val id = expenses.insert(record.copy(id = 0, vehicleId = newVehicleId, lineItems = remapped).toEntity())
            lineItems.insertAll(remapped.map { it.toEntity(id) })
        }
        newVehicleId
    }

    suspend fun saveExpense(record: ExpenseRecord): Long = database.withTransaction {
        val entity = record.toEntity()
        val previous = entity.id.takeIf { it != 0L }?.let { findExpenseWithItems(it) }
        val id = if (entity.id == 0L) expenses.insert(entity) else {
            expenses.update(entity)
            entity.id
        }
        lineItems.deleteForRecord(id)
        lineItems.insertAll(record.lineItems.map { it.toEntity(id) })
        val saved = record.copy(id = id)
        var current = reminders.findForVehicle(record.vehicleId).map { it.toCore() }
        if (previous != null) {
            val remaining = expensesForVehicle(record.vehicleId).filter { it.id != id }
            current = ResetResolver.onRecordDeleted(current, previous, remaining)
        }
        ResetResolver.onRecordLogged(current, saved).forEach { reminders.update(it.toEntity()) }
        id
    }

    suspend fun deleteExpense(record: ExpenseRecord) = database.withTransaction {
        lineItems.deleteForRecord(record.id)
        expenses.delete(record.toEntity())
        val remaining = expensesForVehicle(record.vehicleId)
        val current = reminders.findForVehicle(record.vehicleId).map { it.toCore() }
        ResetResolver.onRecordDeleted(current, record, remaining).forEach { reminders.update(it.toEntity()) }
    }

    suspend fun deleteVehicle(vehicle: Vehicle) = database.withTransaction {
        reminders.deleteForVehicle(vehicle.id)
        lineItems.deleteForVehicle(vehicle.id)
        expenses.deleteForVehicle(vehicle.id)
        fuels.deleteForVehicle(vehicle.id)
        vehicles.delete(vehicle.toEntity())
    }

    private suspend fun expensesForVehicle(vehicleId: Long): List<ExpenseRecord> {
        val entities = expenses.findForVehicle(vehicleId)
        val ids = entities.map { it.id }
        val items = if (ids.isEmpty()) emptyList() else lineItems.findForRecords(ids)
        val byRecord = items.groupBy { it.expenseRecordId }
        return entities.map { entity ->
            entity.toCore().copy(lineItems = byRecord[entity.id].orEmpty().map(ExpenseLineItemEntity::toCore))
        }
    }

    private suspend fun findExpenseWithItems(id: Long): ExpenseRecord? {
        val entity = expenses.findById(id) ?: return null
        val items = lineItems.findForRecord(id)
        return entity.toCore().copy(lineItems = items.map(ExpenseLineItemEntity::toCore))
    }
}
