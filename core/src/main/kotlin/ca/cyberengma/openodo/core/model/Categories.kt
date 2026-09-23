// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.model


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

enum class FuelType {
    FUEL,
    ELECTRIC,
    HYBRID,
}

enum class PerformedBy {
    SELF,
    SHOP,
}
