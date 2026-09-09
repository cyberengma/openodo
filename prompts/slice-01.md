Follow the repo protocol: read docs/architecture.md (v0.1),
docs/slices/01-scaffold/brief.md and progress.md. Session plan first.
This session: scaffold both modules, declare and lock every dependency in the version catalog, get both warm-up commands green (online, then --offline).
Questions and propose-first items go to progress.md — no implementation
until ruled. Write your handoff to docs/slices/01-scaffold/handoffs/session-S.md
— the FULL path; a bare relative name lands at the repo root. Never state
that the gate will pass: state what you changed and why you believe it
will; the gate decides. Never touch protected paths.
Dependency changes end in the designed gate refusal — front-load them all
here and end cleanly on it. The gate image does not exist yet; that is
expected. Do not add any dependency to core beyond stdlib, serialization,
and test libs.
