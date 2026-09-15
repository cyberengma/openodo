// SPDX-License-Identifier: GPL-3.0-only
@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ca.terradevop.openodo.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.terradevop.openodo.core.fuel.FuelStats
import ca.terradevop.openodo.core.fuel.FuelStatsResult
import ca.terradevop.openodo.core.fuel.Economy
import ca.terradevop.openodo.core.fuel.PriceStats
import ca.terradevop.openodo.core.model.*
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.money.CurrencyMinorDigits
import ca.terradevop.openodo.core.portability.ImportResult
import ca.terradevop.openodo.core.reminders.DueCalculator
import ca.terradevop.openodo.core.reminders.ReminderState
import ca.terradevop.openodo.core.reminders.ReminderStatus
import ca.terradevop.openodo.core.units.*
import ca.terradevop.openodo.core.validation.ValidationResult
import ca.terradevop.openodo.core.validation.Validators
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.util.Locale

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    VEHICLES("vehicles", "Vehicles", Icons.Outlined.DirectionsCar),
    DASHBOARD("dashboard", "Dashboard", Icons.Outlined.Speed),
    RECORDS("records", "Records", Icons.Outlined.ReceiptLong),
    REMINDERS("reminders", "Reminders", Icons.Outlined.Notifications),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings),
    STATS("statistics", "Stats", Icons.Outlined.BarChart),
    DATA("portability", "Data", Icons.Outlined.Storage),
    TYPES("record-types", "Types", Icons.Outlined.Category),
}

internal fun initialAppRoute(hasVehicles: Boolean): String =
    if (hasVehicles) Destination.DASHBOARD.route else Destination.VEHICLES.route

@Composable
fun OpenOdoApp(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    if (!state.isLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val navController = rememberNavController()
    val startRoute = initialAppRoute(state.vehicles.isNotEmpty())
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: startRoute
    var lastBack by remember { mutableStateOf(0L) }
    var previousEmpty by remember { mutableStateOf(state.vehicles.isEmpty()) }

    LaunchedEffect(state.vehicles.isEmpty()) {
        if (previousEmpty && state.vehicles.isNotEmpty() && currentRoute == Destination.VEHICLES.route) {
            navController.navigate(Destination.DASHBOARD.route) {
                popUpTo(Destination.VEHICLES.route) { inclusive = true }
            }
        }
        previousEmpty = state.vehicles.isEmpty()
    }

    BackHandler(enabled = currentRoute == Destination.DASHBOARD.route || (currentRoute == Destination.VEHICLES.route && state.vehicles.isEmpty())) {
        val now = System.currentTimeMillis()
        if (now - lastBack < 2000) (context as? Activity)?.finish()
        else { lastBack = now; Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val primary = listOf(Destination.VEHICLES, Destination.DASHBOARD, Destination.RECORDS, Destination.REMINDERS, Destination.SETTINGS)
            if (primary.any { it.route == currentRoute }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    primary.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (item == Destination.DASHBOARD) navController.popBackStack(Destination.DASHBOARD.route, inclusive = false)
                                else navController.navigate(item.route) { launchSingleTop = true }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = startRoute, modifier = Modifier.padding(padding)) {
            composable(Destination.VEHICLES.route) { VehiclesScreen(state, viewModel, Modifier) }
            composable(Destination.DASHBOARD.route) {
                DashboardScreen(state, viewModel, Modifier, { navController.navigate("fuel/new") }, { navController.navigate("expense/new") }, { navController.navigate("reminder/new") })
            }
            composable(Destination.RECORDS.route) { RecordsScreen(state, viewModel, Modifier, null) {} }
            composable(Destination.REMINDERS.route) { RemindersScreen(state, viewModel, Modifier, false) {} }
            composable(Destination.SETTINGS.route) { SettingsScreen(state, Modifier, { navController.navigate(Destination.STATS.route) }, { navController.navigate(Destination.DATA.route) }, { navController.navigate(Destination.TYPES.route) }) }
            composable(Destination.STATS.route) { StatisticsScreen(state, viewModel, Modifier) }
            composable(Destination.DATA.route) { PortabilityScreen(viewModel, Modifier) }
            composable(Destination.TYPES.route) { RecordTypesScreen(viewModel, Modifier) }
            composable("fuel/new") {
                state.vehicles.firstOrNull { it.id == state.activeVehicleId }?.let { vehicle ->
                    FuelForm(vehicle, { viewModel.saveFuel(it); navController.popBackStack() }, { navController.popBackStack() }, Modifier)
                }
            }
            composable("expense/new") {
                state.vehicles.firstOrNull { it.id == state.activeVehicleId }?.let { vehicle ->
                    ExpenseForm(vehicle, viewModel, { viewModel.saveExpense(it); navController.popBackStack() }, { navController.popBackStack() }, Modifier)
                }
            }
            composable("reminder/new") {
                state.vehicles.firstOrNull { it.id == state.activeVehicleId }?.let { vehicle ->
                    ReminderForm(vehicle, viewModel, { viewModel.saveReminder(it); navController.popBackStack() }, { navController.popBackStack() }, Modifier)
                }
            }
        }
    }
}

// ---------- Shared components ----------

@Composable
private fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String = "OpenOdo",
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            actions()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        content()
    }
}

