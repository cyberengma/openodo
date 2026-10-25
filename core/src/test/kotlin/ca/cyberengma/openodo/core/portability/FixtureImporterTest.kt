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
        assertEquals(35_500L, result.domain.fuelEntries.first().unitPrice!!.milli)
        assertEquals(117_292L, result.domain.fuelEntries.first().totalCost.minor)
        assertTrue(result.domain.fuelEntries.all { it.fuelLabel.isEmpty() })
    }

    @Test fun `fuelio datetime rows parse to local dates`() {
        val source = header() + "\"2024-05-10 14:30:00\",\"100\",\"10\",\"1\",\"40.0\",\"0\",\"\",\"\",\"\",\"\",\"0\",\"1\",\"0\",\"4.0\",\"0\",\"0\",\"1\",\"0\"\n" +
            "\"2024-05-11T09:00\",\"120\",\"10\",\"1\",\"40.0\",\"0\",\"\",\"\",\"\",\"\",\"0\",\"1\",\"0\",\"4.0\",\"0\",\"0\",\"2\",\"0\"\n"
        val result = FuelioImporter.import(source)
        assertEquals(2, result.domain.fuelEntries.size)
        assertEquals(java.time.LocalDate.of(2024, 5, 10), result.domain.fuelEntries[0].date)
        assertEquals(java.time.LocalDate.of(2024, 5, 11), result.domain.fuelEntries[1].date)
    }

    @Test fun `fuelio decimal odometer imports roundtrip`() {
        val source = header() + "\"2026-10-26 00:06\",\"42650.0\",\"20.0\",\"1\",\"31.6\",,\"0.0\",\"0.0\",,,\"0\",\"1\",\"111\",\"1.58\",\"0\",\"0.0\",\"1\",\"0.0\",,\"guid1\",\"1792965988778\"\n"
        val result = FuelioImporter.import(source)
        assertEquals(1, result.domain.fuelEntries.size)
        val entry = result.domain.fuelEntries.first()
        assertEquals(java.time.LocalDate.of(2026, 10, 26), entry.date)
        assertEquals(42_650_000L, entry.odometer.value)
        assertEquals(20_000L, entry.volume!!.value)
        assertEquals(3_160L, entry.totalCost.minor)
        assertEquals(1_580L, entry.unitPrice!!.milli)
        assertTrue(entry.fuelLabel.isEmpty())
    }

    private fun header() = "\"## Vehicle\"\n\"Name\",\"Description\",\"DistUnit\",\"FuelUnit\",\"ConsumptionUnit\",\"ImportCSVDateFormat\",\"VIN\",\"Insurance\",\"Plate\",\"Make\",\"Model\",\"Year\",\"TankCount\",\"Tank1Type\",\"Tank2Type\",\"Active\",\"Tank1Capacity\",\"Tank2Capacity\",\"FuelUnitTank2\",\"FuelConsumptionTank2\",\"guid\",\"lastupdated\"\n\"My Car\",\"Description\",\"0\",\"0\",\"0\",\"yyyy-MM-dd\",\"\",\"\",\"\",\"Honda\",\"CRF1100L\",\"2021\",\"1\",\"100\",\"0\",\"1\",\"20.0\",\"0.0\",\"0\",\"0\",\"guid0\",\"1792965958048\"\n\"## Log\"\n\"Data\",\"Odo (km)\",\"Fuel (litres)\",\"Full\",\"Price (optional)\",\"l/100km (optional)\",\"latitude (optional)\",\"longitude (optional)\",\"City (optional)\",\"Notes (optional)\",\"Missed\",\"TankNumber\",\"FuelType\",\"VolumePrice\",\"StationID (optional)\",\"ExcludeDistance\",\"UniqueId\",\"TankCalc\",\"Weather\",\"guid\",\"lastupdated\"\n"

    @Test fun `drivvo fresh fixture imports vehicle and refueling rows`() {
        val result = DrivvoImporter.import(read("drivvo-sample.csv"))
        assertEquals(1, result.domain.vehicles.size)
        assertEquals("Family Sedan", result.domain.vehicles.first().name)
        assertEquals(11, result.domain.fuelEntries.size)
        assertEquals(12_000_000L, result.domain.fuelEntries.first().odometer.value)
        assertEquals(12_500L, result.domain.fuelEntries.first().volume!!.value)
        assertEquals("Shell", result.domain.fuelEntries.first().stationName)
        assertTrue(result.domain.fuelEntries.any { it.stationName == "Costco" })
        assertTrue(result.report.any { it.code == "DRIVVO_MISSED_DEFAULT_FALSE" })
    }

    @Test fun `drivvo imports expense and service records grouped by type`() {
        val result = DrivvoImporter.import(read("drivvo-sample.csv"))
        assertTrue("expenses=${result.domain.expenseRecords.size}", result.domain.expenseRecords.isNotEmpty())
        assertTrue("types=${result.domain.recordTypes.size}", result.domain.recordTypes.isNotEmpty())
        assertTrue(result.domain.expenseRecords.all { it.lineItems.isNotEmpty() && it.lineItems.all { li -> li.typeId != 0L } })
    }

    @Test fun `invalid importer rows are skipped`() {
        val source = """## Vehicle\n\"Name\",\"Description\",\"DistUnit\",\"FuelUnit\",\"ConsumptionUnit\",\"ImportCSVDateFormat\",\"VIN\",\"Insurance\",\"Plate\",\"Make\",\"Model\",\"Year\",\"TankCount\",\"Tank1Type\",\"Tank2Type\",\"Active\",\"Tank1Capacity\",\"Tank2Capacity\"\n\"Car\",\"\",\"0\",\"0\",\"0\",\"yyyy-MM-dd\",\"\",\"\",\"\",\"Make\",\"Model\",\"2020\",\"1\",\"100\",\"0\",\"1\",\"50\",\"0\"\n## Log\n\"Data\",\"Odo (km)\",\"Fuel (litres)\",\"Full\",\"Price (optional)\",\"l/100km (optional)\",\"latitude (optional)\",\"longitude (optional)\",\"City (optional)\",\"Notes (optional)\",\"Missed\",\"TankNumber\",\"FuelType\",\"VolumePrice\",\"StationID (optional)\",\"ExcludeDistance\",\"UniqueId\",\"TankCalc\"\n\"not-a-date\",\"bad\",\"1\",\"1\",\"0\",\"0\",\"\",\"\",\"\",\"\",\"0\",\"1\",\"0\",\"0\",\"0\",\"0\",\"1\",\"0\"\n"""
        val result = FuelioImporter.import(source)
        assertTrue(result.domain.fuelEntries.isEmpty())
        assertTrue(result.report.all { it.level == ImportLevel.SKIP })
    }
}
