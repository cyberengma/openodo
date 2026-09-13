// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.di

import android.content.Context
import androidx.room.Room
import ca.terradevop.openodo.data.*
import ca.terradevop.openodo.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): OpenOdoDatabase =
        Room.databaseBuilder(context, OpenOdoDatabase::class.java, "openodo.db").build()

    @Provides fun vehicleDao(db: OpenOdoDatabase): VehicleDao = db.vehicleDao()
    @Provides fun recordTypeDao(db: OpenOdoDatabase): RecordTypeDao = db.recordTypeDao()
    @Provides fun fuelEntryDao(db: OpenOdoDatabase): FuelEntryDao = db.fuelEntryDao()
    @Provides fun expenseRecordDao(db: OpenOdoDatabase): ExpenseRecordDao = db.expenseRecordDao()
    @Provides fun reminderDao(db: OpenOdoDatabase): ReminderDao = db.reminderDao()

    @Provides fun vehicleRepository(dao: VehicleDao): VehicleRepository = RoomVehicleRepository(dao)
    @Provides fun recordTypeRepository(dao: RecordTypeDao): RecordTypeRepository = RoomRecordTypeRepository(dao)
    @Provides fun fuelEntryRepository(dao: FuelEntryDao): FuelEntryRepository = RoomFuelEntryRepository(dao)
    @Provides fun expenseRecordRepository(dao: ExpenseRecordDao): ExpenseRecordRepository = RoomExpenseRecordRepository(dao)
    @Provides fun reminderRepository(dao: ReminderDao): ReminderRepository = RoomReminderRepository(dao)
}
