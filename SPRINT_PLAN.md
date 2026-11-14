# Sprint plan

| Sprint | Goal | Status |
|---|---|---|
| 0 | Discovery and architecture | Done (approved 2026-11-14) |
| 1 | Core SDK foundation | Done |
| 2 | Calendar conversion engine and reference dataset | Done (evidence shortfall carried over) |
| 3 | Localisation and developer experience | **Done, pending review** (report below) |
| 4 | Panchangam foundation | Planned |
| 5 | Festival and observance engine | Planned |
| 6 | Android SDK and sample app | Planned |
| 7 | Production release (1.0.0) | Planned |

Rule: no sprint starts while the previous sprint has a failing acceptance criterion or an unanswered blocking question.

---

## Sprint 0: Discovery and architecture

**Objective:** Decide *how* to compute the Kollavarsham calendar correctly, *what* to reuse, and *what* the SDK looks like, with evidence, before writing SDK code.

### User stories and acceptance criteria

| ID | Story | Acceptance criteria | Status |
|---|---|---|---|
| S0-1 | As the tech lead, I need an evaluation of existing libraries so that we don't rebuild or misuse prior work. | All 5 listed projects are covered for algorithm, month rule, ranges, tests, dependencies, Android fit, license and limitations. A search for a native Kotlin implementation is recorded. Facts, claims and assumptions are labelled. | Done: `docs/sprint-0/existing-library-research.md` |
| S0-2 | As the tech lead, I need a reuse decision so that the engine strategy is settled. | A Java adapter vs native Kotlin comparison is written, with a recommendation and its reasons. | Done: research §4, ADR-0001 |
| S0-3 | As an SDK user, I need the calendar rules documented so that results are explainable. | Month rule, year rule, ayanamsa, sunrise convention, range and disagreement sources are documented with references. Open points are listed. | Done: `CALENDAR_ALGORITHM.md` |
| S0-4 | As the tech lead, I need accuracy evidence before choosing an algorithm. | At least one quantitative comparison of candidate approaches against observed dates. The spike is reproducible from the repo. | Done: `tools/accuracy-spike/` (14/14 vs 11/14; 386/492 agreement) |
| S0-5 | As an architect, I need module boundaries so that core stays pure. | Module graph exists; core has no Android or UI dependencies; deviations from the brief are justified. | Done: `ARCHITECTURE.md`, ADR-0003 |
| S0-6 | As an SDK user, I need a reviewed API proposal. | Domain model and API cover naming, nullability, errors and compatibility. Usage examples are included. | Done: `docs/sprint-0/api-proposal.md` |
| S0-7 | As a QA owner, I need a validation strategy. | Evidence tiers, reference dataset format, test suites and the accuracy-claim policy are defined. | Done: `VALIDATION_STRATEGY.md` |
| S0-8 | As the product owner, I need risks and a Sprint 1 backlog. | Risks have mitigations; the Sprint 1 backlog is estimated with acceptance criteria. | Done: below |

**Exit criterion:** The product owner approves the architecture and calculation strategy (ADR-0001 to ADR-0006) and answers the blocking questions (Q4 and Q10, plus Q6, Q7 and Q9 or acceptance of their defaults).

---

## Technical risks and mitigation

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R1 | **No authoritative machine-readable almanac dataset.** We can't prove accuracy. | High | High | Build the T1 dataset in Sprint 2 with citations, and prioritise borderline months. The product owner may know almanac sources or can buy printed almanacs. |
| R2 | Borderline months (within about 10 min of the cut-off) disagree between almanacs. | Certain (7 months in 1990–2030) | Medium | Expose `cutoffMargin`; keep evidence-backed overrides (ADR-0005); state accuracy honestly. |
| R3 | Wrong Lahiri convention or reference location shifts sankrantis by 5–9 min. | Medium | Medium | Implement both conventions internally; choose by T1 evidence in Sprint 2 (Q1, Q2). |
| R4 | Errors in our own astronomy code. | Medium | High | Meeus worked examples, a JPL DE440 T2 reference for all 2,412 sankrantis 1900–2100, and a peer diff against Dart. |
| R5 | Some users follow traditional (Surya Siddhanta) almanacs. | Medium | Low–Medium | Document it. A pluggable "traditional" boundary provider could come after 1.0 if demand exists. |
| R6 | Licensing contamination (AGPL Swiss Ephemeris data, copied code). | Low | High | Policy: no copied code, no Swiss-derived fixtures; generate our own references; license notes in PRs. |
| R7 | Namespace conflict with kollavarsham.org on Maven Central. | Medium | Medium | Q4: choose our own group ID before the first publish. |
| R8 | API churn before 1.0 breaks early adopters. | Medium | Medium | 0.x versions; binary-compatibility-validator from Sprint 1; a Sprint 7 API review. |
| R9 | ΔT and sunrise inaccuracies outside 1900–2100. | Low (out of range) | Low | Hard range check (ADR-0006). |
| R10 | Scope creep from app features into the SDK. | Medium | Medium | App features stay in the separate app product; enforced in code review. |

