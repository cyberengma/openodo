// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.reminders

import ca.cyberengma.openodo.core.model.Reminder
import ca.cyberengma.openodo.core.units.Metres
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetResolverTest {
    @Test fun `newer record resets matching reminder`() {
        val result = ResetResolver.onRecordLogged(
            listOf(reminder(anchorDate = TODAY.minusDays(10), anchorMetres = 100_000_000)),
            record(1, TODAY, 120_000_000),
        )
        assertEquals(TODAY, result.single().anchorDate)
        assertEquals(Metres(120_000_000), result.single().anchorOdometer)
    }

    @Test fun `older record does not move anchor backward`() {
        val current = reminder(anchorDate = TODAY, anchorMetres = 120_000_000)
        val result = ResetResolver.onRecordLogged(listOf(current), record(1, TODAY.minusDays(1), 130_000_000))
        assertEquals(TODAY, result.single().anchorDate)
        assertEquals(Metres(120_000_000), result.single().anchorOdometer)
    }

    @Test fun `same day higher odometer resets`() {
        val result = ResetResolver.onRecordLogged(
            listOf(reminder(anchorDate = TODAY, anchorMetres = 100_000_000)),
            record(1, TODAY, 100_000_001),
        )
        assertEquals(Metres(100_000_001), result.single().anchorOdometer)
    }

    @Test fun `same day equal odometer does not reset`() {
        val current = reminder(anchorDate = TODAY, anchorMetres = 100_000_000)
        assertEquals(current, ResetResolver.onRecordLogged(listOf(current), record(1, TODAY, 100_000_000)).single())
    }

    @Test fun `different vehicle is unchanged`() {
        val current = reminder()
        assertEquals(current, ResetResolver.onRecordLogged(listOf(current), record(1, TODAY, 200_000_000, vehicleId = 2)).single())
    }

    @Test fun `different type is unchanged`() {
        val current = reminder()
        assertEquals(current, ResetResolver.onRecordLogged(listOf(current), record(1, TODAY, 200_000_000, typeId = 99)).single())
    }

    @Test fun `unanchored reminder accepts first matching record`() {
        val result = ResetResolver.onRecordLogged(listOf(reminder(anchorDate = null, anchorMetres = null)), record(1, TODAY, 100_000_000))
        assertEquals(TODAY, result.single().anchorDate)
    }

    @Test fun `deletion reanchors to newest remaining record`() {
        val remaining = listOf(
            record(1, TODAY.minusDays(10), 100_000_000),
            record(2, TODAY.minusDays(2), 110_000_000),
            record(3, TODAY.minusDays(5), 120_000_000),
        )
        val result = ResetResolver.onRecordDeleted(listOf(reminder()), remaining[1], remaining.filter { it.id != 2L })
        assertEquals(TODAY.minusDays(5), result.single().anchorDate)
        assertEquals(Metres(120_000_000), result.single().anchorOdometer)
    }

    @Test fun `deletion with no remaining records clears anchor`() {
        val result = ResetResolver.onRecordDeleted(listOf(reminder()), record(1, TODAY, 100_000_000), emptyList())
        assertEquals(null, result.single().anchorDate)
        assertEquals(null, result.single().anchorOdometer)
    }

    @Test fun `deletion ignores different vehicle records`() {
        val current = reminder()
        val result = ResetResolver.onRecordDeleted(listOf(current), record(1, TODAY, 100_000_000), listOf(record(2, TODAY.plusDays(1), 200_000_000, vehicleId = 2)))
        assertEquals(null, result.single().anchorDate)
    }
}

class ManualResetTest {
    @Test fun `manual reset replaces both anchors`() {
        val result = ManualReset.apply(reminder(), TODAY.plusDays(1), Metres(222_000_000))
        assertEquals(TODAY.plusDays(1), result.anchorDate)
        assertEquals(Metres(222_000_000), result.anchorOdometer)
    }

    @Test fun `manual reset preserves configuration`() {
        val original = reminder(distance = 20_000_000, months = 12, active = true)
        val result = ManualReset.apply(original, TODAY, Metres(222_000_000))
        assertEquals(original.intervalDistance, result.intervalDistance)
        assertEquals(original.intervalMonths, result.intervalMonths)
        assertTrue(result.active)
    }
}
