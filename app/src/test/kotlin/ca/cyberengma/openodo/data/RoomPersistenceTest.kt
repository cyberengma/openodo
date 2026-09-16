// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.data

import android.content.Context
import androidx.room.Room
import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.core.units.EnergyUnit
import ca.cyberengma.openodo.core.units.VolumeUnit
import ca.cyberengma.openodo.data.local.OpenOdoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class RoomPersistenceTest {
    private lateinit var db: OpenOdoDatabase
    private lateinit var repository: RoomVehicleRepository

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, OpenOdoDatabase::class.java).allowMainThreadQueries().build()
        repository = RoomVehicleRepository(db.vehicleDao())
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) db.close()
    }

    @Test
    fun `vehicle mapping preserves every field and vehicle scoped settings`() = runBlocking {
        val original = vehicle(distance = DistanceUnit.MILES, volume = VolumeUnit.US_GALLONS, currency = "USD")
        val id = repository.save(original)
        val loaded = repository.find(id)!!
        assertEquals(original.copy(id = id), loaded)
        assertEquals(DistanceUnit.MILES, loaded.distanceUnit)
        assertEquals(VolumeUnit.US_GALLONS, loaded.volumeUnit)
        assertEquals("USD", loaded.currency)
    }

    @Test
    fun `active and archived queries are explicit`() = runBlocking {
        repository.save(vehicle(name = "Active"))
        repository.save(vehicle(name = "Archived", archived = true))
        assertEquals(listOf("Active"), repository.observeActive().first().map { it.name })
        assertEquals(listOf("Archived"), repository.observeArchived().first().map { it.name })
        assertEquals(listOf("Active", "Archived"), db.vehicleDao().observeAll().first().map { it.name })
    }

    @Test
    fun `updating settings does not mutate canonical odometer`() = runBlocking {
        val id = repository.save(vehicle())
        val loaded = repository.find(id)!!
        repository.save(loaded.copy(distanceUnit = DistanceUnit.MILES, volumeUnit = VolumeUnit.UK_GALLONS, currency = "GBP"))
        val updated = repository.find(id)!!
        assertEquals(12_345L, updated.manualOdometer!!.value)
        assertEquals(DistanceUnit.MILES, updated.distanceUnit)
        assertEquals(VolumeUnit.UK_GALLONS, updated.volumeUnit)
        assertEquals("GBP", updated.currency)
    }

    @Test
    fun `delete is explicit and does not archive implicitly`() = runBlocking {
        val id = repository.save(vehicle())
        val loaded = repository.find(id)!!
        repository.delete(loaded)
        assertTrue(repository.find(id) == null)
    }
}
