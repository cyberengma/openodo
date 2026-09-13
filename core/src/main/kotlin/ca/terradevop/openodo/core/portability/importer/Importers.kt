// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.portability.importer

import ca.terradevop.openodo.core.model.*
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.UnitPrice
import ca.terradevop.openodo.core.portability.*
import ca.terradevop.openodo.core.portability.csv.Rfc4180
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val drivvoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

object FuelioImporter {
    fun import(source: String): ImportResult {
        val rows = Rfc4180.read(StringReader(source)).toList()
        val vehicleRow = rows.getOrNull(2) ?: return ImportResult(PortabilityDomain(), listOf(ImportNote(1, ImportLevel.SKIP, "MISSING_VEHICLE", "vehicle section missing")))
        val vehicle = Vehicle(1, vehicleRow[0], vehicleRow.getOrElse(9) { "" }, vehicleRow.getOrElse(10) { "" }, vehicleRow.getOrElse(11) { "0" }.toIntOrNull() ?: 0, null, null, ca.terradevop.openodo.core.units.DistanceUnit.KILOMETRES, ca.terradevop.openodo.core.units.VolumeUnit.LITRES, ca.terradevop.openodo.core.units.EnergyUnit.KILOWATT_HOURS, "USD", null, null, false, vehicleRow.getOrNull(1)?.ifBlank { null }, 0, 0)
        val entries = mutableListOf<FuelEntry>()
        val notes = mutableListOf<ImportNote>()
        rows.drop(5).forEachIndexed { index, row ->
            val line = index + 6
            if (row.size < 18) return@forEachIndexed
            val date = runCatching { LocalDate.parse(row[0]) }.getOrNull()
            val odo = row[1].toLongOrNull()
            if (date == null) { notes += ImportNote(line, ImportLevel.SKIP, "INVALID_DATE", "date is not ISO yyyy-MM-dd"); return@forEachIndexed }
            if (odo == null) { notes += ImportNote(line, ImportLevel.SKIP, "INVALID_ODOMETER", "odometer is not numeric"); return@forEachIndexed }
            val litres = row[2].toBigDecimalOrNull()
            val price = row[4].toBigDecimalOrNull()
            if (litres == null) { notes += ImportNote(line, ImportLevel.SKIP, "INVALID_VOLUME", "fuel volume is not numeric"); return@forEachIndexed }
            entries += FuelEntry(entries.size + 1L, 1, date, Metres(odo * 1_000), FuelKind.LIQUID, Millilitres.litres(litres), null, price?.let { UnitPrice((it * java.math.BigDecimal(1_000)).toLong(), "USD") }, Money((row[4].toBigDecimalOrNull()?.multiply(java.math.BigDecimal(100))?.toLong() ?: 0L), "USD"), row[12], row[3] == "1", row[10] == "1", row[8].ifBlank { null }, null, row[9].ifBlank { null }, 0, 0)
        }
        return ImportResult(PortabilityDomain(vehicles = listOf(vehicle), fuelEntries = entries), notes)
    }
}

object DrivvoImporter {
    fun import(source: String): ImportResult {
        val rows = Rfc4180.read(StringReader(source)).toList()
        val notes = mutableListOf<ImportNote>()
        val vehicleHeader = rows.indexOfFirst { it.firstOrNull() == "Vehicle Name" && it.getOrNull(1) == "Active" }
        val vehicleRow = rows.getOrNull(vehicleHeader + 1)
        val vehicle = vehicleRow?.let { Vehicle(1,it[0],it[3],it[4],it[6].toIntOrNull() ?: 0,null,null,ca.terradevop.openodo.core.units.DistanceUnit.KILOMETRES,ca.terradevop.openodo.core.units.VolumeUnit.LITRES,ca.terradevop.openodo.core.units.EnergyUnit.KILOWATT_HOURS,"USD",null,null,it[1] != "Yes",it.getOrNull(11)?.ifBlank { null },0,0) }
        val fuelHeader = rows.indexOfFirst { it.firstOrNull() == "Vehicle Name" && it.getOrNull(1) == "Odometer (km)" }
        val fuels = mutableListOf<FuelEntry>()
        if (fuelHeader >= 0) notes += ImportNote(fuelHeader + 2, ImportLevel.INFO, "DRIVVO_MISSED_DEFAULT_FALSE", "Drivvo has no missed-fill column; imported as false")
        rows.drop(fuelHeader + 1).forEachIndexed { index, row ->
            val line = fuelHeader + index + 2
            if (row.firstOrNull()?.startsWith("#") == true || row.size < 7 || row[0].isBlank()) return@forEachIndexed
            val date = runCatching { LocalDate.parse(row[2], drivvoDate) }.getOrNull()
            val odo = row[1].toLongOrNull()
            if (date == null) { notes += ImportNote(line, ImportLevel.SKIP, "INVALID_DATE", "Drivvo date is invalid"); return@forEachIndexed }
            if (odo == null) { notes += ImportNote(line, ImportLevel.SKIP, "INVALID_ODOMETER", "Drivvo odometer is invalid"); return@forEachIndexed }
            val volume = row[6].toBigDecimalOrNull()
            if (volume == null) { notes += ImportNote(line, ImportLevel.SKIP, "INVALID_VOLUME", "Drivvo volume is invalid"); return@forEachIndexed }
            fuels += FuelEntry(fuels.size + 1L,1,date,Metres(odo * 1_000),FuelKind.LIQUID,Millilitres.litres(volume),null,UnitPrice((row[4].toBigDecimalOrNull()?.multiply(java.math.BigDecimal(1_000))?.toLong() ?: 0),"USD"),Money((row[5].toBigDecimalOrNull()?.multiply(java.math.BigDecimal(100))?.toLong() ?: 0),"USD"),row[3],row[7] == "Yes",false,row.getOrNull(26)?.ifBlank { null },null,row.getOrNull(28)?.ifBlank { null },0,0)
        }
        return ImportResult(PortabilityDomain(vehicles = listOfNotNull(vehicle), fuelEntries = fuels), notes)
    }
}
