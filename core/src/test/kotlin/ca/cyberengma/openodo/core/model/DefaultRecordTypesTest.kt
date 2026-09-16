// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultRecordTypesTest {
    @Test
    fun `category counts`() {
        assertEquals(14, count(RecordCategory.SERVICE))
        assertEquals(8, count(RecordCategory.REPAIR))
        assertEquals(9, count(RecordCategory.UPGRADE))
        assertEquals(9, count(RecordCategory.OTHER))
        assertEquals(40, DefaultRecordTypes.all.size)
    }

    @Test
    fun `every entry is a default`() {
        assertTrue(DefaultRecordTypes.all.all { it.isDefault })
    }

    @Test
    fun `names unique within category`() {
        val names = DefaultRecordTypes.all
            .groupBy { it.category to it.name }
        assertTrue(names.values.all { it.size == 1 })
    }

    private fun count(category: RecordCategory): Int =
        DefaultRecordTypes.all.count { it.category == category }
}
