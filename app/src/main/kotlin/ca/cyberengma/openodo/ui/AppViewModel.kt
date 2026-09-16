// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ca.cyberengma.openodo.core.model.Vehicle
import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.model.RecordType
import ca.cyberengma.openodo.data.ExpenseRecordRepository
import ca.cyberengma.openodo.data.FuelEntryRepository
import ca.cyberengma.openodo.data.ReminderRepository
import ca.cyberengma.openodo.data.RecordTypeRepository
import ca.cyberengma.openodo.data.VehicleRepository
import ca.cyberengma.openodo.data.DomainTransactionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ca.cyberengma.openodo.core.model.DefaultRecordTypes
import ca.cyberengma.openodo.core.reminders.ResetResolver
import ca.cyberengma.openodo.core.portability.BackupV1
import ca.cyberengma.openodo.core.portability.PortabilityDomain
import ca.cyberengma.openodo.core.portability.ImportResult
import ca.cyberengma.openodo.core.portability.importer.DrivvoImporter
import ca.cyberengma.openodo.core.portability.importer.FuelioImporter
import ca.cyberengma.openodo.core.portability.csv.CsvExport
import java.io.StringWriter

data class ShellState(
    val vehicles: List<Vehicle> = emptyList(),
    val archivedVehicles: List<Vehicle> = emptyList(),
    val activeVehicleId: Long? = null,
    val isLoaded: Boolean = false,
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val vehicles: VehicleRepository,
    private val fuels: FuelEntryRepository,
    private val expenses: ExpenseRecordRepository,
    private val reminders: ReminderRepository,
    private val recordTypes: RecordTypeRepository,
    private val transactions: DomainTransactionCoordinator,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val preferences = context.getSharedPreferences("openodo", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            if (recordTypes.observeAll().first().isEmpty()) {
                DefaultRecordTypes.all.forEach { recordTypes.save(it) }
            }
        }
    }
    private val selected = MutableStateFlow<Long?>(preferences.getLong("active_vehicle_id", 0L).takeIf { it != 0L })
    private val selectedTheme = MutableStateFlow(
        runCatching { ThemeMode.valueOf(preferences.getString("theme_mode", ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM),
    )
    val themeMode: StateFlow<ThemeMode> = selectedTheme
    val state: StateFlow<ShellState> = combine(vehicles.observeActive(), vehicles.observeArchived(), selected) { active, archived, selectedId ->
        val effectiveId = selectedId?.takeIf { id -> active.any { it.id == id } } ?: active.firstOrNull()?.id
        ShellState(active, archived, effectiveId, isLoaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShellState())

    fun select(id: Long) {
        selected.value = id
        preferences.edit().putLong("active_vehicle_id", id).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        selectedTheme.value = mode
        preferences.edit().putString("theme_mode", mode.name).apply()
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
        viewModelScope.launch { transactions.saveExpense(record) }
    }

    fun deleteExpense(record: ExpenseRecord) {
        viewModelScope.launch { transactions.deleteExpense(record) }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            transactions.deleteVehicle(vehicle)
            if (selected.value == vehicle.id) {
                selected.value = null
                preferences.edit().remove("active_vehicle_id").apply()
            }
        }
    }

    fun reminders(vehicleId: Long) = reminders.observeForVehicle(vehicleId)

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch { reminders.save(reminder) }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminders.delete(reminder) }
    }

    fun recordTypes() = recordTypes.observeAll()

    fun saveRecordType(type: RecordType) {
        viewModelScope.launch { recordTypes.save(type) }
    }

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

    fun applyImport(result: ImportResult) {
        viewModelScope.launch {
            val id = transactions.applyImport(result)
            select(id)
        }
    }

    suspend fun restoreJson(source: String): Result<Unit> = runCatching {
        val domain = BackupV1.read(source).getOrThrow()
        transactions.replaceAll(domain)
        domain.vehicles.firstOrNull { !it.isArchived }?.let { select(it.id) }
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
