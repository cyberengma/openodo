// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.portability.csv

import ca.cyberengma.openodo.core.portability.PortabilityDomain
import java.io.Writer

object CsvExport {
    fun vehicles(domain: PortabilityDomain, writer: Writer) = Rfc4180.write(sequence {
        yield(listOf("id","name","make","model","year","currency"))
        domain.vehicles.forEach { yield(listOf(it.id.toString(),it.name,it.make,it.model,it.year.toString(),it.currency)) }
    }, writer)

    fun fuelEntries(domain: PortabilityDomain, writer: Writer) = Rfc4180.write(sequence {
        yield(listOf("id","vehicle_id","date","odometer_m","kind","volume_ml","energy_wh","total_cost_minor","currency","fuel_label"))
        domain.fuelEntries.forEach { yield(listOf(it.id.toString(),it.vehicleId.toString(),it.date.toString(),it.odometer.value.toString(),it.kind.name,it.volume?.value?.toString() ?: "",it.energy?.value?.toString() ?: "",it.totalCost.minor.toString(),it.totalCost.currency,it.fuelLabel)) }
    }, writer)

    fun expenseRecords(domain: PortabilityDomain, writer: Writer) = Rfc4180.write(sequence {
        yield(listOf("id","vehicle_id","type_id","date","odometer_m","cost_minor","currency","performed_by"))
        domain.expenseRecords.forEach { record ->
            record.lineItems.forEach { item ->
                yield(listOf(record.id.toString(),record.vehicleId.toString(),item.typeId.toString(),record.date.toString(),record.odometer.value.toString(),item.cost.minor.toString(),item.cost.currency,record.performedBy.name))
            }
        }
    }, writer)
}
