// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.model


enum class RecordCategory {
    SERVICE,
    REPAIR,
    UPGRADE,
    OTHER,
}

enum class FuelKind {
    LIQUID,
    ELECTRIC,
}

enum class PerformedBy {
    SELF,
    SHOP,
}
