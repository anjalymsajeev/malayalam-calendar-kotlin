# Validation strategy

Status: **In use (Sprint 2).** Current results are in §6.

Accuracy is the product's most important requirement. We only claim what we can show evidence for.

## 1. Evidence tiers

| Tier | Source | Used for | Weight |
|---|---|---|---|
| **T1 Observed** | Printed Kerala almanacs (e.g. Malayala Manorama, Mathrubhumi, Kerala Government calendar or public-holiday orders), Devaswom announcements (Mandala Pooja date ⇒ Vrischikam 1), dated news of Chingam 1 or Vishu | Ground truth for month starts; drives overrides | Highest |
| **T2 Independent computation** | JPL DE440 ephemeris (public domain) through our own reference script; drikpanchang published sankranti times | Sankranti instants (≤ 1 min), sunrise and sunset (≤ 1 min) | High for astronomy, not for observance |
| **T3 Peer implementations** | Dart `kollavarsham` 1.2.1, AjeshAB engine, kollavarsham-js | Detecting disagreements; never ground truth | Low |

Rules:

- A disagreement between our engine and T1 is a **bug or an override candidate**.
- A disagreement with T3 alone is only **something to investigate**.

## 2. Reference dataset (built in Sprint 2)

- Location: `kollavarsham-core/src/test/resources/reference/month-starts.csv`
- Columns: `me_year, month, first_day, tier, source_citation, retrieved_on, notes`
- **Every T1 row must have a citation that a reviewer can check.**

### Seed list

Taken from the Dart package's test suite. These are T3-reported observations: **each needs a primary T1 citation before it counts.**

| ME | Month | First day | Primary citation |
|---|---|---|---|
| 1197 | Vrischikam | 2021-11-16 | Mandala Pooja 2021-12-26 (to be sourced) |
| 1198 | Chingam | 2022-08-17 | to be sourced |
| 1199 | Chingam | 2023-08-17 | Kerala New Year 2023-08-17 (to be sourced) |
| 1200 | Chingam | 2024-08-17 | to be sourced |
| 1201 | Chingam | 2025-08-17 | to be sourced |
| 1198 | Medam | 2023-04-15 | Vishu (to be sourced) |
| 1199 | Medam | 2024-04-14 | to be sourced |
| 1200 | Medam | 2025-04-14 | to be sourced |
| 1199 | Makaram | 2024-01-15 | to be sourced |
| 1200 | Makaram | 2025-01-14 | to be sourced |
| 1200 | Vrischikam | 2024-11-16 | to be sourced |
| 1199 | Karkidakam | 2024-07-16 | to be sourced |
| 1200 | Karkidakam | 2025-07-17 | to be sourced |
| 1201 | Kanni | 2025-09-17 | to be sourced |

### Priority additions

These are the months closest to the cut-off from 1990 to 2030, from the Sprint 0 spike:

- **Thulam 1197** (2021-10-17 or 18, margin +0.1 min). This one decides Q1.
- Mithunam 1166 (1991-06-15)
- Dhanu 1183 (2007-12-17)
- Kanni 1199 (2023-09-18)
- Mithunam 1205 (2030-06-15)

Target for the Sprint 2 exit: **≥ 60 T1 rows**, including every month of ME 1199–1202 and all 7 borderline months.

## 3. Test suites

| Suite | What it checks | Sprint |
|---|---|---|
| Model unit tests | Enum order, `next`/`previous`, structural validation, `Comparable`, `equals` | 1 |
| Boundary-mechanics tests | Conversion and arithmetic against a fake boundary table, including edge cases (day 32, first and last day of the year, Chingam rollover) | 1 |
| Astronomy tests | Meeus worked examples (ch. 15, 22, 25, 32) to the stated precision; sankranti vs T2 reference to ≤ 1 min for every sankranti 1900–2100 | 2 |
| **Reference tests** | Every T1 row: `fromGregorian(first_day) == (year, month, 1)`, and the day before is the last day of the previous month | 2 |
| **Invariant / property tests, 1900–2100** | Consecutive days advance by exactly 1; months are 29–32 days; there are 12 months per year; ME year + 1 only at Chingam 1; `toGregorian(fromGregorian(d)) == d` for every day; Σ month lengths = 365 or 366 days ± 1 between consecutive Chingam 1s | 2 |
| Year rule | ME = G − 824 from Chingam 1 to 31 Dec, else G − 825, for every day | 2 |
| Gregorian leap years | Feb 29 in 1900 (not leap), 2000 (leap), 2024 and 2100 (not leap) converts and round-trips | 2 |
| Range errors | 1899-12-31 and 2101-01-01 throw; `...OrNull` variants return null | 2 |
| Borderline report | Generated list of \|margin\| < 10 min for 1900–2100, checked in. Any change in the list fails CI until reviewed. | 2 |
| Peer diff report | Diff against Dart month starts, not a pass/fail test. Every difference must be explained in `docs/validation/peer-diff.md`. | 2 |
| Concurrency | 32 threads hammering random dates give the same results as single-threaded runs | 2 |
| API compatibility | binary-compatibility-validator `apiCheck` | 1+ |

