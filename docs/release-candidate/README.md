# Release Candidate Plan

The original product slices 01–10 establish the architecture and first-pass
implementation. The following release-candidate slices harden that work into
an installable, testable, release-shaped app.

## Sequence

| RC slice | Name | Depends on |
|---|---|---|
| RC-1 | Functional completion | 01–09 |
| RC-2 | Backup/import/export UI | RC-1, Slice 05 |
| RC-3 | UI and design hardening | RC-1, RC-2, `docs/design/` |
| RC-4 | Test and stability hardening | RC-1–3 |
| RC-5 | Release packaging | RC-4, operator signing decisions |
| RC-6 | Release-candidate verification | RC-5 |

Start with `rc-01-functional-completion.md`. Do not skip ahead to release
signing while user-facing workflows are still placeholders.

## Working rules

- Build locally only; do not use terra228.
- Work only in `/home/enigma/PDev/openodo`.
- No remote Git sync except the approved local-to-`origin/main` push at the
  end of a completed slice.
- Do not add dependencies without recording a ruling and rebuilding the
  locked catalog.
- Keep canonical storage exact: metres, millilitres, watt-hours, integer
  minor money, and milli-unit prices.
- Preserve vehicle-scoped distance, volume, energy, and currency settings.
- Run the offline gate at every slice boundary.
