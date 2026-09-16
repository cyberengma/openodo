// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.portability

import ca.cyberengma.openodo.core.portability.importer.DrivvoImporter
import ca.cyberengma.openodo.core.portability.importer.FuelioImporter
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FixtureImporterTest {
    private fun read(name: String) = Files.readString(Path.of("../fixtures/imports", name))

    @Test fun `fuelio legacy fixture imports two rows`() {
        val result = FuelioImporter.import(read("fuelio-sample.csv"))
        assertEquals(1, result.domain.vehicles.size)
        assertEquals(2, result.domain.fuelEntries.size)
        assertEquals(424_000L, result.domain.fuelEntries.first().odometer.value)
        assertEquals(33_040L, result.domain.fuelEntries.first().volume!!.value)
        assertEquals(5_000L, result.domain.fuelEntries.last().odometer.value)
    }

    @Test fun `drivvo fresh fixture imports vehicle and refueling rows`() {
        val result = DrivvoImporter.import(read("drivvo-sample.csv"))
        assertEquals(1, result.domain.vehicles.size)
        assertTrue("imported=${result.domain.fuelEntries.size} report=${result.report.size}", result.domain.fuelEntries.size > 150)
        assertEquals(40_457_000L, result.domain.fuelEntries.first().odometer.value)
        assertEquals(16_056L, result.domain.fuelEntries.first().volume!!.value)
        assertEquals("Total Hadath", result.domain.fuelEntries.first().stationName)
        assertTrue(result.domain.fuelEntries.none { it.stationName == "Enigma" })
        assertTrue(result.report.any { it.code == "DRIVVO_MISSED_DEFAULT_FALSE" })
    }

    @Test fun `drivvo imports expense and service records grouped by type`() {
        val result = DrivvoImporter.import(read("drivvo-sample.csv"))
        assertTrue("expenses=${result.domain.expenseRecords.size}", result.domain.expenseRecords.isNotEmpty())
        assertTrue("types=${result.domain.recordTypes.size}", result.domain.recordTypes.isNotEmpty())
        assertTrue(result.domain.expenseRecords.all { it.typeId != 0L })
    }

    @Test fun `invalid importer rows are skipped`() {
        val source = """## Vehicle\n\"Name\",\"Description\",\"DistUnit\",\"FuelUnit\",\"ConsumptionUnit\",\"ImportCSVDateFormat\",\"VIN\",\"Insurance\",\"Plate\",\"Make\",\"Model\",\"Year\",\"TankCount\",\"Tank1Type\",\"Tank2Type\",\"Active\",\"Tank1Capacity\",\"Tank2Capacity\"\n\"Car\",\"\",\"0\",\"0\",\"0\",\"yyyy-MM-dd\",\"\",\"\",\"\",\"Make\",\"Model\",\"2020\",\"1\",\"100\",\"0\",\"1\",\"50\",\"0\"\n## Log\n\"Data\",\"Odo (km)\",\"Fuel (litres)\",\"Full\",\"Price (optional)\",\"l/100km (optional)\",\"latitude (optional)\",\"longitude (optional)\",\"City (optional)\",\"Notes (optional)\",\"Missed\",\"TankNumber\",\"FuelType\",\"VolumePrice\",\"StationID (optional)\",\"ExcludeDistance\",\"UniqueId\",\"TankCalc\"\n\"not-a-date\",\"bad\",\"1\",\"1\",\"0\",\"0\",\"\",\"\",\"\",\"\",\"0\",\"1\",\"0\",\"0\",\"0\",\"0\",\"1\",\"0\"\n"""
        val result = FuelioImporter.import(source)
        assertTrue(result.domain.fuelEntries.isEmpty())
        assertTrue(result.report.all { it.level == ImportLevel.SKIP })
    }
}
