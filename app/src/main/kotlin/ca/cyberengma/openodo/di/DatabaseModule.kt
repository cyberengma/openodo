// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ca.cyberengma.openodo.data.*
import ca.cyberengma.openodo.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicles ADD COLUMN fuelType TEXT NOT NULL DEFAULT 'FUEL'")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS expense_line_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "expenseRecordId INTEGER NOT NULL, " +
                "typeId INTEGER NOT NULL, " +
                "costMinor INTEGER NOT NULL, " +
                "costCurrency TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS expense_records_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "vehicleId INTEGER NOT NULL, " +
                "category TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "odometerMetres INTEGER NOT NULL, " +
                "description TEXT, " +
                "performedBy TEXT NOT NULL, " +
                "shopName TEXT, " +
                "warrantyUntil TEXT, " +
                "receiptFileName TEXT, " +
                "notes TEXT, " +
                "createdAt INTEGER NOT NULL, " +
                "updatedAt INTEGER NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO expense_records_new (" +
                "id, vehicleId, category, date, odometerMetres, description, " +
                "performedBy, shopName, warrantyUntil, receiptFileName, notes, " +
                "createdAt, updatedAt) " +
                "SELECT id, vehicleId, 'SERVICE', date, odometerMetres, description, " +
                "performedBy, shopName, warrantyUntil, receiptFileName, notes, " +
                "createdAt, updatedAt FROM expense_records"
        )
        db.execSQL(
            "INSERT INTO expense_line_items (expenseRecordId, typeId, costMinor, costCurrency) " +
                "SELECT id, typeId, costMinor, costCurrency FROM expense_records"
        )
        db.execSQL("DROP TABLE expense_records")
        db.execSQL("ALTER TABLE expense_records_new RENAME TO expense_records")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): OpenOdoDatabase =
        Room.databaseBuilder(context, OpenOdoDatabase::class.java, "openodo.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun vehicleDao(db: OpenOdoDatabase): VehicleDao = db.vehicleDao()
    @Provides fun recordTypeDao(db: OpenOdoDatabase): RecordTypeDao = db.recordTypeDao()
    @Provides fun fuelEntryDao(db: OpenOdoDatabase): FuelEntryDao = db.fuelEntryDao()
    @Provides fun expenseRecordDao(db: OpenOdoDatabase): ExpenseRecordDao = db.expenseRecordDao()
    @Provides fun expenseLineItemDao(db: OpenOdoDatabase): ExpenseLineItemDao = db.expenseLineItemDao()
    @Provides fun reminderDao(db: OpenOdoDatabase): ReminderDao = db.reminderDao()

    @Provides fun vehicleRepository(dao: VehicleDao): VehicleRepository = RoomVehicleRepository(dao)
    @Provides fun recordTypeRepository(dao: RecordTypeDao): RecordTypeRepository = RoomRecordTypeRepository(dao)
    @Provides fun fuelEntryRepository(dao: FuelEntryDao): FuelEntryRepository = RoomFuelEntryRepository(dao)
    @Provides fun expenseRecordRepository(dao: ExpenseRecordDao, lineItems: ExpenseLineItemDao): ExpenseRecordRepository = RoomExpenseRecordRepository(dao, lineItems)
    @Provides fun reminderRepository(dao: ReminderDao): ReminderRepository = RoomReminderRepository(dao)
}
