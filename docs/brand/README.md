# OpenOdo Brand Package

This package defines the visual identity for OpenOdo, aligned with the
approved Sleek design authority (`docs/design/`).

## Logo

- Master asset: `docs/brand/logo.png` (1024×1024).
- Concept: a minimal odometer/speedometer gauge with an integrated fuel drop,
  rendered in flat Material You style. It communicates "vehicle" + "fuel" +
  "measured intervals" without text.
- The mark is geometry-only (no letters), so it reads clearly at 48 px and
  scales to store/social sizes.

## App icon

- `fastlane/metadata/android/en-US/images/icon.png` (512×512).
- Android launcher icons at every density under
  `app/src/main/res/mipmap-*/ic_launcher.png` and
  `ic_launcher_round.png`.

## Color palette

Derived from the Sleek visual system (`docs/design/visual-system.md`):

| Role | Hex | Use |
|---|---|---|
| Primary | `#2F6870` | Brand teal; primary actions, gauge, selected state |
| Background | `#F7F8F7` | App background |
| Foreground | `#1B1D1C` | Primary text |
| Secondary | `#E3ECEC` | Secondary surfaces/actions |
| Accent | `#DCEBED` | Informational surfaces |
| Destructive | `#B3261E` | Overdue/destructive state |
| Chart 1 | `#2F6870` | Teal series |
| Chart 2 | `#6F8F75` | Green series |
| Chart 3 | `#B98235` | Amber series |

## Typography

- Body/heading: a flexible sans family (Roboto Flex in the Sleek reference).
- Tabular values: monospaced style for odometer, cost, and economy figures.
- No runtime font downloads; use bundled/system fonts.

## Usage rules

- Do not recolor, stretch, rotate, or add effects to the logo.
- Do not place the logo on low-contrast or busy backgrounds.
- Use the teal primary for brand-forward surfaces; keep the rest calm and
  neutral.
- Status must never rely on color alone (see `docs/design/accessibility.md`).

## Licensing

- The logo and app icon are original project assets released under
  GPL-3.0-only with the application. Recorded in `docs/release/ASSETS.md`.
