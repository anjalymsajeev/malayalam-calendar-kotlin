# ADR-0003: Module structure

- Status: Proposed
- Date: 2026-11-14

## Decision

The SDK is split into these modules:

- `kollavarsham-astronomy`
- `kollavarsham-core`
- `kollavarsham-locale`
- `kollavarsham-panchangam`
- `kollavarsham-festivals`
- `kollavarsham-android`
- `sample-app`

The dependency graph and responsibilities are in ARCHITECTURE.md. Modules are created only in the sprint that first needs them. Sprint 1 creates `astronomy` (skeleton only), `core` and `locale`.

## Rationale

- **Astronomy as a separate module:** both core (sankranti, sunrise) and panchangam (Moon, tithi) need it.
- **Locale separate from core:** keeps strings and formatting out of the calculation engine, and lets server or CLI users skip it.
- **"data" renamed to "locale":** the old name implied persistence.

## Consequences

- One more published artifact.
- The public API of astronomy is opt-in (`@KollavarshamAstronomyApi`) until 1.0.
