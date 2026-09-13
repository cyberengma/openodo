# Visual System

The approved Sleek direction is the visual reference for Compose implementation.
The React export is at `/tmp/openodo-sleek-export/export-react/` on the design
machine; it is reference material, not production Android code.

## Tokens

Light-theme starting tokens:

| Token | Value | Use |
|---|---|---|
| background | `#F7F8F7` | App background |
| foreground | `#1B1D1C` | Primary text |
| primary | `#2F6870` | Primary actions and selected state |
| primary foreground | `#FFFFFF` | Text on primary |
| secondary | `#E3ECEC` | Secondary surfaces/actions |
| secondary foreground | `#23494E` | Text on secondary |
| muted | `#E7EAE8` | Muted surfaces |
| muted foreground | `#687371` | Supporting text |
| accent | `#DCEBED` | Informational/accent surfaces |
| accent foreground | `#24535A` | Text on accent |
| destructive | `#B3261E` | Destructive/overdue state |
| card | `#FFFFFF` | Raised content surfaces |
| border | `#D8DEDC` | Dividers and outlines |
| input | `#F0F3F1` | Form field surfaces |
| focus ring | `#5B9298` | Keyboard/focus indication |

Chart colors are teal `#2F6870`, green `#6F8F75`, amber `#B98235`, red
`#9B514C`, and purple `#7A6A96`. Charts must not rely on these colors alone;
labels, patterns, order, or text values are required.

Typography uses a flexible sans family for body and headings, a serif only
where a future branded display treatment is justified, and a monospaced
style for tabular values. Compose may use the closest locally available
licensed/system font rather than downloading fonts at runtime.

Base radius is approximately `14dp`; use larger radii for grouped cards and
smaller radii for chips and compact controls. Keep borders subtle and avoid
heavy shadows.

## Theme rules

- Define equivalent dark-theme tokens; do not simply invert light colors.
- Preserve contrast and status distinctions in both themes.
- Dynamic color may be supported only as an optional enhancement; the
  OpenOdo semantic roles must remain recognizable.
- Use the active vehicle's units and currency in every metric/formatted value.
- Never copy web Tailwind classes, CDN dependencies, external font URLs, or
  Iconify runtime code into the Android app.
