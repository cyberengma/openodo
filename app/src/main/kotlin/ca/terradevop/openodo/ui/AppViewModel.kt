// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ca.terradevop.openodo.core.model.Vehicle
import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.model.RecordType
import ca.terradevop.openodo.data.ExpenseRecordRepository
import ca.terradevop.openodo.data.FuelEntryRepository
import ca.terradevop.openodo.data.ReminderRepository
import ca.terradevop.openodo.data.RecordTypeRepository
import ca.terradevop.openodo.data.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ca.terradevop.openodo.core.model.DefaultRecordTypes
import ca.terradevop.openodo.core.reminders.ResetResolver
import ca.terradevop.openodo.core.portability.BackupV1
import ca.terradevop.openodo.core.portability.PortabilityDomain
import ca.terradevop.openodo.core.portability.ImportResult
import ca.terradevop.openodo.core.portability.importer.DrivvoImporter
import ca.terradevop.openodo.core.portability.importer.FuelioImporter
import ca.terradevop.openodo.core.portability.csv.CsvExport
import java.io.StringWriter

data class ShellState(
    val vehicles: List<Vehicle> = emptyList(),
    val archivedVehicles: List<Vehicle> = emptyList(),
    val activeVehicleId: Long? = null,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val vehicles: VehicleRepository,
    private val fuels: FuelEntryRepository,
    private val expenses: ExpenseRecordRepository,
    private val reminders: ReminderRepository,
    private val recordTypes: RecordTypeRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    init {
        viewModelScope.launch {
            if (recordTypes.observeAll().first().isEmpty()) {
                DefaultRecordTypes.all.forEach { recordTypes.save(it) }
            }
        }
    }
    private val selected = MutableStateFlow<Long?>(context.getSharedPreferences("openodo", Context.MODE_PRIVATE).getLong("active_vehicle_id", 0L).takeIf { it != 0L })
    val state: StateFlow<ShellState> = combine(vehicles.observeActive(), vehicles.observeArchived(), selected) { active, archived, selectedId ->
        ShellState(active, archived, selectedId ?: active.firstOrNull()?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShellState())

    fun select(id: Long) {
        selected.value = id
        context.getSharedPreferences("openodo", Context.MODE_PRIVATE).edit().putLong("active_vehicle_id", id).apply()
    }

    fun save(vehicle: Vehicle) {
        viewModelScope.launch {
            val id = vehicles.save(vehicle)
            select(id)
        }
    }

    fun fuelEntries(vehicleId: Long) = fuels.observeForVehicle(vehicleId)
    fun expenseRecords(vehicleId: Long) = expenses.observeForVehicle(vehicleId)

    fun saveFuel(entry: FuelEntry) {
        viewModelScope.launch { fuels.save(entry) }
    }

    fun deleteFuel(entry: FuelEntry) {
        viewModelScope.launch { fuels.delete(entry) }
    }

    fun saveExpense(record: ExpenseRecord) {
        viewModelScope.launch {
            expenses.save(record)
            reminders.observeForVehicle(record.vehicleId).first()
                .let { ResetResolver.onRecordLogged(it, record) }
                .forEach { reminders.save(it) }
        }
    }

    fun deleteExpense(record: ExpenseRecord) {
        viewModelScope.launch { expenses.delete(record) }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch { vehicles.delete(vehicle) }
    }

    fun reminders(vehicleId: Long) = reminders.observeForVehicle(vehicleId)

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch { reminders.save(reminder) }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminders.delete(reminder) }
    }

    fun recordTypes() = recordTypes.observeAll()

    suspend fun backupJson(): String {
        val vehicles = vehicles.observeActive().first() + vehicles.observeArchived().first()
        val types = recordTypes.observeAll().first()
        val fuel = vehicles.flatMap { fuels.observeForVehicle(it.id).first() }
        val expenses = vehicles.flatMap { this.expenses.observeForVehicle(it.id).first() }
        val reminders = vehicles.flatMap { this.reminders.observeForVehicle(it.id).first() }
        return BackupV1.write(PortabilityDomain(vehicles, types, fuel, expenses, reminders), System.currentTimeMillis(), "0.1.0")
    }

    suspend fun exportFuelCsv(): String = exportDomain { domain, writer -> CsvExport.fuelEntries(domain, writer) }
    suspend fun exportExpenseCsv(): String = exportDomain { domain, writer -> CsvExport.expenseRecords(domain, writer) }

    suspend fun importDrivvo(csv: String): ImportResult = DrivvoImporter.import(csv)
    suspend fun importFuelio(csv: String): ImportResult = FuelioImporter.import(csv)

    suspend fun restoreJson(source: String): Result<Unit> = runCatching {
        val domain = BackupV1.read(source).getOrThrow()
        domain.vehicles.forEach { vehicles.save(it) }
        domain.recordTypes.forEach { recordTypes.save(it) }
        domain.fuelEntries.forEach { fuels.save(it) }
        domain.expenseRecords.forEach { expenses.save(it) }
        domain.reminders.forEach { reminders.save(it) }
    }

    private suspend fun exportDomain(write: (PortabilityDomain, StringWriter) -> Unit): String {
        val domain = PortabilityDomain(
            vehicles = vehicles.observeActive().first() + vehicles.observeArchived().first(),
            recordTypes = recordTypes.observeAll().first(),
            fuelEntries = vehicles.observeActive().first().flatMap { fuels.observeForVehicle(it.id).first() },
            expenseRecords = vehicles.observeActive().first().flatMap { expenses.observeForVehicle(it.id).first() },
            reminders = vehicles.observeActive().first().flatMap { reminders.observeForVehicle(it.id).first() },
        )
        return StringWriter().also { write(domain, it) }.toString()
    }
}
