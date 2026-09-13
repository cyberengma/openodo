// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.validation

import ca.terradevop.openodo.core.model.FuelEntry
import ca.terradevop.openodo.core.model.FuelKind
import ca.terradevop.openodo.core.money.Money
import ca.terradevop.openodo.core.units.Metres
import ca.terradevop.openodo.core.units.Millilitres
import ca.terradevop.openodo.core.units.WattHours
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class FuelEntryValidatorTest {
    private val clock = Clock.fixed(
        LocalDate.of(2026, 9, 13).atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneOffset.UTC,
    )

    @Test
    fun `liquid with positive volume is ok`() {
        assertTrue(Validators.fuelEntry(entry(volume = 60_000, kind = FuelKind.LIQUID, date = LocalDate.of(2026, 9, 1)), clock) is ValidationResult.Ok)
    }

    @Test
    fun `electric with positive energy is ok`() {
        assertTrue(Validators.fuelEntry(entry(volume = null, energy = 60_000, kind = FuelKind.ELECTRIC, date = LocalDate.of(2026, 9, 1)), clock) is ValidationResult.Ok)
    }

    @Test
    fun `date today is ok`() {
        assertTrue(Validators.fuelEntry(entry(date = LocalDate.of(2026, 9, 13)), clock) is ValidationResult.Ok)
    }

    @Test
    fun `liquid without volume is missing measurement`() {
        val result = Validators.fuelEntry(entry(volume = null, kind = FuelKind.LIQUID), clock)
        assertCodes(result, ValidationErrorCode.MISSING_MEASUREMENT)
    }

    @Test
    fun `electric without energy is missing measurement`() {
        val result = Validators.fuelEntry(entry(volume = null, energy = null, kind = FuelKind.ELECTRIC), clock)
        assertCodes(result, ValidationErrorCode.MISSING_MEASUREMENT)
    }

    @Test
    fun `both measurements is both measurements`() {
        val result = Validators.fuelEntry(entry(volume = 1_000, energy = 1_000, kind = FuelKind.LIQUID), clock)
        assertCodes(result, ValidationErrorCode.BOTH_MEASUREMENTS)
    }

    @Test
    fun `zero volume rejected`() {
        val result = Validators.fuelEntry(entry(volume = 0), clock)
        assertCodes(result, ValidationErrorCode.NON_POSITIVE_MEASUREMENT)
    }

    @Test
    fun `negative volume rejected`() {
        val result = Validators.fuelEntry(entry(volume = -5), clock)
        assertCodes(result, ValidationErrorCode.NON_POSITIVE_MEASUREMENT)
    }

    @Test
    fun `negative cost rejected`() {
        val result = Validators.fuelEntry(entry(total = -1), clock)
        assertCodes(result, ValidationErrorCode.NEGATIVE_COST)
    }

    @Test
    fun `zero cost accepted`() {
        assertTrue(Validators.fuelEntry(entry(total = 0), clock) is ValidationResult.Ok)
    }

    @Test
    fun `future date rejected`() {
        val result = Validators.fuelEntry(entry(date = LocalDate.of(2026, 9, 14)), clock)
        assertCodes(result, ValidationErrorCode.FUTURE_DATE)
    }

    @Test
    fun `all detected codes are reported`() {
        val result = Validators.fuelEntry(
            entry(volume = null, energy = null, kind = FuelKind.ELECTRIC, total = -1, date = LocalDate.of(2027, 1, 1)),
            clock,
        )
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(
            listOf(
                ValidationErrorCode.MISSING_MEASUREMENT,
                ValidationErrorCode.NEGATIVE_COST,
                ValidationErrorCode.FUTURE_DATE,
            ),
            (result as ValidationResult.Error).codes,
        )
    }

    private fun assertCodes(result: ValidationResult, expected: ValidationErrorCode) {
        assertTrue("expected Error, got $result", result is ValidationResult.Error)
        assertEquals(listOf(expected), (result as ValidationResult.Error).codes)
    }

    private fun entry(
        volume: Long? = 60_000,
        energy: Long? = null,
        kind: FuelKind = FuelKind.LIQUID,
        total: Long = 100_000,
        date: LocalDate = LocalDate.of(2026, 9, 1),
    ) = FuelEntry(
        vehicleId = 1L,
        date = date,
        odometer = Metres(150_000_000),
        kind = kind,
        volume = volume?.let(::Millilitres),
        energy = energy?.let(::WattHours),
        unitPrice = null,
        totalCost = Money(total, "CAD"),
        fuelLabel = "regular",
        fullTank = true,
        missedPreviousFillUp = false,
        stationName = null,
        receiptFileName = null,
        notes = null,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