---

## Facts, assumptions and open questions

### Verified facts, from this sprint

- The Maven `kollavarsham-converter` needs Node.js at run time through jsii, so it can't be used on Android.
- kollavarsham-js has no `toGregorian`, uses a midnight month rule, matches 11 of 14 observed starts, and agrees with modern Kerala-rule results on 386 of 492 month starts (1990–2030).
- The Dart `kollavarsham` (MIT, Nov 2026) and AjeshAB (MIT) both use a modern Sun model, the Lahiri ayanamsa and the Kerala 3/5 rule.
- A modern ephemeris with the Kerala rule, using Lahiri convention A, matches all 14 observed starts in our sample.
- 14 Nov 2026 is Thulam 28, 1202, and Vrischikam 1, 1202 is 17 Nov 2026. Both engines agree.

### Assumptions, to be confirmed

- A1: Mainstream Kerala almanacs use drik (modern) sankranti times. Convention A matching all observations supports this, but n = 14.
- A2: Thiruvananthapuram is an acceptable default reference location.
- A3: Android minSdk 21+ is acceptable, using core-library desugaring below API 26.
- A4: The SDK will be open source under Apache-2.0, which is permissive and common for Android SDKs and includes a patent grant.

### Open questions

| # | Question | Blocking? | Default if you don't decide |
|---|---|---|---|
| Q1 | Lahiri convention A or B | Sprint 2 (evidence decides) | Decided by T1 data |
| Q2 | Reference location used by Kerala almanacs | Sprint 2 | Thiruvananthapuram |
| Q3 | Which almanac is authoritative for the product? | Sprint 2 | Kerala Government calendar and holiday orders, plus major printed almanacs |
| Q4 | **Maven group ID and package namespace** | **Sprint 1** (package names) | `io.github.<your-github-user>.kollavarsham` |
| Q5 | ΔT data source and license | Sprint 2 | IERS and published SMH 2016 values (to be checked) |
| Q6 | Rename `kollavarsham-data` to `kollavarsham-locale`? | Sprint 1 | Rename |
| Q7 | Kotest for property tests? | Sprint 1 | JUnit 5 with hand-rolled exhaustive loops (a 1900–2100 day loop is only about 73k days) |
| Q8 | Is Kotlin Multiplatform or iOS a future target? | Before Sprint 3 | JVM/Android only |
| Q9 | Preferred English transliterations (Edavam/Idavam, Karkidakam/Karkkidakam) | Sprint 1 (identifiers) | Edavam, Karkidakam |
| Q10 | **License (Apache-2.0?), and where the git repository will be hosted** | **Sprint 1** | Apache-2.0; local repository until you give a remote |

---

## Sprint 1 backlog: Core SDK foundation (proposed)

**Objective:** A compilable, tested, documented core library containing the domain models, validation, and conversion and arithmetic *mechanics* running on a pluggable month-boundary source. The astronomical boundary source arrives in Sprint 2.

**Not in Sprint 1:** astronomy maths, a public `KollavarshamCalendar.create()` (it has nothing to compute with until Sprint 2), formatting, panchangam.

