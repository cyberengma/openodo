// SPDX-License-Identifier: GPL-3.0-only
package ca.cyberengma.openodo.core.portability

import ca.cyberengma.openodo.core.model.*
import ca.cyberengma.openodo.core.money.Money
import ca.cyberengma.openodo.core.money.UnitPrice
import ca.cyberengma.openodo.core.units.*
import java.time.LocalDate

internal fun sampleDomain() = PortabilityDomain(
    vehicles = listOf(Vehicle(1,"Africa","Honda","Africa Twin",2021,null,null,DistanceUnit.KILOMETRES,VolumeUnit.LITRES,EnergyUnit.KILOWATT_HOURS,"USD",Metres(40_000_000),1000,false,"note",1,2)),
    recordTypes = listOf(RecordType(2,RecordCategory.SERVICE,"oil change",true)),
    fuelEntries = listOf(FuelEntry(3,1,LocalDate.of(2026,1,2),Metres(40_000_000),FuelKind.LIQUID,Millilitres(15_000),null,UnitPrice(1_599,"USD"),Money(2_398,"USD"),"Gasoline",true,false,"Station",null,"note",1,2)),
    expenseRecords = listOf(ExpenseRecord(4,1,2,LocalDate.of(2026,1,3),Metres(40_100_000),"Oil",null,Money(5_000,"USD"),PerformedBy.SHOP,"Shop",null,null,null,1,2)),
    reminders = listOf(Reminder(5,1,2,Metres(10_000_000),6,LocalDate.of(2026,1,3),Metres(40_100_000),true)),
    photos = listOf("receipt.jpg"),
)
