// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.money

object CurrencyMinorDigits {
    private val table = mapOf(
        "JPY" to 0,
        "KRW" to 0,
        "KWD" to 3,
        "BHD" to 3,
    )

    private const val DEFAULT = 2

    fun of(code: String): Int = table[code] ?: DEFAULT
}
