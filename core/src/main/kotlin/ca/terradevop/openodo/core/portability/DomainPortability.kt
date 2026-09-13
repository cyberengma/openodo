// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.portability

import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.RecordType
import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.model.Vehicle

data class PortabilityDomain(
    val vehicles: List<Vehicle> = emptyList(),
    val recordTypes: List<RecordType> = emptyList(),
    val fuelEntries: List<FuelEntry> = emptyList(),
    val expenseRecords: List<ExpenseRecord> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val photos: List<String> = emptyList(),
)

sealed class BackupError {
    data class InvalidJson(val message: String) : BackupError()
    data class UnsupportedSchema(val version: Int) : BackupError()
}

sealed class ImportLevel { data object INFO : ImportLevel(); data object WARN : ImportLevel(); data object SKIP : ImportLevel() }
data class ImportNote(val row: Int, val level: ImportLevel, val code: String, val message: String)
data class ImportResult(val domain: PortabilityDomain, val report: List<ImportNote>)
