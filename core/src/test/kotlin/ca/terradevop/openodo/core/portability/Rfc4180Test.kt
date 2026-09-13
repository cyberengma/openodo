// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.portability

import ca.terradevop.openodo.core.portability.csv.Rfc4180
import java.io.StringReader
import java.io.StringWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class Rfc4180Test {
    @Test fun `quotes commas and embedded newlines round trip`() {
        val rows = sequenceOf(listOf("name", "note"), listOf("A, B", "line one\nline two\"quoted\""))
        val output = StringWriter()
        Rfc4180.write(rows, output)
        assertEquals(rows.toList(), Rfc4180.read(StringReader(output.toString())).toList())
    }

    @Test fun `corrupted quoted newline is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Rfc4180.read(StringReader("a,b\n\"unterminated\nvalue\n")).toList()
        }
    }

    @Test fun `crlf and lf are accepted`() {
        assertEquals(listOf(listOf("a", "b"), listOf("1", "2")), Rfc4180.read(StringReader("a,b\r\n1,2\n")).toList())
    }

    @Test fun `bom is ignored`() {
        assertEquals(listOf(listOf("a", "b")), Rfc4180.read(StringReader("\uFEFFa,b\n")).toList())
    }

    @Test fun `empty fields are retained`() {
        assertEquals(listOf(listOf("", "b", "")), Rfc4180.read(StringReader(",b,\n")).toList())
    }

    @Test fun `writer quotes only when required`() {
        val output = StringWriter()
        Rfc4180.write(sequenceOf(listOf("a", "plain")), output)
        assertTrue(output.toString().startsWith("a,plain"))
    }
}
