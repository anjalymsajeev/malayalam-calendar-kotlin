# ADR-0005: Verified almanac overrides are data, not code

- Status: Proposed
- Date: 2026-11-14

## Context

- At borderline months, almanacs can start a month a day differently from the astronomical rule.
- The Dart package hard-codes two such corrections.
- The brief forbids hard-coded current-year data in the calculation engine.

## Decision

- The engine is purely computational.
- Overrides are a separate, versioned resource, `verified-overrides.csv` in core resources, loaded once. Every row must cite T1 evidence.
- Callers can disable the bundled overrides (`applyVerifiedOverrides = false`) and add their own (`monthStartOverrides`).
- Each override must keep all affected months within 29–32 days. A violation fails at construction.
- `MonthBoundary.source` reports which source decided each boundary.

## Consequences

- The behaviour is transparent and auditable.
- Adding an override is a reviewed data change that needs evidence, released as a patch version.
