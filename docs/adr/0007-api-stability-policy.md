# ADR-0007: API stability policy for the 0.x releases and the road to 1.0

- Status: Accepted (Sprint 7)
- Date: 2026-11-14

## Context

The first public release (`0.1.0-alpha01`) makes the API visible to other people. Some choices are
cheap to make now and breaking later. The Sprint 7 review of all six `.api` baselines found four.

## Decision

Applied before the first release:

1. **JVM default methods (`-Xjvm-default=all`)** for every module. Interfaces such as
   `ObservanceProvider` compile without `DefaultImpls`, so default methods can be added later
   without breaking callers, and Java sees ordinary interfaces.
2. **Exception constructors are internal.** Callers catch `InvalidMalayalamDateException` and
   `DateOutOfSupportedRangeException`; they don't construct them, so their fields can evolve.
3. **Compiler-generated Compose classes** are excluded from API tracking.

Policy for 0.x:

- Versions are `0.MINOR.PATCH`, with `-alphaNN`/`-betaNN` suffixes for previews. A minor release may break
  the API; the change is listed in `CHANGELOG.md` and visible in the `.api` diff of the pull request.
- **Value types stay Kotlin `data class`es during 0.x**: `MalayalamDate`, `MonthBoundary`,
  `CalendarConfiguration`, `GeoLocation`, `Festival`, `Observance`, `Span` and `PanchangamDay`.
  Their `copy` and `componentN` functions make adding a property a binary-incompatible change.
  This is acceptable before 1.0, and value semantics keep the API easy to use.
- `@KollavarshamAstronomyApi` (opt-in) marks the astronomy module as unstable until 1.0.

Criteria for 1.0.0:

1. The almanac evidence target from VALIDATION_STRATEGY.md is met: 60 or more cited month starts, including the borderline months.
2. Each data class above has been reviewed, and either kept with a documented "no new properties"
   promise, or replaced with a regular class with explicit `equals`/`hashCode`/`toString`.
3. The astronomy opt-in is removed or kept deliberately.
4. At least one external app (for example the planned Malayalam Calendar app) has used the API for a release cycle.

## Consequences

- Early users get a clear signal: 0.x is a preview, and breaks are documented.
- The binary-compatibility validator keeps every API change visible in review.
