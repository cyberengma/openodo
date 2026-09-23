// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE isArchived = 0 ORDER BY name")
    fun observeActive(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isArchived = 1 ORDER BY name")
    fun observeArchived(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY isArchived, name")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun findById(id: Long): VehicleEntity?

    @Insert
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Delete
    suspend fun delete(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles")
    suspend fun deleteAll()
}

@Dao
interface RecordTypeDao {
    @Query("SELECT * FROM record_types ORDER BY category, name")
    fun observeAll(): Flow<List<RecordTypeEntity>>

    @Query("SELECT * FROM record_types WHERE id = :id")
    suspend fun findById(id: Long): RecordTypeEntity?

    @Insert
    suspend fun insert(type: RecordTypeEntity): Long

    @Update
    suspend fun update(type: RecordTypeEntity)

    @Query("DELETE FROM record_types")
    suspend fun deleteAll()
}

@Dao
interface FuelEntryDao {
    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY date DESC, odometerMetres DESC, id DESC")
    fun observeForVehicle(vehicleId: Long): Flow<List<FuelEntryEntity>>

    @Query("SELECT * FROM fuel_entries WHERE id = :id")
    suspend fun findById(id: Long): FuelEntryEntity?

    @Insert
    suspend fun insert(entry: FuelEntryEntity): Long

    @Update
    suspend fun update(entry: FuelEntryEntity)

    @Delete
    suspend fun delete(entry: FuelEntryEntity)

    @Query("DELETE FROM fuel_entries WHERE vehicleId = :vehicleId")
    suspend fun deleteForVehicle(vehicleId: Long)

    @Query("DELETE FROM fuel_entries")
    suspend fun deleteAll()
}

@Dao
interface ExpenseRecordDao {
    @Query("SELECT * FROM expense_records WHERE vehicleId = :vehicleId ORDER BY date DESC, odometerMetres DESC, id DESC")
    fun observeForVehicle(vehicleId: Long): Flow<List<ExpenseRecordEntity>>

    @Query("SELECT * FROM expense_records WHERE id = :id")
    suspend fun findById(id: Long): ExpenseRecordEntity?

    @Query("SELECT * FROM expense_records WHERE vehicleId = :vehicleId")
    suspend fun findForVehicle(vehicleId: Long): List<ExpenseRecordEntity>

    @Insert
    suspend fun insert(record: ExpenseRecordEntity): Long

    @Update
    suspend fun update(record: ExpenseRecordEntity)

    @Delete
    suspend fun delete(record: ExpenseRecordEntity)

    @Query("DELETE FROM expense_records WHERE vehicleId = :vehicleId")
    suspend fun deleteForVehicle(vehicleId: Long)

    @Query("DELETE FROM expense_records")
    suspend fun deleteAll()
}

@Dao
interface ExpenseLineItemDao {
    @Insert
    suspend fun insert(item: ExpenseLineItemEntity): Long

    @Insert
    suspend fun insertAll(items: List<ExpenseLineItemEntity>)

    @Query("SELECT li.* FROM expense_line_items li INNER JOIN expense_records er ON li.expenseRecordId = er.id WHERE er.vehicleId = :vehicleId")
    fun observeForVehicle(vehicleId: Long): Flow<List<ExpenseLineItemEntity>>

    @Query("SELECT * FROM expense_line_items WHERE expenseRecordId = :expenseRecordId ORDER BY id")
    suspend fun findForRecord(expenseRecordId: Long): List<ExpenseLineItemEntity>

    @Query("SELECT * FROM expense_line_items WHERE expenseRecordId IN (:expenseRecordIds)")
    suspend fun findForRecords(expenseRecordIds: List<Long>): List<ExpenseLineItemEntity>

    @Query("SELECT * FROM expense_line_items WHERE expenseRecordId IN (:expenseRecordIds) AND typeId = :typeId ORDER BY id")
    suspend fun findForRecordsAndType(expenseRecordIds: List<Long>, typeId: Long): List<ExpenseLineItemEntity>

    @Query("DELETE FROM expense_line_items WHERE expenseRecordId = :expenseRecordId")
    suspend fun deleteForRecord(expenseRecordId: Long)

    @Query("DELETE FROM expense_line_items WHERE expenseRecordId IN (SELECT id FROM expense_records WHERE vehicleId = :vehicleId)")
    suspend fun deleteForVehicle(vehicleId: Long)

    @Query("DELETE FROM expense_line_items")
    suspend fun deleteAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId ORDER BY id")
    fun observeForVehicle(vehicleId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId AND active = 1 ORDER BY id")
    fun observeActiveForVehicle(vehicleId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE vehicleId = :vehicleId ORDER BY id")
    suspend fun findForVehicle(vehicleId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun findById(id: Long): ReminderEntity?

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE vehicleId = :vehicleId")
    suspend fun deleteForVehicle(vehicleId: Long)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}
