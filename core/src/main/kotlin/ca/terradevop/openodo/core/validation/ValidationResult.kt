// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.validation

sealed class ValidationResult {
    data object Ok : ValidationResult()

    data class Warning(val codes: List<ValidationErrorCode>, val message: String) : ValidationResult()

    data class Error(val codes: List<ValidationErrorCode>, val message: String) : ValidationResult()
}
