// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.data

import androidx.room.Room
import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.PerformedBy
import ca.terradevop.openodo.core.model.RecordCategory
import ca.terradevop.openodo.core.model.RecordType
import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.portability.PortabilityDomain
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.data.local.OpenOdoDatabase
import ca.terradevop.openodo.data.local.ReminderEntity
import ca.terradevop.openodo.data.local.toEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DomainTransactionCoordinatorTest {
    private lateinit var db: OpenOdoDatabase
    private lateinit var coordinator: DomainTransactionCoordinator

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), OpenOdoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        coordinator = DomainTransactionCoordinator(
            db,
            db.vehicleDao(),
            db.recordTypeDao(),
            db.fuelEntryDao(),
            db.expenseRecordDao(),
            db.reminderDao(),
        )
    }

    @After fun tearDown() = db.close()

    @Test fun `saving matching expense resets reminder atomically`() = runBlocking {
        val vehicleId = db.vehicleDao().insert(vehicle().toEntity())
        db.reminderDao().insert(ReminderEntity(1, vehicleId, 7, 10_000, 6, null, null, true))
        coordinator.saveExpense(expense(vehicleId, 7, LocalDate.of(2026, 1, 3), 50_000))
        val reminder = db.reminderDao().findById(1)!!
        assertEquals("2026-01-03", reminder.anchorDate)
        assertEquals(50_000L, reminder.anchorOdometerMetres)
    }

    @Test fun `deleting anchoring expense clears reminder when no record remains`() = runBlocking {
        val vehicleId = db.vehicleDao().insert(vehicle().toEntity())
        val record = expense(vehicleId, 7, LocalDate.of(2026, 1, 3), 50_000)
        val recordId = coordinator.saveExpense(record)
        db.reminderDao().insert(ReminderEntity(1, vehicleId, 7, 10_000, 6, "2026-01-03", 50_000, true))
        coordinator.deleteExpense(record.copy(id = recordId))
        val reminder = db.reminderDao().findById(1)!!
        assertNull(reminder.anchorDate)
        assertNull(reminder.anchorOdometerMetres)
    }

    @Test fun `permanent vehicle delete cascades local history`() = runBlocking {
        val source = vehicle()
        val vehicleId = db.vehicleDao().insert(source.toEntity())
        db.expenseRecordDao().insert(expense(vehicleId, 7, LocalDate.of(2026, 1, 3), 50_000).toEntity())
        db.reminderDao().insert(ReminderEntity(1, vehicleId, 7, 10_000, 6, null, null, true))
        coordinator.deleteVehicle(source.copy(id = vehicleId))
        assertEquals(emptyList<ExpenseRecord>(), db.expenseRecordDao().observeForVehicle(vehicleId).first())
        assertEquals(emptyList<ReminderEntity>(), db.reminderDao().findForVehicle(vehicleId))
        assertNull(db.vehicleDao().findById(vehicleId))
    }

    @Test fun `failed replace rolls back original database`() = runBlocking {
        val originalId = db.vehicleDao().insert(vehicle(name = "Original").toEntity())
        val duplicate = vehicle(id = 99, name = "Duplicate")
        assertThrows(Exception::class.java) {
            runBlocking { coordinator.replaceAll(PortabilityDomain(vehicles = listOf(duplicate, duplicate))) }
        }
        assertEquals("Original", db.vehicleDao().findById(originalId)?.name)
    }

    private fun expense(vehicleId: Long, typeId: Long, date: LocalDate, odometer: Long) = ExpenseRecord(
        vehicleId = vehicleId,
        typeId = typeId,
        date = date,
        odometer = Metres(odometer),
        title = "Service",
        description = null,
        cost = Money(1_000, "CAD"),
        performedBy = PerformedBy.SELF,
        shopName = null,
        warrantyUntil = null,
        receiptFileName = null,
        notes = null,
        createdAt = 1,
        updatedAt = 1,
    )
}
