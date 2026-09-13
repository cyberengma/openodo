// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.model

import ca.terradevop.openodo.core.units.DistanceUnit
import ca.terradevop.openodo.core.units.EnergyUnit
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class CurrentOdometerTest {
    @Test
    fun `maximum over records`() {
        val vehicle = vehicle(manual = null, at = null)
        val records = listOf(
            RecordedOdometer(LocalDate.of(2026, 1, 1), Metres(10_000_000)),
            RecordedOdometer(LocalDate.of(2026, 5, 1), Metres(20_000_000)),
            RecordedOdometer(LocalDate.of(2026, 2, 1), Metres(15_000_000)),
        )
        val result = CurrentOdometer.of(vehicle, records)
        assertEquals(
            CurrentOdometerResult.Value(Metres(20_000_000), CurrentOdometerResult.Source.RECORD),
            result,
        )
    }

    @Test
    fun `manual override newer than every record wins`() {
        val vehicle = vehicle(manual = Metres(30_000_000), at = utc(2026, 9, 1))
        val records = listOf(
            RecordedOdometer(LocalDate.of(2026, 1, 1), Metres(10_000_000)),
            RecordedOdometer(LocalDate.of(2026, 8, 1), Metres(20_000_000)),
        )
        val result = CurrentOdometer.of(vehicle, records)
        assertEquals(
            CurrentOdometerResult.Value(Metres(30_000_000), CurrentOdometerResult.Source.MANUAL),
            result,
        )
    }

    @Test
    fun `manual override tied with newest record day does not win`() {
        val vehicle = vehicle(manual = Metres(30_000_000), at = utc(2026, 8, 1))
        val records = listOf(
            RecordedOdometer(LocalDate.of(2026, 1, 1), Metres(10_000_000)),
            RecordedOdometer(LocalDate.of(2026, 8, 1), Metres(20_000_000)),
        )
        val result = CurrentOdometer.of(vehicle, records)
        assertEquals(
            CurrentOdometerResult.Value(Metres(20_000_000), CurrentOdometerResult.Source.RECORD),
            result,
        )
    }

    @Test
    fun `manual override older than newest record does not win`() {
        val vehicle = vehicle(manual = Metres(30_000_000), at = utc(2025, 1, 1))
        val records = listOf(
            RecordedOdometer(LocalDate.of(2026, 1, 1), Metres(10_000_000)),
            RecordedOdometer(LocalDate.of(2026, 8, 1), Metres(20_000_000)),
        )
        val result = CurrentOdometer.of(vehicle, records)
        assertEquals(
            CurrentOdometerResult.Value(Metres(20_000_000), CurrentOdometerResult.Source.RECORD),
            result,
        )
    }

    @Test
    fun `override without stamp is inert`() {
        val vehicle = vehicle(manual = Metres(30_000_000), at = null)
        val records = listOf(RecordedOdometer(LocalDate.of(2026, 1, 1), Metres(10_000_000)))
        val result = CurrentOdometer.of(vehicle, records)
        assertEquals(
            CurrentOdometerResult.Value(Metres(10_000_000), CurrentOdometerResult.Source.RECORD),
            result,
        )
    }

    @Test
    fun `override alone stands`() {
        val vehicle = vehicle(manual = Metres(7_777_000), at = utc(2026, 9, 1))
        val result = CurrentOdometer.of(vehicle, emptyList())
        assertEquals(
            CurrentOdometerResult.Value(Metres(7_777_000), CurrentOdometerResult.Source.MANUAL),
            result,
        )
    }

    @Test
    fun `nothing at all is empty`() {
        val vehicle = vehicle(manual = null, at = null)
        assertEquals(CurrentOdometerResult.Empty, CurrentOdometer.of(vehicle, emptyList()))
    }

    private fun utc(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun vehicle(manual: Metres?, at: Long?) = Vehicle(
        name = "Pickup",
        make = "Ford",
        model = "F-250",
        year = 2019,
        vin = null,
        photoFileName = null,
        distanceUnit = DistanceUnit.KILOMETRES,
        volumeUnit = VolumeUnit.LITRES,
        energyUnit = EnergyUnit.KILOWATT_HOURS,
        currency = "CAD",
        manualOdometer = manual,
        manualOdometerAt = at,
        isArchived = false,
        notes = null,
        createdAt = Instant.EPOCH.toEpochMilli(),
        updatedAt = Instant.EPOCH.toEpochMilli(),
    )
}
