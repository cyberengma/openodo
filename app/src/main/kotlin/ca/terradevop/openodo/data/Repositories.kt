// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.data

import ca.terradevop.openodo.core.model.*
import ca.terradevop.openodo.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface VehicleRepository {
    fun observeActive(): Flow<List<Vehicle>>
    fun observeArchived(): Flow<List<Vehicle>>
    suspend fun find(id: Long): Vehicle?
    suspend fun save(vehicle: Vehicle): Long
    suspend fun delete(vehicle: Vehicle)
}

class RoomVehicleRepository @Inject constructor(private val dao: VehicleDao) : VehicleRepository {
    override fun observeActive() = dao.observeActive().map { it.map(VehicleEntity::toCore) }
    override fun observeArchived() = dao.observeArchived().map { it.map(VehicleEntity::toCore) }
    override suspend fun find(id: Long) = dao.findById(id)?.toCore()
    override suspend fun save(vehicle: Vehicle): Long {
        val entity = vehicle.toEntity()
        if (entity.id == 0L) return dao.insert(entity)
        dao.update(entity)
        return entity.id
    }
    override suspend fun delete(vehicle: Vehicle) = dao.delete(vehicle.toEntity())
}

interface RecordTypeRepository {
    fun observeAll(): Flow<List<RecordType>>
    suspend fun save(type: RecordType): Long
}

class RoomRecordTypeRepository @Inject constructor(private val dao: RecordTypeDao) : RecordTypeRepository {
    override fun observeAll() = dao.observeAll().map { it.map(RecordTypeEntity::toCore) }
    override suspend fun save(type: RecordType): Long {
        val entity = type.toEntity()
        if (entity.id == 0L) return dao.insert(entity)
        dao.update(entity)
        return entity.id
    }
}

interface FuelEntryRepository {
    fun observeForVehicle(vehicleId: Long): Flow<List<FuelEntry>>
    suspend fun save(entry: FuelEntry): Long
    suspend fun delete(entry: FuelEntry)
}

class RoomFuelEntryRepository @Inject constructor(private val dao: FuelEntryDao) : FuelEntryRepository {
    override fun observeForVehicle(vehicleId: Long) = dao.observeForVehicle(vehicleId).map { it.map(FuelEntryEntity::toCore) }
    override suspend fun save(entry: FuelEntry): Long {
        val entity = entry.toEntity()
        if (entity.id == 0L) return dao.insert(entity)
        dao.update(entity)
        return entity.id
    }
    override suspend fun delete(entry: FuelEntry) = dao.delete(entry.toEntity())
}

interface ExpenseRecordRepository {
    fun observeForVehicle(vehicleId: Long): Flow<List<ExpenseRecord>>
    suspend fun findForVehicleAndType(vehicleId: Long, typeId: Long): List<ExpenseRecord>
    suspend fun save(record: ExpenseRecord): Long
    suspend fun delete(record: ExpenseRecord)
}

class RoomExpenseRecordRepository @Inject constructor(private val dao: ExpenseRecordDao) : ExpenseRecordRepository {
    override fun observeForVehicle(vehicleId: Long) = dao.observeForVehicle(vehicleId).map { it.map(ExpenseRecordEntity::toCore) }
    override suspend fun findForVehicleAndType(vehicleId: Long, typeId: Long) = dao.findForVehicleAndType(vehicleId,typeId).map(ExpenseRecordEntity::toCore)
    override suspend fun save(record: ExpenseRecord): Long {
        val entity = record.toEntity()
        if (entity.id == 0L) return dao.insert(entity)
        dao.update(entity)
        return entity.id
    }
    override suspend fun delete(record: ExpenseRecord) = dao.delete(record.toEntity())
}

interface ReminderRepository {
    fun observeForVehicle(vehicleId: Long): Flow<List<Reminder>>
    fun observeActiveForVehicle(vehicleId: Long): Flow<List<Reminder>>
    suspend fun save(reminder: Reminder): Long
    suspend fun delete(reminder: Reminder)
}

class RoomReminderRepository @Inject constructor(private val dao: ReminderDao) : ReminderRepository {
    override fun observeForVehicle(vehicleId: Long) = dao.observeForVehicle(vehicleId).map { it.map(ReminderEntity::toCore) }
    override fun observeActiveForVehicle(vehicleId: Long) = dao.observeActiveForVehicle(vehicleId).map { it.map(ReminderEntity::toCore) }
    override suspend fun save(reminder: Reminder): Long {
        val entity = reminder.toEntity()
        if (entity.id == 0L) return dao.insert(entity)
        dao.update(entity)
        return entity.id
    }
    override suspend fun delete(reminder: Reminder) = dao.delete(reminder.toEntity())
}
