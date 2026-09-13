// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.portability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupV1Test {
    @Test fun `domain round trips losslessly`() {
        val domain = sampleDomain()
        val read = BackupV1.read(BackupV1.write(domain, 1_700_000_000_000, "0.1.0")).getOrThrow()
        assertEquals(domain, read)
    }

    @Test fun `unknown fields are ignored`() {
        val json = BackupV1.write(sampleDomain(), 1, "0.1.0").replaceFirst("{", "{\"futureField\":true,")
        assertEquals(sampleDomain(), BackupV1.read(json).getOrThrow())
    }

    @Test fun `schema version two is rejected`() {
        val json = BackupV1.write(PortabilityDomain(), 1, "0.1.0").replaceFirst("\"schemaVersion\":1", "\"schemaVersion\":2")
        assertTrue(BackupV1.read(json).isFailure)
    }

    @Test fun `golden fixture exists and has schema version`() {
        val golden = javaClass.getResourceAsStream("/backup/v1-golden.json")!!.bufferedReader().readText()
        assertTrue(golden.contains("\"schemaVersion\":1"))
    }
}
