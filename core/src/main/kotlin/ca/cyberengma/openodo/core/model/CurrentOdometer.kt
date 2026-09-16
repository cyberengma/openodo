// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import ca.cyberengma.openodo.core.units.Metres
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class RecordedOdometer(val date: LocalDate, val odometer: Metres)

sealed class CurrentOdometerResult {
    data class Value(val odometer: Metres, val source: Source) : CurrentOdometerResult()

    enum class Source { RECORD, MANUAL }

    data object Empty : CurrentOdometerResult()
}

object CurrentOdometer {
    private val MANUAL_OVERRIDE_ZONE = ZoneOffset.UTC

    /**
     * Architecture rule: current odometer is the maximum over all recorded
     * odometers, unless the vehicle's manual override is newer than every
     * record. "Newer" compares whole days: the override's epoch-millis stamp
     * is mapped to a LocalDate in UTC (fixed zone, so the result never
     * depends on a user locale/time zone); a tie does not count as newer.
     *
     * Returns [Empty] when there is neither a record maximum nor an override
     * that stands alone (an override with a `null` stamp is inert).
     */
    fun of(vehicle: Vehicle, records: List<RecordedOdometer>): CurrentOdometerResult {
        val recordMax = records.maxByOrNull { it.odometer.value }?.odometer
        val override = vehicle.manualOdometer
        val overrideAt = vehicle.manualOdometerAt
        if (override != null && overrideAt != null) {
            val overrideDate = Instant.ofEpochMilli(overrideAt).atZone(MANUAL_OVERRIDE_ZONE).toLocalDate()
            if (records.all { it.date < overrideDate }) {
                return CurrentOdometerResult.Value(override, CurrentOdometerResult.Source.MANUAL)
            }
        }
        return if (recordMax != null) {
            CurrentOdometerResult.Value(recordMax, CurrentOdometerResult.Source.RECORD)
        } else {
            CurrentOdometerResult.Empty
        }
    }
}
