package ca.cyberengma.openodo.core.portability

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleBackupRoundTripTest {
    @Test
    fun `sample backup parses and contains generic seed data`() {
        val source = Files.readString(Path.of("../fixtures/sample-data/openodo-sample-backup.json"))
        val domain = BackupV1.read(source).getOrThrow()
        assertEquals(1, domain.vehicles.size)
        assertEquals(40, domain.recordTypes.size)
        assertEquals(12, domain.fuelEntries.size)
        assertEquals(8, domain.expenseRecords.size)
        assertEquals(4, domain.reminders.size)
        assertTrue(domain.fuelEntries.any { it.kind == ca.cyberengma.openodo.core.model.FuelKind.ELECTRIC })
        assertTrue(domain.expenseRecords.all { record -> record.lineItems.all { li -> domain.recordTypes.any { it.id == li.typeId } } })
    }
}