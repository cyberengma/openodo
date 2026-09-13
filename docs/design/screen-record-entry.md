# Expense Record Entry

Fields: category (Service, Repair, Upgrade, Other), record type, date,
odometer, title, description, cost, Self/Shop, conditional shop name,
warranty-until, receipt, and notes.

The category/type choice is prominent because it drives list scanning and
reminder matching. Cost uses the active vehicle currency. A matching saved
record should explain that linked reminders were reset.

Blocking validation, odometer warning, receipt failure, save/cancel, delete,
and unsaved-change behavior must match `states.md`.
