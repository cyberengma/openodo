# Sleek Export Reference

The approved Sleek project is `OpenOdo Mobile`:

`https://sleek.design/project/KH6FVW4AnKr`

The React export was inspected from:

`/tmp/openodo-sleek-export/export-react/`

Screens included in the export:

- Vehicles
- Dashboard
- Fuel Entry
- Fuel History
- Expense Entry
- Reminders
- Statistics
- Settings
- Portability

The export is a visual reference for composition, hierarchy, spacing, card
shapes, tokens, and content density. It is not an implementation dependency.
It contains static sample values and mostly happy-path states. Compose agents
must follow `states.md`, `accessibility.md`, and the domain model when adding
runtime behavior.

Known translation rules:

- Tailwind utilities become Compose theme/spacing/component tokens.
- Iconify icons become bundled/vector Material-compatible icons.
- External Google Fonts become local/system or properly licensed packaged
  fonts; no runtime downloads.
- Static sample data becomes repository state and active-vehicle formatting.
- Web bottom navigation becomes Compose Navigation.
