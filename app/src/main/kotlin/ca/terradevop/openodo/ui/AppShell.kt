// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import ca.terradevop.openodo.core.model.Vehicle
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
            Destination.RECORDS -> PlaceholderScreen("Records", "Expense and fuel records will appear here.", Modifier.padding(padding))
            Destination.REMINDERS -> PlaceholderScreen("Reminders", "Maintenance reminders will appear here.", Modifier.padding(padding))
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
private fun PlaceholderScreen(title: String, message: String, modifier: Modifier) { Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall); Card(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(20.dp)) } } }
