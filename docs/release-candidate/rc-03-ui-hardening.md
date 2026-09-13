# RC-3 — UI and Design Hardening

## Goal

Bring every app surface into alignment with the approved Sleek-inspired
authority under `docs/design/`.

## Scope

- Replace remaining compressed/prototype composables with maintainable screen
  and component functions.
- Apply visual tokens from `docs/design/visual-system.md` consistently.
- Complete light and dark themes.
- Add loading, empty, validation, warning, database-error, import-error,
  cancellation, and unsaved-change states.
- Add destructive confirmation dialogs.
- Add accessibility labels, semantics, focus order, chart text alternatives,
  and non-color status treatment.
- Verify large text, keyboard scrolling, narrow-screen, and landscape layouts.
- Remove placeholder copy, stale sample values, dead actions, and fake
  statuses.
- Ensure statistics and portability are reachable from the shell.

## Acceptance

- Every screen maps to a file in `docs/design/`.
- No primary button has an empty callback.
- No screen depends on color alone for meaning.
- Dark theme and large-text checks pass.
- Visual review against the Sleek project finds no intentional mismatch in
  hierarchy, tokens, or vehicle-scoped formatting.