## 4. Publishing accuracy statements

Release notes and the README must state accuracy in this form:

> Month starts match **N / N** cited almanac observations for ME *a*–*b*. Between 1900 and 2100, *K* month starts have a sankranti within 10 minutes of the cut-off; for these, almanacs may differ by one day. The list is available.

Words such as "100 % accurate" are not allowed.

## 5. Recording disagreements

Disagreements between almanacs, or between an almanac and astronomy, are logged in `docs/validation/disagreements.md`. Each entry records the date, the sources on each side, the computed margin, and the resolution: override, rule change, or "documented, no change".

## 6. Status after Sprint 2

| Item | Status |
|---|---|
| Reference dataset (`kollavarsham-core/src/test/resources/reference/month-starts.csv`) | **21 rows: 11 T1-search, 10 T3-reported.** All are reproduced by the engine. The Sprint 2 target of 60 or more T1 rows was **not met** (see below). |
| T1-search tier | New tier: a dated news report found by web search whose page couldn't be opened from the build environment. Each row needs a reviewer to read the linked page and promote it to T1. |
| T2 astronomy reference | astronomy-engine fixtures (sankranti 1899–2101, sunrise and sunset 2000–2030) in `kollavarsham-astronomy/src/test/resources/reference/`, generated by `tools/reference-gen`. JPL DE440 is still to do, because the JPL hosts are unreachable from the build environment. |
| Swiss Ephemeris cross-check | Done once, locally, against the Dart package's fixture: about 1 s standard deviation for 1900–2025. The fixture isn't vendored (AGPL provenance). |
| Invariants | Every day 1900–2100: round trip, consecutive days, month lengths 29–32, years of 365 or 366 days, ME year offset. |
| Borderline list | `borderline-months.csv` (30 months). The test fails on any change. |
| Peer diff vs the Dart package | Not done as code. Its default convention is B, so we expect it to differ on 9 of 2,424 months for 1900–2100, plus its two hand overrides. To be written up in `docs/validation/peer-diff.md`. |

**Why the T1 target was missed:** the build environment can't open news, government or almanac sites. Only search summaries were available. To reach 60 or more T1 rows we need one of the following:

- **(a)** Network access to the news sites, so pages can be read and cited properly.
- **(b)** Printed almanac pages (Manorama, Mathrubhumi, Kerala Government calendar) photographed or transcribed by the project owner.
- **(c)** Contributors submitting cited dates.

The highest-value months to source are the 30 borderline months, especially 2023-09-18 (Kanni 1199), 2030-06-15 (Mithunam 1205) and 2035-05-15 (Edavam 1210).

## 7. Festival validation (Sprint 5)

| Item | Result |
|---|---|
| Reference (`kollavarsham-festivals/src/test/resources/reference/festival-dates.csv`) | 126 dates, 2009–2027, for 16 festivals plus Easter |
| In-sample (T3 dates from the Dart package's tests, with their original sources) | 116 of 116 reproduced. **Caution:** the Dart author fitted the rules to these same dates, so this mainly confirms that our independent engine implements the documented rules. |
| **Out-of-sample** (2026–2027 dates the rules were not fitted to) | **4 of 5:** Onam 2026, Attukal Pongala 2026, Thrissur Pooram 2026 and Karkidaka Vavu 2026 match. Thrissur Pooram 2027 (announced 17 April) does not: the engine gives 14 May. |
| Known miss: Thrissur Pooram | When Pooram falls twice in Medam, Kerala kept the later day in 2019 and announced the earlier day for 2027. No first/last rule fits both, and two cases can't justify a new rule. The miss is recorded in the dataset (`KNOWN-MISS`), and `FestivalCalendar.candidates()` shows both days. |
| Not shipped | Ashtami Rohini (the rule is not simple), Dhanu Thiruvathira (sources disagree), Thaipooyam (no Kerala source), Islamic festivals (moon sighting). Apps can add these as dated data. |
