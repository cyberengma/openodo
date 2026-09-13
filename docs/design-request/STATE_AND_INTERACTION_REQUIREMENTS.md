# State and Interaction Requirements

## Required state matrix

Every data-driven screen must document:

- Loading.
- Loaded with data.
- Loaded with no data.
- Blocking validation error.
- Non-blocking warning.
- Local-storage/database error.
- User cancellation.
- Long-content behavior.
- Offline behavior.

## Validation severity

Blocking errors prevent save and identify the field and correction. Warnings
allow save after acknowledgment or remain visible without blocking when the
domain permits it.

Examples:

- Missing fuel measurement: blocking.
- Both volume and energy supplied: blocking.
- Non-positive fuel measurement: blocking.
- Negative cost: blocking.
- Future date: blocking.
- Odometer decrease: warning, not blocking.
- Stale odometer: warning/status metadata, not a new reminder state.

## Navigation

Document:

- Start destination.
- Vehicle selection and switching.
- Dashboard quick-action destinations.
- Back behavior from every form.
- Unsaved form exit confirmation.
- Save success destination.
- Delete success destination.
- Import/restore confirmation and result destination.
- State preservation when moving between tabs or destinations.

## Reminder interaction rules

- Both intervals use whichever elapses first.
- A reminder without an anchor is explicitly unanchored.
- Logging a matching expense resets the anchor.
- Older matching records never move an anchor backward.
- Deleting the latest matching record reanchors to the newest remaining
  matching record or clears the anchor.
- Manual reset changes the anchor without changing interval configuration.

## Import and backup interactions

- Show a preview or confirmation before applying imported data.
- Show imported, warning, and skipped counts.
- Preserve report codes and explain skipped rows.
- Never silently discard malformed rows.
- Treat backup schema errors as actionable local errors.
- Handle file picker cancellation without losing existing data.
- Do not imply cloud backup or upload.

## Destructive actions

Require confirmation for:

- Vehicle archive/delete.
- Fuel entry delete.
- Expense record delete.
- Reminder delete.
- Backup restore that replaces or merges local data.

The design must state whether the action is reversible. Destructive buttons
must not be distinguished by color alone.
