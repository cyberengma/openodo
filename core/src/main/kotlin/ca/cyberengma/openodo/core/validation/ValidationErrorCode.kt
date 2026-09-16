// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.validation

enum class ValidationErrorCode {
    MISSING_MEASUREMENT,
    BOTH_MEASUREMENTS,
    NON_POSITIVE_MEASUREMENT,
    NEGATIVE_COST,
    FUTURE_DATE,
    NO_INTERVAL,
    NON_POSITIVE_INTERVAL,
    NEGATIVE_DISTANCE,
    ODOMETER_DECREASED,
}
