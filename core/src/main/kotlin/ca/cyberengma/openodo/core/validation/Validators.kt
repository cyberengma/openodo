// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.validation

import ca.cyberengma.openodo.core.model.ExpenseRecord
import ca.cyberengma.openodo.core.model.FuelEntry
import ca.cyberengma.openodo.core.model.FuelKind
import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.units.Metres
import java.time.Clock
import java.time.LocalDate

object Validators {
    private fun fail(codes: List<ValidationErrorCode>, message: String): ValidationResult.Error =
        ValidationResult.Error(codes, message)

    /**
     * FuelEntry rules: exactly one positive measurement for the entry kind
     * (volume for LIQUID, energy for ELECTRIC); cost >= 0; date not after
     * today at the injected [clock].
     */
    fun fuelEntry(entry: FuelEntry, clock: Clock): ValidationResult {
        val volume = entry.volume
        val energy = entry.energy
        val codes = mutableListOf<ValidationErrorCode>()
        when {
            volume != null && energy != null ->
                codes.add(ValidationErrorCode.BOTH_MEASUREMENTS)
            entry.kind == FuelKind.LIQUID && volume == null ->
                codes.add(ValidationErrorCode.MISSING_MEASUREMENT)
            entry.kind == FuelKind.ELECTRIC && energy == null ->
                codes.add(ValidationErrorCode.MISSING_MEASUREMENT)
            volume != null && volume.value <= 0 ->
                codes.add(ValidationErrorCode.NON_POSITIVE_MEASUREMENT)
            energy != null && energy.value <= 0 ->
                codes.add(ValidationErrorCode.NON_POSITIVE_MEASUREMENT)
        }
        if (entry.totalCost.minor < 0) {
            codes.add(ValidationErrorCode.NEGATIVE_COST)
        }
        if (entry.date.isAfter(LocalDate.now(clock))) {
            codes.add(ValidationErrorCode.FUTURE_DATE)
        }
        if (codes.isEmpty()) {
            return ValidationResult.Ok
        }
        return fail(codes, "invalid fuel entry")
    }

    /** ExpenseRecord rules: cost >= 0. */
    fun expenseRecord(record: ExpenseRecord): ValidationResult {
        if (record.cost.minor < 0) {
            return fail(listOf(ValidationErrorCode.NEGATIVE_COST), "cost must not be negative")
        }
        return ValidationResult.Ok
    }

    /** Reminder rules: at least one interval set, each one positive. */
    fun reminder(reminder: Reminder): ValidationResult {
        val codes = mutableListOf<ValidationErrorCode>()
        val hasAny = reminder.intervalDistance != null || reminder.intervalMonths != null
        if (!hasAny) {
            codes.add(ValidationErrorCode.NO_INTERVAL)
        } else {
            if (reminder.intervalDistance != null && reminder.intervalDistance.value <= 0) {
                codes.add(ValidationErrorCode.NON_POSITIVE_INTERVAL)
            }
            if (reminder.intervalMonths != null && reminder.intervalMonths <= 0) {
                codes.add(ValidationErrorCode.NON_POSITIVE_INTERVAL)
            }
        }
        if (codes.isEmpty()) {
            return ValidationResult.Ok
        }
        return fail(codes, "invalid reminder")
    }

    /**
     * A proposed odometer lower than the previous maximum is a warning, not
     * an error (records may be entered out of sequence).
     */
    fun odometer(previousMax: Metres?, proposed: Metres): ValidationResult {
        if (previousMax != null && proposed.value < previousMax.value) {
            return ValidationResult.Warning(
                listOf(ValidationErrorCode.ODOMETER_DECREASED),
                "odometer ${proposed.value} m is below previous maximum ${previousMax.value} m",
            )
        }
        return ValidationResult.Ok
    }
}
