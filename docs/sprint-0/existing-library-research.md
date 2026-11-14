# Existing-library research report (Sprint 0)

Status: **Draft for approval** · Date: 2026-11-14

Labels used throughout:

- **[FACT]**: verified in this sprint by reading source code, running code, or reading package metadata.
- **[CLAIM]**: stated by a third party; not independently verified.
- **[ASSUMPTION]**: our working belief; needs confirmation.
- **[OPEN]**: unresolved question.

## 1. Candidates evaluated

| # | Project | Language / form | License | Last activity | Malayalam conversion |
|---|---|---|---|---|---|
| 1 | [kollavarsham/kollavarsham-js](https://github.com/kollavarsham/kollavarsham-js) | TypeScript; npm `kollavarsham` 2.5.4 | MIT (© 2014–2025 The Kollavarsham Team) | Jan 2025 | Gregorian → ME only |
| 2 | [org.kollavarsham.converter:kollavarsham-converter](https://central.sonatype.com/artifact/org.kollavarsham.converter/kollavarsham-converter) | Java (jsii-generated from #1) | MIT | follows #1 | same as #1 |
| 3 | [kollavarsham/calendar](https://github.com/kollavarsham/calendar) | AngularJS 1.x web app | MIT | Jan 2026 | none locally (calls a remote `calendar-api` over HTTP) |
| 4 | [AjeshAB/malayalam-calendar](https://github.com/AjeshAB/malayalam-calendar) | Node.js/Express web app | MIT (© 2026 Ajesh AB) | Aug 2026 | both directions, inside an app |
| 5 | [pub.dev `kollavarsham`](https://pub.dev/packages/kollavarsham) ([midhunarmid/kollavarsham](https://github.com/midhunarmid/kollavarsham)) | Pure Dart package, plus Flutter app | MIT (© 2026 Midhun Mohanan) | 3 Nov 2026 (v1.2.1) | both directions |
| – | Native Kotlin implementation | – | – | – | **none found** [FACT for public GitHub, pub and Maven searches; absence can't be proven] |

## 2. Findings per project

### 2.1 kollavarsham-js (#1)

- **Algorithm [FACT]:** A port of the Perl `pancanga` program, based on Sewell & Dikshit, with a fix credited to Yano.
  - Planetary positions use the **Surya Siddhanta** (or optionally the *Pañcasiddhāntikā*) mean-motion and manda-equation model (`lib/celestial`), not a modern ephemeris.
  - There is no ayanamsa: the model is inherently sidereal.
- **Month start [FACT]:** `isTodaySauraMasaFirst` makes a day the first of the month when the solar longitude crosses a multiple of 30° between that day's local 00:00 and 24:00 (after a *desantara* correction from Ujjain). That is a **midnight rule**. It is not the Kerala 3/5-of-daytime rule (see `CALENDAR_ALGORITHM.md` §2).
- **Year [FACT]:** ME year = Saka year − 747 + a correction (Sewell p.45), applied in `SakaDate.generateKollavarshamDate()`, which carries a `TODO: Add unit tests`.
- **Kollavarsham → Gregorian [FACT]:** `toGregorianDate()` **throws "not implemented"**.
- **Tests [FACT]:** There are 1,646 lines of tests, but they are mostly unit tests of internal maths. The end-to-end conversion tests cover **three Gregorian dates** (1979-05-22, 1983-09-07, 1983-09-21), and none is traced to an external almanac.
- **API shape [FACT]:**
  - The result object is mutable and stringly-typed; month names are padded with trailing spaces (`'Idavam    '`).
  - The month name is spelled `Karkitakam`.
  - `Calendar` uses a module-level mutable `samkranti` object and caches conjunction state on the `Celestial` instance, so **it isn't thread-safe**.
- **Default location [FACT]:** Ujjain (23.2° N, 75.8° E), not Kerala.
- **Measured accuracy [FACT, this sprint]:** see §3. It matches 11 of 14 observed Kerala month starts, and its month-start dates differ from a modern computation in **106 of 492 months** (1990–2030), almost always one day early.

### 2.2 Maven `kollavarsham-converter` (#2)

- **[FACT]** The POM depends on `software.amazon.jsii:jsii-runtime` (Apache-2.0).
  - jsii's Java runtime runs the original JavaScript in a **child Node.js process** and talks to it over a pipe.
  - It needs a `node` executable at run time, which **does not exist on Android**.
  - It also adds process start-up cost and Jackson and other transitive dependencies on the JVM.
- **[FACT]** Because it is generated from #1, it has every algorithmic limitation of #1, including the missing `toGregorian`.
- **Conclusion:** It **cannot be used as an Android calculation adapter.** It could only be used on a server JVM, and even there it wouldn't be the right choice.

### 2.3 kollavarsham/calendar (#3)

- **[FACT]** A UI over the remote `https://api.aws.kollavarsham.org/` service, with no conversion logic of its own. That breaks the offline-first requirement.
- **Useful for:** UI and UX ideas only, for example month and year pickers and bilingual switching. It has no reusable calculation value.

### 2.4 AjeshAB/malayalam-calendar (#4)

- **Algorithm [FACT]:** Modern astronomy: perturbation-series Sun and Moon ("NASA/Moshier-style"), Lahiri ayanamsa, an iterative sunrise. The month rule is a sankranti before **sunrise + 0.6 × (sunset − sunrise)**, which is the Kerala rule.
- **Structure [FACT]:** A 1,421-line `panchang-engine.js` mixes astronomy, the calendar, festivals and UI-oriented strings.
  - There are in-code comments working out the year rule by trial (`"INCORRECT" … "CORRECT"`).
  - The app also depends on an LLM (Gemini) for horoscope prose. That is irrelevant to us, and it is a reason not to take the engine wholesale.
- **Tests [FACT]:** Tests cover nazhika, nakshatra, observances and horoscope. **No test covers Kollavarsham month boundaries.**
- **Useful for:** Its observance rules (sunrise-tithi, pradosha window, 6-nazhika nakshatra) are a reference for Sprint 4–5. We should not port the code.

### 2.5 Dart `kollavarsham` (#5)

The strongest reference found.

- **Algorithm [FACT, read in code and docs]:**
  - Sun: a truncated VSOP87 (Meeus ch. 32) with nutation and aberration.
  - ΔT: an IERS table for 1900–2100.
  - Ayanamsa: Lahiri, in a convention calibrated to drikpanchang.
  - Sunrise: upper limb with −0.833° refraction.
  - Month rule: the Kerala 3/5 rule at Thiruvananthapuram by default.
  - It supports the Kanni year start (North Malabar) and ME 1–2000.
- **Validation [CLAIM, partly checked]:**
  - It claims all 20 drikpanchang sankrantis of 2024–25 to within 1 minute, and 2,412 sankrantis from 1900–2100 to within 1 minute of Swiss Ephemeris.
  - It lists 31 borderline months (within 10 minutes of the cut-off) between 1900 and 2100.
  - **[FACT]** Our independent spike reproduces its sankranti times and margins for the two months we checked to within a minute (§3).
- **Honesty about limits [FACT]:** Two months are hand-corrected with `verifiedMonthStarts` (Vrischikam 1197, Chingam 1199), because the computed sankranti fell 0.7 and 3.7 minutes *after* the cut-off while the almanacs started the month on the sankranti day.
- **Performance [FACT]:** For the default location it ships a generated month-length table for ME 1075–1276, one character per month.
- **Fixture provenance [FACT]:** `sankranti_1900_2100.csv` was generated with the Swiss Ephemeris, which is AGPL or commercially licensed (see §5).
- **Useful for:**
  - Algorithm design confirmation.
  - The list of borderline months.
  - Observed-date test cases with their sources.
  - API ideas: `monthStartMargin`, overrides, `YearStart`.
- **Re-use form:** MIT allows porting with attribution, but we will **implement from primary sources** (Meeus, Sewell & Dikshit) and use #5 only as an *independent cross-check*. That keeps our validation genuinely independent (see ADR-0001).

## 3. Accuracy spike (run this sprint)

A throwaway Node.js script, `tools/accuracy-spike/compare.js`, computed every month start from 1990 to 2030 two ways and compared both with observed Kerala dates.

- **Modern engine:** `astronomy-engine` (MIT, VSOP87) for the Sun, the Lahiri ayanamsa, and the Kerala 3/5 rule at Thiruvananthapuram.
- **kollavarsham-js:** run with Thiruvananthapuram coordinates.

Two ayanamsa conventions were tried:

- **A:** the 1956 Lahiri definition (23°15′00.658″ at 1956-03-21 TT), carried by IAU 2006 precession, plus nutation in longitude.
- **B:** the Dart package's drikpanchang-calibrated convention (23.863092° at J2000, no nutation term).

| Measure | Modern A | Modern B | kollavarsham-js |
|---|---|---|---|
| Observed Kerala month starts matched (n = 14, list in `VALIDATION_STRATEGY.md`) | **14 / 14** | 12 / 14 | 11 / 14 |
| Agreement with Modern A, month starts 1990–2030 (n = 492) | – | 489 / 492 | 386 / 492 (78.5 %) |
| Direction of disagreements vs Modern A | – | 3 × one day late (Thulam 1197, Vrischikam 1197, Chingam 1199) | 92 × one day **early**, 14 × one day late |
| Disagreements with the sankranti within 15 min of the cut-off | – | 3 of 3 | 6 of 106 |

What the numbers show:

1. **kollavarsham-js is systematically off, not just noisy at the edges.**
   - About 21 % of its month starts differ from a modern Kerala-rule computation, and only 6 of those 106 are borderline.
   - The likely causes are its midnight rule and the Surya Siddhanta solar model.
   - The misses against observed dates are Chingam 1199, Medam 1198 and Medam 1199.
2. **The modern approach plus the Kerala rule reproduces every observed date** in our small sample. Conventions A and B differ only on the two borderline months.
   - Under **A**, the sankrantis fall 5–9 minutes earlier (Vrischikam 1197 at 13:09 IST, margin +7.9 min; Chingam 1199 at 13:39, +1.2 min).
   - That is enough to match the almanacs *without* the overrides the Dart package needs.
   - Two data points can't decide the convention. Choosing it is **Open question Q1**.
   - The months that would decide it: under Modern A, 7 month starts from 1990 to 2030 have the sankranti within 10 minutes of the cut-off.
     - Mithunam 1991-06-15 (+1.1 min)
     - Dhanu 2007-12-17 (−6.8)
     - **Thulam 2021-10-17 (+0.1)**
     - Vrischikam 2021-11-16 (+7.9)
     - Chingam 2023-08-17 (+1.2)
     - Kanni 2023-09-18 (−7.9)
     - Mithunam 2030-06-15 (+9.6)
   - The observed date of **Thulam 1 1197** (17 or 18 October 2021) alone separates A from B and should be sourced first.
3. Agreement with Swiss Ephemeris "Lahiri": spike A is 4.3–7.6 minutes later, spike B −0.5 to +16.9 minutes. Each figure combines differences in ayanamsa convention and in nutation handling. It does **not** measure the error of the solar theory.

The spike results in `tools/accuracy-spike/results/` are measurements only. **None of this code ships.**

## 4. Reuse recommendation

| Option | Verdict | Reason |
|---|---|---|
| Wrap the Maven jsii artifact behind a Kotlin adapter | **Rejected** | Needs Node.js at run time, so it can't run on Android. No ME→Gregorian. 78.5 % month-start agreement. Not thread-safe. |
| Port kollavarsham-js to Kotlin | Rejected | It would carry over the midnight rule and the Surya Siddhanta inaccuracy. |
| Port the Dart package to Kotlin | Not chosen as the primary path | It's legal (MIT) and accurate, but we would inherit its calibration choices and lose an independent cross-check. |
| **Native Kotlin engine from primary sources, validated against the Dart package, observed almanac data, and an independent ephemeris** | **Recommended** | It runs on Android, offline, with no dependencies. Validation stays independent, and the algorithm is documented from first principles. |

### Native Kotlin vs Java adapter

| Criterion | Native Kotlin | jsii Java adapter |
|---|---|---|
| Runs on Android | Yes | **No** (needs a Node.js child process) |
| Offline, no network | Yes | Yes (JVM only) |
| Transitive dependencies | Kotlin stdlib only | jsii-runtime, Jackson, a Node.js binary |
| ME → Gregorian | We implement it | Not implemented |
| Kerala month rule | Yes (3/5 of daytime) | No (midnight rule) |
| Thread safety | Immutable by design | Shared mutable state |
| Effort | Higher (estimated 2 sprints for the engine) | Low to integrate, but can't ship |
| Maintenance risk | Owned by us | Depends on an upstream project with low activity |

## 5. License review

| Source | License | What we may do | Attribution |
|---|---|---|---|
| kollavarsham-js / Maven | MIT | Read; cite as a reference | Only if code is copied (none planned) |
| kollavarsham/calendar | MIT | UI ideas only | n/a |
| AjeshAB/malayalam-calendar | MIT | Reference for observance rules (Sprint 4–5) | Only if code is copied |
| Dart `kollavarsham` | MIT | Cross-check. We may port with attribution, but we plan not to copy code. We can cite its documented observed dates and their primary sources. | Credit in `ACKNOWLEDGEMENTS.md` |
| Dart fixture `sankranti_1900_2100.csv` | Generated with **Swiss Ephemeris (AGPL / commercial)** | **Don't vendor it** until reviewed. Generate our own reference with a permissively licensed or public-domain ephemeris (e.g. JPL DE440 via NAIF SPICE, public domain). | – |
| Meeus, *Astronomical Algorithms* | Copyrighted book. The algorithms aren't copyrightable. | Implement from the published equations. | Cite in `CALENDAR_ALGORITHM.md` |
| `astronomy-engine` (spike only) | MIT | Spike measurement only; not shipped | n/a |

Policy: **No third-party source code is copied into the SDK.** Any future exception needs an ADR recording the license and adding attribution.

## 6. Known limitations and issues in the existing work

- **kollavarsham-js:**
  - `toGregorian` is not implemented.
  - The midnight rule.
  - The default location is Ujjain.
  - Mutable shared state.
  - Padded name strings.
  - Only three end-to-end tests.
- **Dart:**
  - Two hand-corrected months.
  - Several festival rules were fitted to only a few years.
  - Accuracy degrades outside 1900–2100 because ΔT is extrapolated.
- **AjeshAB:** no month-boundary tests; a monolithic engine.
- **All of them:** none cites a printed Kerala almanac for a large set of month starts. **No authoritative, machine-readable reference dataset exists**, and creating one is a key Sprint 2 dependency.

## 7. Sources

- Source code of #1, #3, #4 and #5, cloned 2026-11-14.
- The `kollavarsham-converter` POM from Maven Central. Central rate-limited the metadata request, but the POM was retrieved.
- pub.dev package API for `kollavarsham` 1.2.1.
- R. Sewell & S. B. Dikshit, *The Indian Calendar* (1896): rules for the start of a solar month, including the Malabar "afternoon rule".
- [Hindu solar calendar differences (secondary)](https://www.hindu-blog.com/2019/04/hindu-solar-calendars-differences.html): the Malayalam rule, sankranti before 3/5 of daytime.
- [kollavarsham.org implementations](https://www.kollavarsham.org/implementations.html): could not be fetched, because the host isn't on this session's network allowlist.