@Composable
private fun StatusPill(text: String, background: Color, foreground: Color) {
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Text(
            text,
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ConfigPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyState(title: String, message: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null && onAction != null) {
                Button(onClick = onAction) { Text(action) }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChartCard(title: String, message: String) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsRow(title: String, detail: String, onClick: (() -> Unit)? = null) {
    Card(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth(),
        enabled = onClick != null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onClick != null) Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// ---------- Vehicles ----------

@Composable
private fun FormTopBar(title: String, onClose: () -> Unit, onSave: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Close") }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Button(onClick = onSave) { Text("Save") }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
private fun rememberDiscardRequest(dirty: Boolean, discard: () -> Unit): () -> Unit {
    var confirm by remember { mutableStateOf(false) }
    val request: () -> Unit = { if (dirty) confirm = true else discard() }
    BackHandler { request() }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Discard changes?") },
            text = { Text("Your unsaved changes will be lost.") },
            confirmButton = { TextButton(onClick = { confirm = false; discard() }) { Text("Discard", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Keep editing") } },
        )
    }
    return request
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun VehiclesScreen(state: ShellState, viewModel: AppViewModel, modifier: Modifier) {
    var form by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Vehicle?>(null) }
    var deleteCandidate by remember { mutableStateOf<Vehicle?>(null) }
    if (form || editing != null) {
        VehicleForm({ viewModel.save(it); form = false; editing = null }, { form = false; editing = null }, modifier, editing)
        return
    }
    ScreenHeader("Vehicles", modifier, actions = {
        TextButton(onClick = { form = true }) { Icon(Icons.Filled.Add, contentDescription = "Add vehicle"); Spacer(Modifier.width(4.dp)); Text("Add") }
    }) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.vehicles.isEmpty()) {
                item { EmptyState("Your garage is empty", "Add your first vehicle to start a private, offline log.", "Add vehicle") { form = true } }
            }
            items(state.vehicles, key = { it.id }) { v ->
                VehicleCard(v, state.activeVehicleId == v.id, { viewModel.select(v.id) }, { editing = v }, { deleteCandidate = v })
            }
            if (state.archivedVehicles.isNotEmpty()) {
                item { Text("Archived", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(state.archivedVehicles, key = { it.id }) { VehicleCard(it, false, {}, { editing = it }, { deleteCandidate = it }) }
            }
        }
    }
    deleteCandidate?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Permanently delete ${vehicle.name}?") },
            text = { Text("This permanently deletes the vehicle, its fuel entries, expense records, and reminders. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteVehicle(vehicle); deleteCandidate = null }) { Text("Delete permanently", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun VehicleCard(v: Vehicle, active: Boolean, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (active) 2.dp else 1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(v.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (active) StatusPill("Active", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text("${v.make} ${v.model} • ${v.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ConfigPill(distanceLabel(v.distanceUnit))
                ConfigPill(volumeLabel(v.volumeUnit))
                ConfigPill(energyLabel(v.energyUnit))
                ConfigPill(v.currency)
            }
            Row {
                TextButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Edit", Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Edit") }
                TextButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "Delete", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(4.dp)); Text("Delete", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun VehicleForm(save: (Vehicle) -> Unit, cancel: () -> Unit, modifier: Modifier, initial: Vehicle? = null) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var make by remember(initial) { mutableStateOf(initial?.make ?: "") }
    var model by remember(initial) { mutableStateOf(initial?.model ?: "") }
    var currency by remember(initial) { mutableStateOf(initial?.currency ?: "USD") }
    var distance by remember(initial) { mutableStateOf(initial?.distanceUnit ?: DistanceUnit.KILOMETRES) }
    var volume by remember(initial) { mutableStateOf(initial?.volumeUnit ?: VolumeUnit.LITRES) }
    var energy by remember(initial) { mutableStateOf(initial?.energyUnit ?: EnergyUnit.KILOWATT_HOURS) }
    val dirty = name != (initial?.name ?: "") || make != (initial?.make ?: "") || model != (initial?.model ?: "") || currency != (initial?.currency ?: "USD") || distance != (initial?.distanceUnit ?: DistanceUnit.KILOMETRES) || volume != (initial?.volumeUnit ?: VolumeUnit.LITRES) || energy != (initial?.energyUnit ?: EnergyUnit.KILOWATT_HOURS)
    val requestCancel = rememberDiscardRequest(dirty, cancel)

    ScreenHeader(if (initial == null) "Add vehicle" else "Edit vehicle", modifier) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { TextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Vehicle name") }, singleLine = true) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(make, { make = it }, Modifier.weight(1f), label = { Text("Make") }, singleLine = true)
                    TextField(model, { model = it }, Modifier.weight(1f), label = { Text("Model") }, singleLine = true)
                }
            }
            item { Text("Distance unit", style = MaterialTheme.typography.titleSmall) }
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { DistanceUnit.entries.forEach { FilterChip(distance == it, { distance = it }, label = { Text(distanceLabel(it)) }) } } }
            item { Text("Volume unit", style = MaterialTheme.typography.titleSmall) }
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { VolumeUnit.entries.forEach { FilterChip(volume == it, { volume = it }, label = { Text(volumeLabel(it)) }) } } }
            item { Text("Energy unit", style = MaterialTheme.typography.titleSmall) }
            item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { EnergyUnit.entries.forEach { FilterChip(energy == it, { energy = it }, label = { Text(energyLabel(it)) }) } } }
            item { TextField(currency, { currency = it.uppercase().take(3) }, Modifier.fillMaxWidth(), label = { Text("Currency (ISO code)") }, singleLine = true) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = requestCancel, Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            save(Vehicle(
                                id = initial?.id ?: 0, name = name, make = make, model = model,
                                year = initial?.year ?: 2024, vin = initial?.vin, photoFileName = initial?.photoFileName,
                                distanceUnit = distance, volumeUnit = volume, energyUnit = energy,
                                currency = currency.ifBlank { "USD" }, manualOdometer = initial?.manualOdometer,
                                manualOdometerAt = initial?.manualOdometerAt, isArchived = initial?.isArchived ?: false,
                                notes = initial?.notes, createdAt = initial?.createdAt ?: 0, updatedAt = System.currentTimeMillis(),
                            ))
                        },
                        Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                    ) { Text("Save") }
                }
            }
        }
    }
}

// ---------- Dashboard ----------