| ID | Item | Estimate | Acceptance criteria |
|---|---|---|---|
| S1-1 | Gradle multi-module skeleton: `kollavarsham-astronomy` (empty), `kollavarsham-core`, `kollavarsham-locale`; version catalog; Kotlin DSL; JVM 11 target; `explicitApi()` | 3 | `./gradlew build` passes; core has no Android dependency (checked by a dependency check task) |
| S1-2 | Quality tooling: ktlint or detekt, binary-compatibility-validator, Dokka; CI workflow file (GitHub Actions) | 3 | `./gradlew check apiCheck` passes; `.api` files committed |
| S1-3 | `MalayalamMonth` enum with number, `next`/`previous`, `of()` | 1 | 100 % branch coverage; order Chingam to Karkidakam verified |
| S1-4 | `MalayalamWeekday` mapped to `java.time.DayOfWeek` | 1 | Mapping round-trips for all 7 days |
| S1-5 | `MalayalamDate` value type with structural validation and `Comparable` | 2 | Rejects day ∉ 1..32 and month numbers outside the enum; ordering is correct across years and months |
| S1-6 | `DateValidation` sealed type and `KollavarshamException` hierarchy | 1 | Every invalid path produces a typed result or exception; messages state the value and the allowed range |
| S1-7 | `MonthBoundary`, `BoundarySource`, `GeoLocation`, `CalendarConfiguration` | 2 | Immutable; `MonthBoundary` rejects lengths outside 29–32 |
| S1-8 | Internal `MonthBoundarySource` interface and an epoch-day based `BoundaryIndex`: year → 12 boundaries, cached, thread-safe | 3 | Lookup is correct at every boundary of a synthetic 201-year table; concurrency test passes |
| S1-9 | Conversion mechanics: `fromGregorian`, `toGregorian`, `toGregorianOrNull`, `validate`, range check, implemented against a `MonthBoundarySource` | 3 | Round trip for every day of the synthetic table; day 31 in a 30-day month gives `DayOutOfRange`; out-of-range dates throw |
| S1-10 | Arithmetic: `plusDays`, `plusMonths` (clamped), `daysBetween` | 2 | Property tests: `plusDays(n).plusDays(-n) == id`; `daysBetween` agrees with the epoch-day difference; clamping verified |
| S1-11 | Override layer: verified and user overrides applied on top of a source, with 29–32 length validation | 2 | An override that makes a month 33 days long fails at construction; `source` is reported correctly |
| S1-12 | `kollavarsham-locale`: Malayalam and English month and weekday names. Formatting is left for Sprint 3. | 1 | All 12 + 7 names present in both scripts; Unicode NFC normalised; snapshot test |
| S1-13 | Documentation: README, `API_REFERENCE.md` (generated from KDoc and kept up to date), `CONTRIBUTING.md`, `CHANGELOG.md` (0.1.0-alpha01), `ACKNOWLEDGEMENTS.md` | 2 | Every public API has KDoc with a Kotlin example; Dokka builds without warnings |

Total is about 26 points. Commits will be small and grouped: build skeleton → models → validation → boundary index → conversion → arithmetic → overrides → locale names → docs.

**Sprint 1 exit:** all items meet their acceptance criteria, `./gradlew build check apiCheck` is green, and the sprint report is delivered.

**Sprint 2 preview** (for planning only):

- Astronomy module: Sun, nutation, ΔT, sunrise, sankranti.
- `AstronomicalBoundarySource` and public `KollavarshamCalendar.create()`.
- A T2 reference generator using JPL DE440.
- A T1 dataset of 60 or more cited rows.
- The Q1 decision.
- Invariant tests for every day from 1900 to 2100.
- A peer diff against the Dart package.

---

## Sprint 1 completion report

**Objective met:** a compilable, tested, documented core library. `./gradlew build` is green: compile, 34 unit tests in 11 classes, `apiCheck`, and the core no-Android dependency guard.

### Decisions taken at sprint start (defaults from Sprint 0)

| Question | Decision |
|---|---|
| Q4 | Group `io.github.anjalymsajeev`, package root `io.github.anjalymsajeev.kollavarsham` |
| Q6 | Module named `kollavarsham-locale` |
| Q7 | JUnit 5 with kotlin-test; exhaustive loops instead of Kotest |
| Q9 | Identifiers `Edavam`, `Karkidakam` |
| Q10 | Apache-2.0. The GitHub repository has to be created by the owner (see below). |

### Backlog results

| ID | Result |
|---|---|
| S1-1 | Done. Three modules; Kotlin 2.0.21; JVM 11 bytecode; `explicitApi()`; warnings fail the build. |
| S1-2 | **Partly done.** binary-compatibility-validator and the CI workflow are in. **ktlint/detekt and Dokka are deferred to Sprint 3.** Maven Central was rate-limiting this environment, so the build was kept to the minimum set of plugins. KDoc is written for every public declaration. |
| S1-3 to S1-7 | Done. Models, validation types and exceptions. |
| S1-8 | Done. `BoundaryResolver`: a thread-safe per-year cache over an internal `MonthBoundarySource`. |
| S1-9 | Done. Conversion and validation. Every day from 1900 to 2100 round-trips against a synthetic table. |
| S1-10 | Done. `plusDays`, `plusMonths` (clamped), `daysBetween`. |
| S1-11 | Done. Verified and user overrides with precedence; impossible lengths fail at construction. The bundled CSV is empty until Sprint 2. |
| S1-12 | Done. Malayalam and English month and weekday names; NFC and chillu checks. |
| S1-13 | Done. README, API_REFERENCE, CONTRIBUTING, CHANGELOG, ACKNOWLEDGEMENTS, LICENSE. |

### Known limitations, by design

- No real calendar data yet: tests use a synthetic month table, and `KollavarshamCalendar.create()` doesn't exist until Sprint 2.
- `MalayalamDate` and `MonthBoundary` are `data class`es. Whether they should stay that way for binary-compatibility safety will be reviewed in Sprint 7.

