// SPDX-License-Identifier: GPL-3.0-only
package ca.terradevop.openodo.core.portability.csv

import ca.terradevop.openodo.core.portability.PortabilityDomain
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
        yield(listOf("id","vehicle_id","type_id","date","odometer_m","title","cost_minor","currency","performed_by"))
        domain.expenseRecords.forEach { yield(listOf(it.id.toString(),it.vehicleId.toString(),it.typeId.toString(),it.date.toString(),it.odometer.value.toString(),it.title,it.cost.minor.toString(),it.cost.currency,it.performedBy.name)) }
    }, writer)
}
