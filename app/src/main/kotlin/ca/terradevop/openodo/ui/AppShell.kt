// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import ca.terradevop.openodo.core.model.Vehicle
import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.FuelKind
import ca.terradevop.openodo.core.model.ExpenseRecord
import ca.terradevop.openodo.core.model.RecordCategory
import ca.terradevop.openodo.core.model.PerformedBy
import ca.terradevop.openodo.core.model.Reminder
import ca.terradevop.openodo.core.reminders.DueCalculator
import ca.terradevop.openodo.core.reminders.ReminderState
import ca.terradevop.openodo.core.reminders.ReminderStatus
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.UnitPrice
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import ca.terradevop.openodo.core.units.WattHours
import java.time.LocalDate
import ca.terradevop.openodo.core.units.DistanceUnit
import ca.terradevop.openodo.core.units.EnergyUnit
import ca.terradevop.openodo.core.units.VolumeUnit

private enum class Destination(val label: String) { VEHICLES("Vehicles"), DASHBOARD("Dashboard"), RECORDS("Records"), REMINDERS("Reminders"), SETTINGS("Settings") }

@Composable
fun OpenOdoApp(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    var destination by remember { mutableStateOf(Destination.DASHBOARD) }
    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = { Text(item.label.take(1)) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        when (destination) {
            Destination.VEHICLES -> VehiclesScreen(state, viewModel, Modifier.padding(padding))
            Destination.DASHBOARD -> DashboardScreen(state, Modifier.padding(padding))
            Destination.RECORDS -> RecordsScreen(state, viewModel, Modifier.padding(padding))
            Destination.REMINDERS -> RemindersScreen(state, viewModel, Modifier.padding(padding))
            Destination.SETTINGS -> SettingsScreen(state, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun VehiclesScreen(state: ShellState, viewModel: AppViewModel, modifier: Modifier) {
    var editing by remember { mutableStateOf(false) }
    if (editing) {
        VehicleForm(onSave = { viewModel.save(it); editing = false }, onCancel = { editing = false }, modifier)
        return
    }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column { Text("OpenOdo", style = MaterialTheme.typography.labelSmall); Text("Vehicles", style = MaterialTheme.typography.headlineSmall) }
            IconButton(onClick = { editing = true }) { Text("+") }
        }
        if (state.vehicles.isEmpty()) {
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("No vehicles yet", style = MaterialTheme.typography.titleLarge); Text("Add a vehicle to start your private, offline log."); Button(onClick = { editing = true }) { Text("Add vehicle") } } }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.vehicles, key = { it.id }) { vehicle -> VehicleCard(vehicle, state.activeVehicleId == vehicle.id) { viewModel.select(vehicle.id) } }
                if (state.archivedVehicles.isNotEmpty()) { item { Text("Archived", style = MaterialTheme.typography.titleMedium) }; items(state.archivedVehicles, key = { it.id }) { VehicleCard(it, false) {} } }
            }
        }
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle, active: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(vehicle.name, style = MaterialTheme.typography.titleMedium); if (active) Text("Active", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium); Text("${vehicle.make} ${vehicle.model} • ${vehicle.year}"); Text("${vehicle.distanceUnit.name} • ${vehicle.volumeUnit.name} • ${vehicle.energyUnit.name} • ${vehicle.currency}", style = MaterialTheme.typography.labelSmall) } }
}

@Composable
private fun VehicleForm(onSave: (Vehicle) -> Unit, onCancel: () -> Unit, modifier: Modifier) {
    var name by remember { mutableStateOf("") }; var make by remember { mutableStateOf("") }; var model by remember { mutableStateOf("") }; var year by remember { mutableStateOf("") }; var currency by remember { mutableStateOf("USD") }; var distance by remember { mutableIntStateOf(0) }; var volume by remember { mutableIntStateOf(0) }
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Add vehicle", style = MaterialTheme.typography.headlineSmall) }
        item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Vehicle name") }) }
        item { OutlinedTextField(make, { make = it }, Modifier.fillMaxWidth(), label = { Text("Make") }) }
        item { OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Model") }) }
        item { OutlinedTextField(year, { year = it }, Modifier.fillMaxWidth(), label = { Text("Year") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
        item { Text("Distance unit", style = MaterialTheme.typography.labelLarge); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { DistanceUnit.entries.forEachIndexed { i, unit -> FilterChip(selected = distance == i, onClick = { distance = i }, label = { Text(unit.name) }) } } }
        item { Text("Volume unit", style = MaterialTheme.typography.labelLarge); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { VolumeUnit.entries.forEachIndexed { i, unit -> FilterChip(selected = volume == i, onClick = { volume = i }, label = { Text(unit.name) }) } } }
        item { OutlinedTextField(currency, { currency = it.uppercase().take(3) }, Modifier.fillMaxWidth(), label = { Text("Currency") }) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedButton(onClick = onCancel) { Text("Cancel") }; Button(enabled = name.isNotBlank(), onClick = { onSave(Vehicle(name = name, make = make, model = model, year = year.toIntOrNull() ?: 0, vin = null, photoFileName = null, distanceUnit = DistanceUnit.entries[distance], volumeUnit = VolumeUnit.entries[volume], energyUnit = EnergyUnit.KILOWATT_HOURS, currency = currency.ifBlank { "USD" }, manualOdometer = null, manualOdometerAt = null, isArchived = false, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())) }) { Text("Save") } } }
    }
}

@Composable
private fun DashboardScreen(state: ShellState, modifier: Modifier) { val vehicle = state.vehicles.firstOrNull { it.id == state.activeVehicleId }; if (vehicle == null) PlaceholderScreen("Dashboard", "Add a vehicle to begin.", modifier) else Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(vehicle.name, style = MaterialTheme.typography.headlineSmall); Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text("Current reading", style = MaterialTheme.typography.labelMedium); Text(vehicle.manualOdometer?.value?.toString() ?: "No reading", style = MaterialTheme.typography.displaySmall); Text(vehicle.distanceUnit.name) } }; Text("Quick actions", style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = {}) { Text("Add fuel") }; OutlinedButton(onClick = {}) { Text("Add expense") } } } }

