// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.data

import android.content.Context
import androidx.room.Room
import ca.cyberengma.openodo.data.local.OpenOdoDatabase
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class RepositoryConstructionTest {
    @Test
    fun `database and repository construct through application context`() {
        val context = RuntimeEnvironment.getApplication()
        val database = Room.inMemoryDatabaseBuilder(context, OpenOdoDatabase::class.java).build()
        assertNotNull(database.vehicleDao())
        assertNotNull(RoomVehicleRepository(database.vehicleDao()))
        database.close()
    }
}
