# CSV Format

OpenOdo CSV exports use RFC-4180 quoting, CRLF row endings, a header row,
and a period as the decimal separator. Values are UTF-8. Display-unit
columns are selected by the caller; canonical storage remains metres,
millilitres, watt-hours, and minor currency units.

The exported entity headers are:

- Vehicles: `id,name,make,model,year,currency`
- Fuel entries: `id,vehicle_id,date,odometer_m,kind,volume_ml,energy_wh,total_cost_minor,currency,fuel_label`
- Expense records: `id,vehicle_id,type_id,date,odometer_m,title,cost_minor,currency,performed_by`

Fields containing commas, quotes, CR, or LF are enclosed in double quotes;
embedded quotes are doubled.
