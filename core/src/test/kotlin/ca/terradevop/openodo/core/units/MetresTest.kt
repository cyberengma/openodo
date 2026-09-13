// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.units

import org.junit.Assert.assertEquals
import org.junit.Test

class MetresTest {
    @Test
    fun `toKilometres returns whole kilometres`() {
        assertEquals(1_599L, Metres(1_599_000L).toKilometres())
    }

    @Test
    fun `of round trips through toKilometres`() {
        assertEquals(42_000L, Metres.of(42).toKilometres() * 1_000L)
        assertEquals(Metres(42_000_000L), Metres.of(42_000))
    }
}
