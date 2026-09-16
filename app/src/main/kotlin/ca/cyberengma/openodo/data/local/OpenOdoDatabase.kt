// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        VehicleEntity::class,
        RecordTypeEntity::class,
        FuelEntryEntity::class,
        ExpenseRecordEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OpenOdoDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun recordTypeDao(): RecordTypeDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
    abstract fun reminderDao(): ReminderDao
}