@Composable
private fun DashboardScreen(
    state: ShellState,
    viewModel: AppViewModel,
    modifier: Modifier,
    onFuel: () -> Unit,
    onExpense: () -> Unit,
    onReminder: () -> Unit,
) {
    val v = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
    ScreenHeader(v?.name ?: "Dashboard", modifier, eyebrow = "Dashboard") {
        if (v == null) {
            Column(Modifier.padding(20.dp)) { EmptyState("Set up your first vehicle", "OpenOdo keeps your log on this device.", "Go to vehicles") {} }
            return@ScreenHeader
        }
        val fuels = viewModel.fuelEntries(v.id).collectAsState(initial = emptyList()).value
        val expenses = viewModel.expenseRecords(v.id).collectAsState(initial = emptyList()).value
        val reminders = viewModel.reminders(v.id).collectAsState(initial = emptyList()).value
        val currentOdometer = currentOdometer(v, fuels, expenses)
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { HeroCard(v, currentOdometer) }
            item { QuickActions(onFuel, onExpense, onReminder) }
            item { Text("Active reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (reminders.isEmpty()) item { EmptyState("No reminders yet", "Add a service interval to stay ahead.", "Add reminder", onReminder) }
            items(reminders.take(3), key = { it.id }) { ReminderCard(it, DueCalculator.evaluate(it, currentOdometer, LocalDate.now()), "Service", v, currentOdometer, viewModel, {}) }
            item { Text("Recent fuel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (fuels.isEmpty()) item { EmptyState("No fuel history", "Log your first fill-up or charging session.", "Add fuel", onFuel) }
            items(fuels.take(3), key = { it.id }) { FuelCard(it, {}, {}) }
        }
    }
}

@Composable
private fun HeroCard(v: Vehicle, currentOdometer: Metres?) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("CURRENT READING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            Text(currentOdometer?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, Locale.getDefault()) } ?: "—", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            Text(distanceLabel(v.distanceUnit), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
        }
    }
}

private fun currentOdometer(vehicle: Vehicle, fuels: List<FuelEntry>, expenses: List<ExpenseRecord>): Metres? {
    val readings = (fuels.map { RecordedOdometer(it.date, it.odometer) } + expenses.map { RecordedOdometer(it.date, it.odometer) })
        .filter { it.odometer.value >= 0 }
    return when (val result = CurrentOdometer.of(vehicle, readings)) {
        is CurrentOdometerResult.Value -> result.odometer
        CurrentOdometerResult.Empty -> null
    }
}

@Composable
private fun QuickActions(onFuel: () -> Unit, onExpense: () -> Unit, onReminder: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionTile(Icons.Filled.LocalGasStation, "Add fuel", onFuel, Modifier.weight(1f))
        ActionTile(Icons.Filled.Build, "Expense", onExpense, Modifier.weight(1f))
        ActionTile(Icons.Filled.Alarm, "Reminder", onReminder, Modifier.weight(1f))
    }
}

@Composable
private fun ActionTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ---------- Records ----------

