# UI Agent Request — OpenOdo Design Authority

## Assignment

Create the complete product design package for OpenOdo, a privacy-first,
offline Android vehicle log. Your output must define the screens, user
flows, navigation, reusable components, visual language, interaction states,
accessibility behavior, and responsive behavior required to implement Slices
06–10 without UI guesswork.

The output is design authority. It must be specific enough that an Android
Compose implementation agent can build the UI without inventing product
behavior.

## Required output location

The final approved design package will be copied into `docs/design/` in the
OpenOdo repository. Preserve the existing architecture and domain model.
Do not modify `docs/architecture.md`, existing slice briefs, fixtures, or
source code while producing the design package.

## Read first

- `docs/architecture.md`
- `docs/slices/01-scaffold/brief.md`
- `docs/slices/02-domain-units-money/brief.md`
- `docs/slices/03-fuel-engine/brief.md`
- `docs/slices/04-reminders-engine/brief.md`
- `docs/slices/05-portability/brief.md`
- `docs/design-request/PRODUCT_CONTEXT.md`

## Non-negotiable constraints

- Offline is the normal operating mode. Do not design accounts, login,
  cloud sync, server calls, online maps, analytics, tracking, ads, or remote
  services.
- Use Material 3 and Compose-compatible patterns.
- Do not add features outside the current product scope.
- Do not change the canonical domain model.
- Do not replace exact integer domain values with floating-point storage.
- Do not assume a network connection for any screen or flow.
- Do not add a dependency or write implementation code in this phase.
- Keep the UI suitable for phones first, with sensible large-screen behavior.
- Design both light and dark themes, dynamic text sizes, empty datasets, and
  local-storage error states.
- Distinguish blocking validation errors from non-blocking warnings.

## Core question your design must answer

For every screen: what does the user see, what can they do, what happens
when data is missing or invalid, how does the user navigate away, and how is
the behavior communicated accessibly?

## Required decisions

Resolve or explicitly escalate decisions about:

- Bottom navigation versus navigation rail.
- Dashboard information hierarchy.
- Vehicle switching placement and behavior.
- Whether add actions use a floating action button, top-bar action, or both.
- Form grouping and progressive disclosure.
- How liquid-fuel and electric-entry forms differ.
- How reminder urgency is represented without color alone.
- How import reports are presented before data is applied.
- How backup/restore confirmation and failure are presented.
- Chart alternatives for users who cannot interpret visual charts.

## Do not leave vague

Avoid statements such as “make it intuitive,” “show useful stats,” or “use
appropriate validation” without specifying the layout, copy intent, states,
actions, and resulting navigation.
