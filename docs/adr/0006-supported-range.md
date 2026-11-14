# ADR-0006: Initial supported range is 1900–2100 CE (ME 1075–1276)

- Status: Proposed
- Date: 2026-11-14

## Decision

- Conversions are supported for Gregorian dates from 1900-01-01 to 2100-12-31.
- Dates outside the range throw `DateOutOfSupportedRangeException`, and `...OrNull` returns null.

## Rationale

- ΔT is well known in this range.
- An independent ephemeris reference exists for it.
- It covers every realistic consumer-app use: birth dates, events and planning.

## Consequences

Extending the range for historical dates needs a new ADR that covers:

- ΔT uncertainty.
- Julian-calendar handling for dates before 1582.
- The accuracy we can claim.