@Composable
private fun RecordsScreen(state: ShellState, vm: AppViewModel, modifier: Modifier, action: String?, clear: () -> Unit) {
    val v = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
    var mode by remember { mutableIntStateOf(0) }
    var addFuel by remember { mutableStateOf(action == "fuel") }
    var addExpense by remember { mutableStateOf(action == "expense") }
    var editingFuel by remember { mutableStateOf<FuelEntry?>(null) }
    var editingExpense by remember { mutableStateOf<ExpenseRecord?>(null) }
    var deletingFuel by remember { mutableStateOf<FuelEntry?>(null) }
    var deletingExpense by remember { mutableStateOf<ExpenseRecord?>(null) }
    LaunchedEffect(action) {
        when (action) {
            "fuel" -> { addFuel = true; clear() }
            "expense" -> { addExpense = true; clear() }
        }
    }

    if (v != null && (addFuel || editingFuel != null)) {
        FuelForm(v, { vm.saveFuel(it); addFuel = false; editingFuel = null }, { addFuel = false; editingFuel = null }, modifier, editingFuel)
        return
    }
    if (v != null && (addExpense || editingExpense != null)) {
        ExpenseForm(v, vm, { vm.saveExpense(it); addExpense = false; editingExpense = null }, { addExpense = false; editingExpense = null }, modifier, editingExpense)
        return
    }
    val fuels = v?.let { vm.fuelEntries(it.id).collectAsState(initial = emptyList()).value }
    val expenses = v?.let { vm.expenseRecords(it.id).collectAsState(initial = emptyList()).value }

    ScreenHeader("Records", modifier, actions = {
        TextButton(onClick = { if (mode == 0) addFuel = true else addExpense = true }) {
            Icon(Icons.Filled.Add, contentDescription = "Add"); Spacer(Modifier.width(4.dp)); Text("Add")
        }
    }) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(mode == 0, { mode = 0 }, label = { Text("Fuel") })
                FilterChip(mode == 1, { mode = 1 }, label = { Text("Expenses") })
            }
            if (mode == 0) {
                if (fuels.isNullOrEmpty()) EmptyState("No fuel history", "Log fuel or charging to see it here.", "Add fuel") { addFuel = true }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(fuels, key = { it.id }) { FuelCard(it, { deletingFuel = it }, { editingFuel = it }) } }
            } else {
                if (expenses.isNullOrEmpty()) EmptyState("No expenses", "Log service, repair, upgrade, or other work.", "Add expense") { addExpense = true }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(expenses, key = { it.id }) { ExpenseCard(it, { deletingExpense = it }, { editingExpense = it }) } }
            }
        }
    }
    deletingFuel?.let { entry ->
        AlertDialog(
            onDismissRequest = { deletingFuel = null },
            title = { Text("Delete fuel entry?") },
            text = { Text("Delete ${entry.fuelLabel} from ${entry.date}? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { vm.deleteFuel(entry); deletingFuel = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deletingFuel = null }) { Text("Cancel") } },
        )
    }
    deletingExpense?.let { record ->
        AlertDialog(
            onDismissRequest = { deletingExpense = null },
            title = { Text("Delete expense record?") },
            text = { Text("Delete ${record.title}? Matching reminders will be re-anchored to the newest remaining record, or become unanchored. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { vm.deleteExpense(record); deletingExpense = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deletingExpense = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun FuelForm(v: Vehicle, save: (FuelEntry) -> Unit, cancel: () -> Unit, modifier: Modifier, initial: FuelEntry? = null) {
    val locale = Locale.getDefault()
    var kind by remember(initial) { mutableStateOf(initial?.kind ?: FuelKind.LIQUID) }
    var date by remember(initial) { mutableStateOf(initial?.date?.toString() ?: LocalDate.now().toString()) }
    var amount by remember(initial, v.volumeUnit, v.energyUnit) { mutableStateOf(if (initial?.kind == FuelKind.ELECTRIC) initial.energy?.let { VehicleValueFormatter.formatEnergy(it, v.energyUnit, locale) } ?: "" else initial?.volume?.let { VehicleValueFormatter.formatVolume(it, v.volumeUnit, locale) } ?: "") }
    var unitPrice by remember(initial) { mutableStateOf(initial?.unitPrice?.let { VehicleValueFormatter.formatUnitPrice(it, locale) } ?: "") }
    var cost by remember(initial) { mutableStateOf(initial?.totalCost?.let { VehicleValueFormatter.formatMoney(it, locale) } ?: "") }
    var odo by remember(initial, v.distanceUnit) { mutableStateOf(initial?.odometer?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale) } ?: "") }
    var label by remember(initial) { mutableStateOf(initial?.fuelLabel ?: "") }
    var station by remember(initial) { mutableStateOf(initial?.stationName ?: "") }
    var notes by remember(initial) { mutableStateOf(initial?.notes ?: "") }
    var full by remember(initial) { mutableStateOf(initial?.fullTank ?: true) }
    var missed by remember(initial) { mutableStateOf(initial?.missedPreviousFillUp ?: false) }
    var receipt by remember(initial) { mutableStateOf(initial?.receiptFileName) }
    var error by remember { mutableStateOf<String?>(null) }
    val initialAmount = if (initial?.kind == FuelKind.ELECTRIC) initial.energy?.let { VehicleValueFormatter.formatEnergy(it, v.energyUnit, locale) } ?: "" else initial?.volume?.let { VehicleValueFormatter.formatVolume(it, v.volumeUnit, locale) } ?: ""
    val dirty = kind != (initial?.kind ?: FuelKind.LIQUID) || date != (initial?.date?.toString() ?: LocalDate.now().toString()) || amount != initialAmount || unitPrice != (initial?.unitPrice?.let { VehicleValueFormatter.formatUnitPrice(it, locale) } ?: "") || cost != (initial?.totalCost?.let { VehicleValueFormatter.formatMoney(it, locale) } ?: "") || odo != (initial?.odometer?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale) } ?: "") || label != (initial?.fuelLabel ?: "") || station != (initial?.stationName ?: "") || notes != (initial?.notes ?: "") || full != (initial?.fullTank ?: true) || missed != (initial?.missedPreviousFillUp ?: false) || receipt != initial?.receiptFileName
    val requestCancel = rememberDiscardRequest(dirty, cancel)
    val receiptPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> receipt = uri?.toString() }
    val doSave: () -> Unit = {
        val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
        val parsedOdometer = VehicleValueFormatter.parseDistance(odo, v.distanceUnit, locale)
        val parsedMoney = VehicleValueFormatter.parseMoney(cost, v.currency, locale)
        val parsedVolume = if (kind == FuelKind.LIQUID) VehicleValueFormatter.parseVolume(amount, v.volumeUnit, locale) else null
        val parsedEnergy = if (kind == FuelKind.ELECTRIC) VehicleValueFormatter.parseEnergy(amount, v.energyUnit, locale) else null
        val candidate = if (parsedDate != null && parsedOdometer != null && parsedMoney != null && (parsedVolume != null || parsedEnergy != null)) FuelEntry(id = initial?.id ?: 0, vehicleId = v.id, date = parsedDate, odometer = parsedOdometer, kind = kind, volume = parsedVolume, energy = parsedEnergy, unitPrice = unitPrice.takeIf { it.isNotBlank() }?.let { VehicleValueFormatter.parseUnitPrice(it, v.currency, locale) }, totalCost = parsedMoney, fuelLabel = label.ifBlank { if (kind == FuelKind.LIQUID) "Fuel" else "Charging" }, fullTank = full, missedPreviousFillUp = missed, stationName = station.ifBlank { null }, receiptFileName = receipt, notes = notes.ifBlank { null }, createdAt = initial?.createdAt ?: System.currentTimeMillis(), updatedAt = System.currentTimeMillis()) else null
        when (val validation = candidate?.let { Validators.fuelEntry(it, Clock.systemDefaultZone()) }) {
            null -> error = "Use a valid date, odometer, decimal measurement, cost, and optional unit price"
            is ValidationResult.Error -> error = validation.message
            else -> save(candidate)
        }
    }

    Column(modifier.fillMaxSize()) {
        FormTopBar(if (initial == null) "Add fuel" else "Edit fuel", onClose = requestCancel, onSave = doSave)
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(kind == FuelKind.LIQUID, { kind = FuelKind.LIQUID }, label = { Text("Liquid fuel") }, leadingIcon = { Icon(Icons.Outlined.LocalGasStation, null, Modifier.size(16.dp)) }, modifier = Modifier.weight(1f))
                    FilterChip(kind == FuelKind.ELECTRIC, { kind = FuelKind.ELECTRIC }, label = { Text("Electric charging") }, leadingIcon = { Icon(Icons.Outlined.Bolt, null, Modifier.size(16.dp)) }, modifier = Modifier.weight(1f))
                }
            }
            item {
                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextField(date, { date = it }, Modifier.weight(1f), label = { Text("Date") }, singleLine = true)
                    }
                    TextField(odo, { odo = it }, Modifier.fillMaxWidth(), label = { Text("Odometer (${distanceLabel(v.distanceUnit)})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                }
            }
            item {
                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextField(amount, { amount = it }, Modifier.weight(1f), label = { Text(if (kind == FuelKind.LIQUID) "Volume (${volumeLabel(v.volumeUnit)})" else "Energy (${energyLabel(v.energyUnit)})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                        TextField(unitPrice, { unitPrice = it }, Modifier.weight(1f), label = { Text("Unit price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    }
                    TextField(cost, { cost = it }, Modifier.fillMaxWidth(), label = { Text("Total cost (${v.currency})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(full, { full = it }); Text(if (kind == FuelKind.LIQUID) "Full tank" else "Full charge") }
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(missed, { missed = it }); Text("Missed fill-up") }
                    }
                }
            }
            item {
                SectionCard {
                    TextField(station, { station = it }, Modifier.fillMaxWidth(), label = { Text("Gas station") }, placeholder = { Text("e.g. Shell, Ampol, Costco") }, singleLine = true)
                    TextField(label, { label = it }, Modifier.fillMaxWidth(), label = { Text("Fuel grade (optional)") }, placeholder = { Text("e.g. Unleaded 95, Regular, Premium") }, singleLine = true)
                    TextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notes (optional)") }, minLines = 2)
                    TextButton(onClick = { receiptPicker.launch(arrayOf("image/*", "application/pdf")) }) {
                        Icon(if (receipt == null) Icons.Outlined.AttachFile else Icons.Filled.CheckCircle, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (receipt == null) "Attach receipt" else "Receipt attached")
                    }
                }
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
}

@Composable
private fun ExpenseForm(v: Vehicle, vm: AppViewModel, save: (ExpenseRecord) -> Unit, cancel: () -> Unit, modifier: Modifier, initial: ExpenseRecord? = null) {
    val locale = Locale.getDefault()
    var title by remember(initial) { mutableStateOf(initial?.title ?: "") }
    var cost by remember(initial) { mutableStateOf(initial?.cost?.let { VehicleValueFormatter.formatMoney(it, locale) } ?: "") }
    var odo by remember(initial, v.distanceUnit) { mutableStateOf(initial?.odometer?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale) } ?: "") }
    val types = vm.recordTypes().collectAsState(initial = emptyList()).value
    var category by remember(initial?.id) { mutableStateOf(RecordCategory.SERVICE) }
    var type by remember(initial) { mutableStateOf(initial?.typeId ?: 0) }
    var typeInitialized by remember(initial?.id) { mutableStateOf(false) }
    var performedBy by remember(initial) { mutableStateOf(initial?.performedBy ?: PerformedBy.SELF) }
    var shop by remember(initial) { mutableStateOf(initial?.shopName ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    val categoryTypes = types.filter { it.category == category }
    LaunchedEffect(types, initial?.id) {
        if (!typeInitialized && types.isNotEmpty()) {
            val existing = initial?.let { record -> types.firstOrNull { it.id == record.typeId } }
            category = existing?.category ?: RecordCategory.SERVICE
            type = existing?.id ?: types.firstOrNull { it.category == category }?.id ?: 0
            typeInitialized = true
        }
    }
    LaunchedEffect(category) {
        if (typeInitialized && types.none { it.id == type && it.category == category }) type = categoryTypes.firstOrNull()?.id ?: 0
    }
    val dirty = title != (initial?.title ?: "") || cost != (initial?.cost?.let { VehicleValueFormatter.formatMoney(it, locale) } ?: "") || odo != (initial?.odometer?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale) } ?: "") || performedBy != (initial?.performedBy ?: PerformedBy.SELF) || shop != (initial?.shopName ?: "")
    val requestCancel = rememberDiscardRequest(dirty, cancel)
    val doSave: () -> Unit = {
        val parsedCost = VehicleValueFormatter.parseMoney(cost, v.currency, locale)
        val parsedOdometer = VehicleValueFormatter.parseDistance(odo, v.distanceUnit, locale)
        val candidate = if (type != 0L && title.isNotBlank() && parsedCost != null && parsedOdometer != null) ExpenseRecord(id = initial?.id ?: 0, vehicleId = v.id, typeId = type, date = initial?.date ?: LocalDate.now(), odometer = parsedOdometer, title = title, description = initial?.description, cost = parsedCost, performedBy = performedBy, shopName = shop.ifBlank { null }, warrantyUntil = initial?.warrantyUntil, receiptFileName = initial?.receiptFileName, notes = initial?.notes, createdAt = initial?.createdAt ?: System.currentTimeMillis(), updatedAt = System.currentTimeMillis()) else null
        when (val validation = candidate?.let(Validators::expenseRecord)) {
            null -> error = "Choose a type and enter a title, odometer, and valid cost"
            is ValidationResult.Error -> error = validation.message
            else -> save(candidate)
        }
    }

    Column(modifier.fillMaxSize()) {
        FormTopBar(if (initial == null) "Add expense" else "Edit expense", onClose = requestCancel, onSave = doSave)
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("Category", style = MaterialTheme.typography.titleSmall) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecordCategory.entries.forEach { c ->
                        FilterChip(category == c, { category = c }, leadingIcon = { Icon(categoryIcon(c), null, Modifier.size(16.dp)) }, label = { Text(categoryLabel(c)) })
                    }
                }
            }
            item { Text("Record type", style = MaterialTheme.typography.titleSmall) }
            item { RecordTypeDropdown(categoryTypes, type) { type = it } }
            item { TextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true) }
            item { TextField(odo, { odo = it }, Modifier.fillMaxWidth(), label = { Text("Odometer (${distanceLabel(v.distanceUnit)})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) }
            item { TextField(cost, { cost = it }, Modifier.fillMaxWidth(), label = { Text("Cost (${v.currency})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) }
            item { Text("Performed by", style = MaterialTheme.typography.titleSmall) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(performedBy == PerformedBy.SELF, { performedBy = PerformedBy.SELF }, label = { Text("Self") })
                    FilterChip(performedBy == PerformedBy.SHOP, { performedBy = PerformedBy.SHOP }, label = { Text("Shop") })
                }
            }
            if (performedBy == PerformedBy.SHOP) item { TextField(shop, { shop = it }, Modifier.fillMaxWidth(), label = { Text("Shop / mechanic name") }, singleLine = true) }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }
}

@Composable
private fun RecordTypeDropdown(types: List<RecordType>, selected: Long, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = types.firstOrNull { it.id == selected }?.name ?: "Select type"
    Box {
        TextField(
            value = selectedName,
            onValueChange = {},
            Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text("Record type") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            singleLine = true,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            types.forEach { t -> DropdownMenuItem(text = { Text(t.name) }, onClick = { onSelect(t.id); expanded = false }) }
        }
        // Invisible clickable overlay to open the menu
        Box(Modifier.matchParentSize().clickable { expanded = true })
    }
}

@Composable
private fun FuelCard(f: FuelEntry, onDelete: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(f.fuelLabel, fontWeight = FontWeight.SemiBold)
                Text("${f.date} • ${f.totalCost.currency} ${formatMoney(f.totalCost.minor, f.totalCost.currency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (f.receiptFileName != null) IconButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(f.receiptFileName), "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) } }) { Icon(Icons.Outlined.Receipt, contentDescription = "View receipt") }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ExpenseCard(r: ExpenseRecord, onDelete: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(r.title, fontWeight = FontWeight.SemiBold)
                Text("${r.date} • ${r.cost.currency} ${formatMoney(r.cost.minor, r.cost.currency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (r.receiptFileName != null) IconButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(r.receiptFileName), "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) } }) { Icon(Icons.Outlined.Receipt, contentDescription = "View receipt") }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ---------- Reminders ----------

@Composable
private fun RemindersScreen(state: ShellState, vm: AppViewModel, modifier: Modifier, open: Boolean, clear: () -> Unit) {
    val v = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
    var form by remember { mutableStateOf(open) }
    var editing by remember { mutableStateOf<Reminder?>(null) }
    LaunchedEffect(open) { if (open) { form = true; clear() } }
    if (v != null && (form || editing != null)) {
        ReminderForm(v, vm, { vm.saveReminder(it); form = false; editing = null }, { form = false; editing = null }, modifier, editing)
        return
    }
    val rs = if (v == null) emptyList<Reminder>() else vm.reminders(v.id).collectAsState(initial = emptyList()).value
    val fuels = if (v == null) emptyList() else vm.fuelEntries(v.id).collectAsState(initial = emptyList()).value
    val expenses = if (v == null) emptyList() else vm.expenseRecords(v.id).collectAsState(initial = emptyList()).value
    val currentOdometer = v?.let { currentOdometer(it, fuels, expenses) }
    val typeNames = vm.recordTypes().collectAsState(initial = emptyList()).value.associate { it.id to it.name }
    var filter by remember { mutableStateOf<ReminderState?>(null) }
    val statuses = rs.map { it to DueCalculator.evaluate(it, currentOdometer, LocalDate.now()) }
    val filtered = if (filter == null) statuses else statuses.filter { it.second.state == filter }
    ScreenHeader("Reminders", modifier, actions = {
        TextButton(onClick = { form = true }) { Icon(Icons.Filled.Add, contentDescription = "Add reminder"); Spacer(Modifier.width(4.dp)); Text("Add") }
    }) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(filter == null, { filter = null }, label = { Text("All (${rs.size})") })
                    listOf(ReminderState.OVERDUE, ReminderState.DUE_SOON, ReminderState.OK, ReminderState.INACTIVE).forEach { st ->
                        FilterChip(filter == st, { filter = st }, label = { Text("${st.name.lowercase().replaceFirstChar { it.uppercase() }} (${statuses.count { it.second.state == st }})") })
                    }
                }
            }
            item { Text("Whichever interval comes first", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (filtered.isEmpty()) item { EmptyState("No reminders", "Add a service interval linked to a service type.", "Add reminder") { form = true } }
            if (v != null) items(filtered, key = { it.first.id }) { (r, s) -> ReminderCard(r, s, typeNames[r.typeId] ?: "Service", v, currentOdometer, vm, { editing = r }) }
        }
    }
}

