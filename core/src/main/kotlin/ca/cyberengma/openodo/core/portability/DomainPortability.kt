// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.portability

import ca.cyberengma.openodo.core.model.ExpenseLineItem
import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.model.RecordType
import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.model.Vehicle
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.UnitPrice

data class PortabilityDomain(
    val vehicles: List<Vehicle> = emptyList(),
    val recordTypes: List<RecordType> = emptyList(),
    val fuelEntries: List<FuelEntry> = emptyList(),
    val expenseRecords: List<ExpenseRecord> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val photos: List<String> = emptyList(),
) {
    fun withCurrency(code: String): PortabilityDomain {
        val currency = code.uppercase().trim()
        return copy(
            vehicles = vehicles.map { it.copy(currency = currency) },
            fuelEntries = fuelEntries.map { fuel ->
                fuel.copy(
                    totalCost = Money(fuel.totalCost.minor, currency),
                    unitPrice = fuel.unitPrice?.let { UnitPrice(it.milli, currency) },
                )
            },
            expenseRecords = expenseRecords.map { record ->
                record.copy(
                    lineItems = record.lineItems.map { li -> ExpenseLineItem(li.typeId, Money(li.cost.minor, currency)) },
                )
            },
        )
    }
}

sealed class BackupError {
    data class InvalidJson(val message: String) : BackupError()
    data class UnsupportedSchema(val version: Int) : BackupError()
}

sealed class ImportLevel { data object INFO : ImportLevel(); data object WARN : ImportLevel(); data object SKIP : ImportLevel() }
data class ImportNote(val row: Int, val level: ImportLevel, val code: String, val message: String)
data class ImportResult(val domain: PortabilityDomain, val report: List<ImportNote>)
