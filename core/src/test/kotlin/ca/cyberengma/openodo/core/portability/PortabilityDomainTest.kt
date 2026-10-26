// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.portability

import org.junit.Assert.assertEquals
import org.junit.Test

class PortabilityDomainTest {
    @Test fun `withCurrency rewrites vehicle and money without changing values`() {
        val domain = sampleDomain()
        val gbp = domain.withCurrency("gbp")
        assertEquals("GBP", gbp.vehicles.first().currency)
        assertEquals(2_398L, gbp.fuelEntries.first().totalCost.minor)
        assertEquals("GBP", gbp.fuelEntries.first().totalCost.currency)
        assertEquals(1_599L, gbp.fuelEntries.first().unitPrice!!.milli)
        assertEquals("GBP", gbp.fuelEntries.first().unitPrice!!.currency)
        assertEquals(5_000L, gbp.expenseRecords.first().lineItems.first().cost.minor)
        assertEquals("GBP", gbp.expenseRecords.first().lineItems.first().cost.currency)
    }
}
