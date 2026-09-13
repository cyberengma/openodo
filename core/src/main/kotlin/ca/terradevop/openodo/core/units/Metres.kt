// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.units

@JvmInline
value class Metres(val value: Long) {
    fun toKilometres(): Long = value / 1_000L

    companion object {
        fun of(kilometres: Long): Metres = Metres(kilometres * 1_000L)
    }
}