@Composable
private fun ReminderForm(v: Vehicle, vm: AppViewModel, save: (Reminder) -> Unit, cancel: () -> Unit, modifier: Modifier, initial: Reminder? = null) {
    val locale = Locale.getDefault()
    val types = vm.recordTypes().collectAsState(initial = emptyList()).value.filter { it.category == RecordCategory.SERVICE }
    var type by remember(initial?.id) { mutableStateOf(initial?.typeId ?: 0) }
    var typeInitialized by remember(initial?.id) { mutableStateOf(false) }
    var months by remember(initial) { mutableStateOf(initial?.intervalMonths?.toString() ?: "") }
    var distance by remember(initial, v.distanceUnit) { mutableStateOf(initial?.intervalDistance?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(types, initial?.id) {
        if (!typeInitialized && types.isNotEmpty()) {
            type = initial?.typeId?.takeIf { id -> types.any { it.id == id } } ?: types.first().id
            typeInitialized = true
        }
    }
    val dirty = type != (initial?.typeId ?: 0) || months != (initial?.intervalMonths?.toString() ?: "") || distance != (initial?.intervalDistance?.let { VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale) } ?: "")
    val requestCancel = rememberDiscardRequest(dirty, cancel)

    ScreenHeader(if (initial == null) "Add reminder" else "Edit reminder", modifier) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("Service type", style = MaterialTheme.typography.titleSmall) }
            item { RecordTypeDropdown(types, type) { type = it } }
            item { TextField(months, { months = it }, Modifier.fillMaxWidth(), label = { Text("Interval months") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) }
            item { TextField(distance, { distance = it }, Modifier.fillMaxWidth(), label = { Text("Interval distance (${distanceLabel(v.distanceUnit)})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = requestCancel, Modifier.weight(1f)) { Text("Cancel") }
                    Button(onClick = {
                        val m = months.toIntOrNull()
                        val d = distance.takeIf { it.isNotBlank() }?.let { VehicleValueFormatter.parseDistance(it, v.distanceUnit, locale) }
                        val candidate = Reminder(id = initial?.id ?: 0, vehicleId = v.id, typeId = type, intervalDistance = d, intervalMonths = m, anchorDate = initial?.anchorDate, anchorOdometer = initial?.anchorOdometer, active = initial?.active ?: true)
                        when (val validation = Validators.reminder(candidate)) {
                            is ValidationResult.Error -> error = validation.message
                            else -> if (type == 0L) error = "Choose a service type" else save(candidate)
                        }
                    }, Modifier.weight(1f)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(r: Reminder, s: ReminderStatus, name: String, v: Vehicle, currentOdometer: Metres?, vm: AppViewModel, onEdit: () -> Unit) {
    val overdue = s.state == ReminderState.OVERDUE
    val dueSoon = s.state == ReminderState.DUE_SOON
    val accent = when { overdue -> MaterialTheme.colorScheme.error; dueSoon -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.primary }
    val locale = Locale.getDefault()
    val intervalParts = buildList {
        r.intervalDistance?.let { add("${VehicleValueFormatter.formatDistance(it, v.distanceUnit, locale)} ${distanceSymbol(v.distanceUnit)}") }
        r.intervalMonths?.let { add("$it mo") }
    }
    val intervalText = if (intervalParts.isEmpty()) "No interval" else "Every ${intervalParts.joinToString(" or ")}"
    val remainingDays = s.remainingDays
    val remainingDistance = s.remainingDistance
    val nextDueDate = s.nextDueDate
    val nextDueOdometer = s.nextDueOdometer
    val intervalDistance = r.intervalDistance
    val remaining = when {
        remainingDays != null && remainingDays < 0 -> "${-remainingDays} days past due"
        remainingDistance != null && remainingDistance.value < 0 -> "${VehicleValueFormatter.formatDistance(Metres(-remainingDistance.value), v.distanceUnit, locale)} ${distanceSymbol(v.distanceUnit)} past due"
        remainingDays != null -> "$remainingDays days left"
        remainingDistance != null -> "${VehicleValueFormatter.formatDistance(remainingDistance, v.distanceUnit, locale)} ${distanceSymbol(v.distanceUnit)} left"
        else -> null
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (overdue) 2.dp else 1.dp, if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                StatusPill(s.state.name, if (overdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, if (overdue) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(intervalText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (nextDueDate != null) Text("Due $nextDueDate", style = MaterialTheme.typography.bodySmall, color = accent)
            if (nextDueOdometer != null) Text("Due at ${VehicleValueFormatter.formatDistance(nextDueOdometer, v.distanceUnit, locale)} ${distanceSymbol(v.distanceUnit)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            remaining?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = accent, fontWeight = FontWeight.SemiBold) }
            if (dueSoon && remainingDistance != null && intervalDistance != null) {
                val progress = (remainingDistance.value.toFloat() / intervalDistance.value.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Edit", Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Edit") }
                TextButton(onClick = { vm.saveReminder(r.copy(anchorDate = LocalDate.now(), anchorOdometer = currentOdometer)) }, enabled = currentOdometer != null) { Icon(Icons.Outlined.Refresh, contentDescription = null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Reset") }
                IconButton(onClick = { vm.saveReminder(r.copy(active = false)) }) { Icon(Icons.Outlined.PowerSettingsNew, contentDescription = "Deactivate", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

// ---------- Settings / Stats / Portability ----------

@Composable
private fun SettingsScreen(state: ShellState, modifier: Modifier, onStats: () -> Unit, onData: () -> Unit, onTypes: () -> Unit) {
    ScreenHeader("Settings", modifier) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SettingsRow("Active vehicle", state.vehicles.firstOrNull { it.id == state.activeVehicleId }?.currency ?: "No vehicle") }
            item { SettingsRow("Statistics", "Fuel economy and cost trends", onStats) }
            item { SettingsRow("Record types", "Manage service, repair, upgrade, and other types", onTypes) }
            item { SettingsRow("Backup & migration", "JSON • CSV • Drivvo • Fuelio", onData) }
            item { SettingsRow("Privacy", "Offline-first • zero tracking") }
        }
    }
}

@Composable
private fun RecordTypesScreen(vm: AppViewModel, modifier: Modifier) {
    val types = vm.recordTypes().collectAsState(initial = emptyList()).value
    var adding by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(RecordCategory.SERVICE) }
    ScreenHeader("Record types", modifier, actions = {
        TextButton(onClick = { adding = true }) { Icon(Icons.Filled.Add, contentDescription = "Add type"); Spacer(Modifier.width(4.dp)); Text("Add") }
    }) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (adding) {
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Type name") }, singleLine = true)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RecordCategory.entries.forEach { c -> FilterChip(category == c, { category = c }, leadingIcon = { Icon(categoryIcon(c), null, Modifier.size(16.dp)) }, label = { Text(categoryLabel(c)) }) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { adding = false; name = "" }, Modifier.weight(1f)) { Text("Cancel") }
                                Button(onClick = { if (name.isNotBlank()) { vm.saveRecordType(RecordType(category = category, name = name.trim(), isDefault = false)); adding = false; name = "" } }, Modifier.weight(1f), enabled = name.isNotBlank()) { Text("Save") }
                            }
                        }
                    }
                }
            }
            RecordCategory.entries.forEach { c ->
                val inCategory = types.filter { it.category == c }
                if (inCategory.isNotEmpty()) {
                    item { Text(categoryLabel(c), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(inCategory, key = { it.id }) { t -> Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { Text(t.name, Modifier.padding(14.dp)) } }
                }
            }
        }
    }
}

@Composable
private fun StatisticsScreen(state: ShellState, vm: AppViewModel, modifier: Modifier) {
    val v = state.vehicles.firstOrNull { it.id == state.activeVehicleId }
    val entries = if (v == null) emptyList() else vm.fuelEntries(v.id).collectAsState(initial = emptyList()).value
    val expenses = if (v == null) emptyList() else vm.expenseRecords(v.id).collectAsState(initial = emptyList()).value
    val stats = if (entries.isEmpty()) null else (FuelStats.of(entries, Clock.systemUTC()) as? FuelStatsResult.Stats)
    val prices = PriceStats.of(entries)
    var period by remember { mutableIntStateOf(1) }
    val months = listOf(3, 6, 12, 24)
    val monthCount = months[period]
    val buckets = stats?.monthBuckets?.takeLast(monthCount) ?: emptyList()
    val maxCost = buckets.maxOfOrNull { it.cost.minor.toFloat() } ?: 1f
    ScreenHeader(if (v == null) "Statistics" else "${v.name} statistics", modifier) {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("3M", "6M", "1Y", "2Y").forEachIndexed { i, label -> FilterChip(period == i, { period = i }, label = { Text(label) }) }
                }
            }
            item { MetricCard("Average consumption", stats?.averageMlPer100Km?.let { "${it.toDouble() / 1000.0} L/100km" } ?: "Add fuel entries to calculate") }
            item { MetricCard("Efficiency", stats?.averageMlPer100Km?.let { "${Economy.kmPerLitre(it, 2)} km/L" } ?: "—") }
            item { MetricCard("Total fuel cost", stats?.totalCost?.let { "${it.currency} ${formatMoney(it.minor, it.currency)}" } ?: "No cost data") }
            item { MetricCard("Cost per km", stats?.costPerKmMilli?.let { "${it.toDouble() / 1000.0}" } ?: "—") }
            item { MetricCard("Valid spans", stats?.spans?.count { it.valid }?.toString() ?: "0") }
            item { MetricCard("Best span", stats?.best?.mlPer100km?.let { "${it.toDouble() / 1000.0} L/100km" } ?: "—") }
            item { MetricCard("Worst span", stats?.worst?.mlPer100km?.let { "${it.toDouble() / 1000.0} L/100km" } ?: "—") }
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Monthly spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (buckets.isEmpty()) Text("No data in this period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                            buckets.forEach { b ->
                                val h = (b.cost.minor.toFloat() / maxCost * 100f).coerceAtLeast(4f)
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 2.dp), contentAlignment = Alignment.BottomCenter) {
                                        Box(Modifier.fillMaxWidth().fillMaxHeight(h / 100f).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                    }
                                    Text(b.month.monthValue.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            if (expenses.isNotEmpty()) item { ChartCard("Expense records", "${expenses.size} records tracked") }
            if (prices.isNotEmpty()) item { ChartCard("Unit price by label", prices.joinToString("\n") { "${it.label}: ${it.minimum}–${it.maximum} milli" }) }
        }
    }
}

@Composable
private fun PortabilityScreen(vm: AppViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("No portability operation yet") }
    var preview by remember { mutableStateOf<ImportResult?>(null) }

    val json = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch { context.contentResolver.openOutputStream(uri)?.use { it.write(vm.backupJson().toByteArray()) }; message = "JSON backup created" }
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            message = if (text == null) "Could not read backup" else if (vm.restoreJson(text).isSuccess) "Backup restored" else "Backup restore failed"
        }
    }
    val csv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch { context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportFuelCsv().toByteArray()) }; message = "Fuel CSV exported" }
    }
    val expensesCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch { context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportExpenseCsv().toByteArray()) }; message = "Expense CSV exported" }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (text == null) message = "Could not read selected file"
            else { preview = if (text.contains("#Refueling")) vm.importDrivvo(text) else vm.importFuelio(text); message = "Review import before applying" }
        }
    }

    ScreenHeader("Portability", modifier, eyebrow = "Offline data") {
        LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Keep your data portable and local.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Button(onClick = { json.launch("openodo-backup.json") }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Save, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Create JSON backup") } }
            item { OutlinedButton(onClick = { restore.launch(arrayOf("application/json", "text/json")) }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Upload, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Restore JSON backup") } }
            item { OutlinedButton(onClick = { csv.launch("openodo-fuel.csv") }, Modifier.fillMaxWidth()) { Text("Export fuel CSV") } }
            item { OutlinedButton(onClick = { expensesCsv.launch("openodo-expenses.csv") }, Modifier.fillMaxWidth()) { Text("Export expense CSV") } }
            item { OutlinedButton(onClick = { importer.launch(arrayOf("text/csv", "text/plain", "text/comma-separated-values")) }, Modifier.fillMaxWidth()) { Text("Import Drivvo / Fuelio CSV") } }
            preview?.let { p ->
                item { ChartCard("Import preview", "${p.domain.fuelEntries.size} fuel • ${p.domain.expenseRecords.size} expenses • ${p.report.count { it.level.toString().contains("WARN") }} warnings • ${p.report.count { it.level.toString().contains("SKIP") }} skipped") }
                if (p.domain.recordTypes.isNotEmpty()) item { ChartCard("Expense types to import", p.domain.recordTypes.joinToString("\n") { "${it.name} → ${categoryLabel(it.category)}" }) }
                if (p.report.isNotEmpty()) item { ChartCard("Report details", p.report.take(8).joinToString("\n") { "Row ${it.row}: ${it.code}" }) }
                item { Button(onClick = { vm.applyImport(p); preview = null; message = "Import applied" }, Modifier.fillMaxWidth()) { Text("Apply import") } }
            }
            item { Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

private fun distanceLabel(u: DistanceUnit) = when (u) {
    DistanceUnit.METRES -> "Metres (m)"
    DistanceUnit.KILOMETRES -> "Kilometres (km)"
    DistanceUnit.MILES -> "Miles (mi)"
}

private fun distanceSymbol(u: DistanceUnit) = when (u) {
    DistanceUnit.METRES -> "m"
    DistanceUnit.KILOMETRES -> "km"
    DistanceUnit.MILES -> "mi"
}

private fun volumeLabel(u: VolumeUnit) = when (u) {
    VolumeUnit.MILLILITRES -> "Millilitres (mL)"
    VolumeUnit.LITRES -> "Litres (L)"
    VolumeUnit.US_GALLONS -> "US gallons (gal)"
    VolumeUnit.UK_GALLONS -> "UK gallons (gal)"
}

private fun energyLabel(u: EnergyUnit) = when (u) {
    EnergyUnit.WATT_HOURS -> "Watt-hours (Wh)"
    EnergyUnit.KILOWATT_HOURS -> "Kilowatt-hours (kWh)"
}

private fun categoryLabel(c: RecordCategory) = when (c) {
    RecordCategory.SERVICE -> "Service"
    RecordCategory.REPAIR -> "Repair"
    RecordCategory.UPGRADE -> "Upgrade"
    RecordCategory.OTHER -> "Other"
}

private fun categoryIcon(c: RecordCategory): ImageVector = when (c) {
    RecordCategory.SERVICE -> Icons.Outlined.Build
    RecordCategory.REPAIR -> Icons.Outlined.Handyman
    RecordCategory.UPGRADE -> Icons.Outlined.AutoAwesome
    RecordCategory.OTHER -> Icons.Outlined.MoreHoriz
}

private fun formatMoney(minor: Long, currency: String): String {
    val digits = CurrencyMinorDigits.of(currency)
    return java.math.BigDecimal(minor).movePointLeft(digits).setScale(digits).toPlainString()
}
