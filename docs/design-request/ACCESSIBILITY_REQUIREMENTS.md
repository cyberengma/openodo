# Accessibility and Responsive Requirements

## Semantics

- Every interactive control has a meaningful accessible label.
- Icons that convey meaning have text alternatives.
- Decorative icons are hidden from accessibility services.
- Status is not communicated by color alone.
- Form errors are associated with their fields.
- Warnings are announced without trapping focus.
- Lists expose item identity and important values in a logical order.
- Charts provide a textual summary and data-table-like alternative.

## Visual accessibility

- Meet Material and platform contrast expectations in light and dark themes.
- Do not rely on red/green alone for reminder urgency.
- Use text, icons, labels, and ordering in addition to color.
- Define focus, pressed, selected, disabled, and error states.
- Ensure status badges remain legible at high contrast.

## Text and input

- Support large font scales without clipping or hidden actions.
- Long vehicle names, shop names, notes, and imported text must wrap safely.
- Numeric fields must identify units and currency.
- Date formats must be unambiguous and locale-aware at the display layer.
- Do not use placeholder text as the only field label.

## Touch and layout

- Interactive targets must be comfortably touchable.
- Important actions must remain reachable on narrow screens.
- Forms must work with the keyboard open and support scrolling to errors.
- Define phone portrait and landscape behavior.
- Define large-screen behavior where a two-pane layout improves usability.
- Do not hide essential actions behind swipe-only interactions.

## Motion and feedback

- Avoid motion that is required to understand status.
- Respect reduced-motion preferences where supported.
- Loading indicators must not block screen understanding unnecessarily.
- Confirm save, delete, import, and restore results with accessible feedback.