@Composable
private fun SettingsScreen(state: ShellState, viewModel: AppViewModel, modifier: Modifier) { Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Settings", style = MaterialTheme.typography.headlineSmall); Text("Active vehicle configuration", style = MaterialTheme.typography.titleMedium); state.vehicles.firstOrNull { it.id == state.activeVehicleId }?.let { Text("${it.distanceUnit.name} • ${it.volumeUnit.name} • ${it.currency}") }; OutlinedButton(onClick = { }) { Text("Manage vehicles") } } }

@Composable
private fun RemindersScreen(state: ShellState, viewModel: AppViewModel, modifier: Modifier) {
    val vehicle = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
    var editing by remember { mutableStateOf(false) }
    val reminders = if (vehicle == null) emptyList() else viewModel.reminders(vehicle.id).collectAsState(initial = emptyList()).value
    if (editing && vehicle != null) { ReminderForm(vehicle, { viewModel.saveReminder(it); editing = false }, { editing = false }, modifier); return }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Reminders", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.weight(1f)); IconButton(onClick = { editing = true }) { Text("+") } }
        if (reminders.isEmpty()) EmptyCard("No reminders", "Add a maintenance reminder to stay ahead of service.") else if (vehicle != null) LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(reminders, key = { it.id }) { reminder -> ReminderCard(reminder, viewModel, vehicle) } }
    }
}

@Composable
private fun ReminderCard(reminder: Reminder, viewModel: AppViewModel, vehicle: Vehicle) {
    val status = DueCalculator.evaluate(reminder, vehicle.manualOdometer, LocalDate.now())
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("Reminder #${reminder.id}", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium); Text(status.state.name, color = if (status.state == ReminderState.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium) }; Text("${status.nextDueDate ?: "No date"} • ${status.nextDueOdometer?.value ?: "No odometer"} m", style = MaterialTheme.typography.bodySmall); if (status.staleOdometer) Text("Odometer is stale", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { viewModel.saveReminder(reminder.copy(anchorDate = LocalDate.now(), anchorOdometer = vehicle.manualOdometer)) }) { Text("Reset") }; OutlinedButton(onClick = { viewModel.deleteReminder(reminder) }) { Text("Delete") } } } }
}

@Composable
private fun ReminderForm(vehicle: Vehicle, onSave: (Reminder) -> Unit, onCancel: () -> Unit, modifier: Modifier) {
    var months by remember { mutableStateOf("") }; var distance by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Add reminder", style = MaterialTheme.typography.headlineSmall); OutlinedTextField(months, { months = it }, Modifier.fillMaxWidth(), label = { Text("Interval months") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); OutlinedTextField(distance, { distance = it }, Modifier.fillMaxWidth(), label = { Text("Interval distance (${vehicle.distanceUnit.name})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); error?.let { Text(it, color = MaterialTheme.colorScheme.error) }; Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = onCancel) { Text("Cancel") }; Button(onClick = { val m = months.toIntOrNull(); val d = distance.toLongOrNull(); if ((m == null || m <= 0) && (d == null || d <= 0)) error = "At least one positive interval is required" else onSave(Reminder(vehicleId = vehicle.id, typeId = 0, intervalDistance = d?.let { Metres(it * 1_000) }, intervalMonths = m, anchorDate = null, anchorOdometer = null, active = true)) }) { Text("Save") } } }
}

