// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.terradevop.openodo.core.model.Vehicle
import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.data.ExpenseRecordRepository
import ca.terradevop.openodo.data.FuelEntryRepository
import ca.terradevop.openodo.data.ReminderRepository
import ca.terradevop.openodo.data.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
) : ViewModel() {
    private val selected = MutableStateFlow<Long?>(null)
    val state: StateFlow<ShellState> = combine(vehicles.observeActive(), vehicles.observeArchived(), selected) { active, archived, selectedId ->
        ShellState(active, archived, selectedId ?: active.firstOrNull()?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShellState())

    fun select(id: Long) { selected.value = id }

    fun save(vehicle: Vehicle) {
        viewModelScope.launch {
            val id = vehicles.save(vehicle)
            selected.value = id
        }
    }

    fun fuelEntries(vehicleId: Long) = fuels.observeForVehicle(vehicleId)
    fun expenseRecords(vehicleId: Long) = expenses.observeForVehicle(vehicleId)

    fun saveFuel(entry: FuelEntry) {
        viewModelScope.launch { fuels.save(entry) }
    }

    fun saveExpense(record: ExpenseRecord) {
        viewModelScope.launch { expenses.save(record) }
    }

    fun reminders(vehicleId: Long) = reminders.observeForVehicle(vehicleId)

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch { reminders.save(reminder) }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminders.delete(reminder) }
    }
}
