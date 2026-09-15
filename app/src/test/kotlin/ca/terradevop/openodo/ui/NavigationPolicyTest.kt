// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationPolicyTest {
    @Test fun `empty install starts at vehicle setup`() {
        assertEquals("vehicles", initialAppRoute(hasVehicles = false))
    }

    @Test fun `existing install starts at dashboard`() {
        assertEquals("dashboard", initialAppRoute(hasVehicles = true))
    }
}
