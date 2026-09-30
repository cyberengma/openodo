// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.cyberengma.openodo.MainActivity
import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.Vehicle
import ca.cyberengma.openodo.core.reminders.DueReminders
import ca.cyberengma.openodo.core.reminders.ReminderState
import ca.cyberengma.openodo.core.reminders.ReminderStatus
import ca.cyberengma.openodo.core.units.DistanceUnit
import ca.cyberengma.openodo.data.ExpenseRecordRepository
import ca.cyberengma.openodo.data.FuelEntryRepository
import ca.cyberengma.openodo.data.RecordTypeRepository
import ca.cyberengma.openodo.data.ReminderRepository
import ca.cyberengma.openodo.data.VehicleRepository
import ca.cyberengma.openodo.ui.VehicleValueFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun vehicles(): VehicleRepository
    fun reminders(): ReminderRepository
    fun fuels(): FuelEntryRepository
    fun expenses(): ExpenseRecordRepository
    fun recordTypes(): RecordTypeRepository
}

class ReminderCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext
        val notifications = NotificationManagerCompat.from(app)
        if (!notifications.areNotificationsEnabled()) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val entry = EntryPointAccessors.fromApplication(app, WorkerEntryPoint::class.java)
        val today = LocalDate.now()
        val vehicles = entry.vehicles().observeActive().first()
        val typeNames = entry.recordTypes().observeAll().first().associate { it.id to it.name }

        vehicles.forEach { vehicle ->
            val reminders = entry.reminders().observeForVehicle(vehicle.id).first()
            val fuels = entry.fuels().observeForVehicle(vehicle.id).first()
            val expenses = entry.expenses().observeForVehicle(vehicle.id).first()
            DueReminders.of(vehicle, reminders, fuels, expenses, today).forEach { due ->
                val typeName = typeNames[due.reminder.typeId] ?: "Service"
                post(notifications, vehicle, typeName, due.reminder.id, due.status)
            }
        }
        return Result.success()
    }

    private fun post(
        notifications: NotificationManagerCompat,
        vehicle: Vehicle,
        typeName: String,
        reminderId: Long,
        status: ReminderStatus,
    ) {
        val overdue = status.state == ReminderState.OVERDUE
        val parts = mutableListOf<String>()
        status.remainingDays?.let { days ->
            parts += if (days < 0) "${-days} days overdue" else "$days days left"
        }
        status.remainingDistance?.let { distance ->
            val formatted = VehicleValueFormatter.formatDistance(distance, vehicle.distanceUnit, Locale.getDefault())
            val symbol = distanceSymbol(vehicle.distanceUnit)
            parts += if (distance.value < 0) "$formatted $symbol overdue" else "$formatted $symbol left"
        }
        val text = parts.joinToString(" • ").ifBlank { "Maintenance due" }

        val title = if (overdue) "$typeName overdue" else "$typeName due soon"
        val icon = ca.cyberengma.openodo.R.drawable.ic_stat_reminder

        ensureChannel(notificationManager())
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${vehicle.name} — $text"))
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(if (overdue) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching { notifications.notify(reminderId.toInt(), notification) }
    }

    private fun notificationManager(): NotificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Maintenance reminders", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun distanceSymbol(unit: DistanceUnit): String = when (unit) {
        DistanceUnit.METRES -> "m"
        DistanceUnit.KILOMETRES -> "km"
        DistanceUnit.MILES -> "mi"
    }

    companion object {
        private const val CHANNEL_ID = "reminders"
    }
}