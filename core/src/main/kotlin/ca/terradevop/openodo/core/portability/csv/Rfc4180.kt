// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.portability.csv

import java.io.Reader
import java.io.Writer

object Rfc4180 {
    fun read(reader: Reader): Sequence<List<String>> = sequence {
        val field = StringBuilder()
        val row = mutableListOf<String>()
        var quoted = false
        var first = true
        var next = reader.read()
        while (next >= 0) {
            val c = next.toChar()
            if (first && c == '\uFEFF') { first = false; next = reader.read(); continue }
            first = false
            when {
                quoted && c == '"' -> {
                    reader.mark(1)
                    val peek = reader.read()
                    if (peek == '"'.code) field.append('"') else { quoted = false; if (peek >= 0) reader.reset() }
                }
                !quoted && c == '"' && field.isEmpty() -> quoted = true
                !quoted && c == ',' -> { row += field.toString(); field.clear() }
                !quoted && c == '\n' -> { row += field.toString().removeSuffix("\r"); field.clear(); yield(row.toList()); row.clear() }
                else -> field.append(c)
            }
            next = reader.read()
        }
        require(!quoted) { "unterminated quoted field" }
        if (field.isNotEmpty() || row.isNotEmpty()) { row += field.toString(); yield(row.toList()) }
    }

    fun write(rows: Sequence<List<String>>, writer: Writer) {
        rows.forEach { row ->
            row.forEachIndexed { index, value ->
                if (index > 0) writer.write(','.code)
                val quote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
                if (quote) writer.write('"'.code)
                writer.write(value.replace("\"", "\"\""))
                if (quote) writer.write('"'.code)
            }
            writer.write("\r\n")
        }
    }
}
