# Sprint plan

| Sprint | Goal | Status |
|---|---|---|
| 0 | Discovery and architecture | **Awaiting approval** |
| 1 | Core SDK foundation | Planned (backlog below) |
| 2 | Calendar conversion engine and reference dataset | Planned |
| 3 | Localisation and developer experience | Planned |
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
