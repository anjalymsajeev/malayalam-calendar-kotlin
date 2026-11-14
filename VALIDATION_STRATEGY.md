# Validation strategy

Status: **Proposed (Sprint 0)**

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
