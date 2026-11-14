# ADR-0004: Use java.time in the public API; no custom GregorianDate

- Status: Proposed
- Date: 2026-11-14

## Decision

- The public API uses `java.time.LocalDate`, `Instant`, `Duration` and `Clock`.
- Internally, calculations use epoch days (`Long`) and Julian dates (`Double`).
- Android consumers below API 26 enable core-library desugaring. This is documented in the README.

## Alternatives

- **A custom `GregorianDate`:** duplicates a standard type and adds conversions for every caller.
- **kotlinx-datetime:** adds a dependency, and is only justified if we go multiplatform (Q8).

## Consequences

- A move to Kotlin Multiplatform later would need an expect/actual date type or kotlinx-datetime at the API edge. That is a breaking change, so Q8 must be decided before 1.0.