@Composable
private fun RecordsScreen(state: ShellState, viewModel: AppViewModel, modifier: Modifier) {
    val vehicle = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
    var mode by remember { mutableIntStateOf(0) }
    var addingFuel by remember { mutableStateOf(false) }
    var addingExpense by remember { mutableStateOf(false) }
    val fuels = if (vehicle == null) null else viewModel.fuelEntries(vehicle.id).collectAsState(initial = emptyList()).value
    val expenses = if (vehicle == null) null else viewModel.expenseRecords(vehicle.id).collectAsState(initial = emptyList()).value
    if (addingFuel && vehicle != null) { FuelForm(vehicle, { viewModel.saveFuel(it); addingFuel = false }, { addingFuel = false }, modifier); return }
    if (addingExpense && vehicle != null) { ExpenseForm(vehicle, { viewModel.saveExpense(it); addingExpense = false }, { addingExpense = false }, modifier); return }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Records", style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.weight(1f)); IconButton(onClick = { if (mode == 0) addingFuel = true else addingExpense = true }) { Text("+") } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("Fuel") }); FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("Expenses") }) }
        if (mode == 0) {
            if (fuels.isNullOrEmpty()) EmptyCard("No fuel entries", "Add fuel or charging to begin.") else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(fuels, key = { it.id }) { entry -> RecordCard("${entry.fuelLabel} • ${entry.kind.name}", entry.date.toString(), "${entry.totalCost.currency} ${entry.totalCost.minor}", "${entry.odometer.value} m") } }
        } else {
            if (expenses.isNullOrEmpty()) EmptyCard("No expense records", "Add service, repair, upgrade, or other work.") else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(expenses, key = { it.id }) { record -> RecordCard(record.title, record.date.toString(), "${record.cost.currency} ${record.cost.minor}", record.performedBy.name) } }
        }
    }
}

@Composable private fun EmptyCard(title: String, message: String) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(message, style = MaterialTheme.typography.bodyMedium) } } }

@Composable private fun RecordCard(title: String, date: String, amount: String, detail: String) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(date, style = MaterialTheme.typography.labelSmall); Row { Text(detail, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall); Text(amount, style = MaterialTheme.typography.labelLarge) } } } }

@Composable private fun FuelForm(vehicle: Vehicle, onSave: (FuelEntry) -> Unit, onCancel: () -> Unit, modifier: Modifier) {
    var electric by remember { mutableStateOf(false) }; var odo by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var cost by remember { mutableStateOf("") }; var label by remember { mutableStateOf("") }; var full by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Add ${if (electric) "charging" else "fuel"}", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = !electric, onClick = { electric = false }, label = { Text("Liquid") }); FilterChip(selected = electric, onClick = { electric = true }, label = { Text("Electric") }) }
        OutlinedTextField(odo, { odo = it }, Modifier.fillMaxWidth(), label = { Text("Odometer (${vehicle.distanceUnit.name})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(amount, { amount = it }, Modifier.fillMaxWidth(), label = { Text(if (electric) "Energy (${vehicle.energyUnit.name})" else "Volume (${vehicle.volumeUnit.name})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(cost, { cost = it }, Modifier.fillMaxWidth(), label = { Text("Total cost (${vehicle.currency})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(label, { label = it }, Modifier.fillMaxWidth(), label = { Text("Fuel or charging label") })
        Row(verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Checkbox(full, { full = it }); Text(if (electric) "Full charge" else "Full tank") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = onCancel) { Text("Cancel") }; Button(onClick = { val o = odo.toLongOrNull(); val a = amount.toLongOrNull(); val c = cost.toLongOrNull(); if (o == null || a == null || a <= 0 || c == null || c < 0) error = "Enter a positive measurement and non-negative cost" else onSave(FuelEntry(vehicleId = vehicle.id, date = LocalDate.now(), odometer = Metres(o), kind = if (electric) FuelKind.ELECTRIC else FuelKind.LIQUID, volume = if (electric) null else Millilitres(a), energy = if (electric) WattHours(a) else null, unitPrice = null, totalCost = Money(c, vehicle.currency), fuelLabel = label.ifBlank { if (electric) "Charging" else "Fuel" }, fullTank = full, missedPreviousFillUp = false, stationName = null, receiptFileName = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())) }) { Text("Save") } }
    }
}

@Composable private fun ExpenseForm(vehicle: Vehicle, onSave: (ExpenseRecord) -> Unit, onCancel: () -> Unit, modifier: Modifier) {
    var title by remember { mutableStateOf("") }; var cost by remember { mutableStateOf("") }; var category by remember { mutableStateOf(RecordCategory.SERVICE) }; var error by remember { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Add expense", style = MaterialTheme.typography.headlineSmall); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { RecordCategory.entries.forEach { FilterChip(selected = category == it, onClick = { category = it }, label = { Text(it.name) }) } }; OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }); OutlinedTextField(cost, { cost = it }, Modifier.fillMaxWidth(), label = { Text("Cost (${vehicle.currency})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); error?.let { Text(it, color = MaterialTheme.colorScheme.error) }; Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = onCancel) { Text("Cancel") }; Button(onClick = { val value = cost.toLongOrNull(); if (title.isBlank() || value == null || value < 0) error = "Title and non-negative cost are required" else onSave(ExpenseRecord(0,vehicle.id,0,LocalDate.now(),Metres(0),title,null,Money(value,vehicle.currency),PerformedBy.SELF,null,null,null,null,System.currentTimeMillis(),System.currentTimeMillis())) }) { Text("Save") } } }
}

@Composable
private fun PlaceholderScreen(title: String, message: String, modifier: Modifier) { Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall); Card(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(20.dp)) } } }