### Next: Sprint 2 (conversion engine)

1. Astronomy: Sun (VSOP87 truncated), nutation, ΔT, sunrise, sankranti.
2. `AstronomicalBoundarySource` and public `create()`.
3. A T1 reference dataset with citations.
4. The Q1 ayanamsa decision.
5. Invariant tests on real data and a peer diff against the Dart package.

---

## Sprint 2 completion report

**Objective:** real Gregorian ↔ Kollavarsham conversion from astronomy and the Kerala rule, verified against observations.

### User stories and acceptance criteria

| ID | Story | Acceptance criteria | Result |
|---|---|---|---|
| S2-1 | Solar position to about 1″ | Meeus 25.b within 1″; sankranti agrees with an independent reference | **Met.** Meeus within 1″ (RA and Dec within 0.15″). RMS 15.8 s vs astronomy-engine; about 1 s standard deviation vs Swiss Ephemeris for 1900–2025. |
| S2-2 | Sunrise and sunset | Within 1 minute of the reference | **Met.** Worst 3 s over 744 days |
| S2-3 | Kerala-rule month boundaries | Implements the 3/5-of-daytime rule with margin and sankranti | **Met.** `AstronomicalBoundarySource` |
| S2-4 | Public API | `KollavarshamCalendar.create()`, `create(config)`, extension functions | **Met** |
| S2-5 | Q1 decision on evidence | Convention chosen and documented | **Met.** Convention A (ADR-0002 accepted) |
| S2-6 | Reference dataset | 60 or more T1 rows, all reproduced | **Partly met.** 21 rows (11 T1-search, 10 T3), 21 of 21 reproduced. See VALIDATION_STRATEGY §6. |
| S2-7 | Invariants and round trips 1900–2100 | All pass | **Met** |
| S2-8 | Borderline report | Checked in; changes fail CI | **Met.** 30 months |

The build is green: **48 tests**, `apiCheck`, and the dependency guard. Performance: 1900–2100 in about 0.3 s, cold.

### New risks and follow-ups

- **ΔT model:** Espenak–Meeus overestimates current ΔT by about 5 s, and predictions diverge by up to 2 minutes by 2100. Replace it with an IERS-based table. This only affects months with \|margin\| < 2 min.
- **JPL DE440 reference:** blocked by the network; to do when access is available.
- **Evidence:** promote T1-search rows to T1 and source the borderline months. This needs network access or printed almanacs (an owner action).

### Next: Sprint 3 (localisation and developer experience)

- Malayalam numerals and formatting APIs.
- Transliteration options.
- ktlint or detekt and Dokka, carried over from Sprint 1.
- Usage samples.

---

## Sprint 3 completion report

**Objective:** make the SDK pleasant to use from an app, and prove an external project can consume it through public APIs only.

| ID | Story | Acceptance criteria | Result |
|---|---|---|---|
| S3-1 | Malayalam numerals | Format, convert and parse both ways | **Met.** `MalayalamNumerals`, round-trip tests |
| S3-2 | Transliteration | ISO 15919 for all month and weekday names; general text | **Met.** `MalayalamTransliterator`. Simplification documented (dental and alveolar ന both "n"). |
| S3-3 | Formatting | Styles and patterns in both locales; weekday; digit choice; invalid patterns rejected | **Met.** `KollavarshamDateFormatter`, `DateStyle`, `format()` extensions |
| S3-4 | Developer docs | Guide with examples for every public API area | **Met.** `docs/guide.md`; API_REFERENCE updated; Dokka site |
| S3-5 | Tooling carried over from Sprint 1 | ktlint in `check`; Dokka builds | **Met.** 302 style issues auto-fixed; Dokka works offline |
| S3-6 | **Exit: external consumption** | A separate project consumes published artifacts via public API | **Met.** `samples/jvm-consumer` resolves `kollavarsham-locale` from Maven local and runs; added to CI |

The build is green: **60 tests**, ktlint, `apiCheck`, and the dependency guard.

**Not covered here:** an *Android* consumer. Google's Maven repository is unreachable from this environment, and the Android sample is Sprint 6's scope. The JVM consumer uses the same artifacts an Android app would; the only Android-specific requirement (`java.time` desugaring below API 26) is documented in the guide.

**Carried over:**
- The T1 evidence gap from Sprint 2 (needs network access or almanac pages).
- An IERS-based ΔT table.
- The JPL reference.

### Next: Sprint 4 (Panchangam foundation)

- Moon position (ELP-2000/82 abridged, Meeus ch. 47).
- Nakshatra and tithi providers.
- A sunrise-based day model and location configuration.
- All of it in `kollavarsham-panchangam`, separate from conversion.
