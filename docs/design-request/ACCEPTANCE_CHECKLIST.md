# Design Package Acceptance Checklist

The package is ready for approval only when every item is satisfied.

## Scope and product

- [ ] Product purpose and target user are explicit.
- [ ] Offline/privacy constraints are explicit.
- [ ] No account, sync, network, analytics, or unsupported feature was added.
- [ ] Canonical units and currency behavior are documented.

## Screens

- [ ] Vehicle selection, empty state, creation, and editing.
- [ ] Dashboard.
- [ ] Fuel entry for liquid and electric.
- [ ] Fuel history and detail.
- [ ] Expense record entry/editing.
- [ ] Records list/detail.
- [ ] Reminder list.
- [ ] Reminder create/edit/detail.
- [ ] Statistics/trends.
- [ ] Settings.
- [ ] Backup/import/export flows.

## Behavior

- [ ] Every required screen has loading, empty, warning, and error states.
- [ ] Validation severity is explicit.
- [ ] Odometer decrease is treated as a warning.
- [ ] Stale odometer is shown without a new reminder state.
- [ ] Both reminder axes use whichever elapses first.
- [ ] Reminder reset and deletion re-anchoring are documented.
- [ ] Import report behavior is documented.
- [ ] Destructive actions have confirmation and reversibility decisions.

## Navigation and components

- [ ] Navigation map is complete.
- [ ] Back and unsaved-form behavior is defined.
- [ ] Shared components are inventoried.
- [ ] Component states and variants are documented.
- [ ] Screen-to-component usage is traceable.

## Accessibility and responsive behavior

- [ ] No state depends on color alone.
- [ ] Controls and fields have accessible labels.
- [ ] Form errors are associated with fields.
- [ ] Charts have textual alternatives.
- [ ] Large text and narrow screens are covered.
- [ ] Keyboard and scrolling behavior is documented.
- [ ] Light and dark themes are covered.

## Planning readiness

- [ ] Decisions are recorded.
- [ ] Remaining questions have owners and deadlines.
- [ ] Slice 06–10 briefs can be authored from the package without UI guesses.
- [ ] No source code or dependency changes are required to understand the design.
