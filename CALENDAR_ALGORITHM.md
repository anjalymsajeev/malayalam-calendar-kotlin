# Calendar algorithm

Status: **Proposed (Sprint 0)**. The engine is implemented in Sprint 2.

Labels: [FACT] verified · [SOURCE] stated in a cited primary or secondary source · [DECISION] our choice · [OPEN] unresolved.

## 1. What the Kollavarsham calendar is

- [SOURCE] The Malayalam calendar is **sidereal and solar**.
  - Each month corresponds to the Sun's transit through one sidereal rashi (30° of nirayana longitude).
  - A month starts on the civil day determined by the moment of **sankranti**, when the Sun enters the rashi.
- [FACT] Months last **29 to 32 days**, and the length changes from year to year. Every month length in our spike falls within this range. A fixed month-to-Gregorian-day table is therefore wrong, as is a simple year offset.
- [SOURCE] The year begins at **Chingam 1** (Simha sankranti), mid-August. In North Malabar it traditionally began at Kanni 1. The SDK will support Kanni later as a numbering option; the month starts themselves don't change.
- [FACT] There are **no leap days or leap months** in the solar Kollavarsham. The Gregorian leap rule affects only the Gregorian side; java.time handles it.

| # | Malayalam month | Rashi (sidereal sign) | Sun's nirayana longitude | Approximate start |
|---|---|---|---|---|
| 1 | Chingam ചിങ്ങം | Simha | 120° | 16–17 Aug |
| 2 | Kanni കന്നി | Kanya | 150° | 16–18 Sep |
| 3 | Thulam തുലാം | Tula | 180° | 17–18 Oct |
| 4 | Vrischikam വൃശ്ചികം | Vrischika | 210° | 16–17 Nov |
| 5 | Dhanu ധനു | Dhanu | 240° | 16–17 Dec |
| 6 | Makaram മകരം | Makara | 270° | 14–15 Jan |
| 7 | Kumbham കുംഭം | Kumbha | 300° | 13–14 Feb |
| 8 | Meenam മീനം | Meena | 330° | 14–15 Mar |
| 9 | Medam മേടം | Mesha | 0° | 14–15 Apr |
| 10 | Edavam ഇടവം | Vrishabha | 30° | 14–15 May |
| 11 | Mithunam മിഥുനം | Mithuna | 60° | 14–15 Jun |
| 12 | Karkidakam കർക്കടകം | Karka | 90° | 16–17 Jul |

## 2. Month-start rule

[SOURCE] Kerala uses the "afternoon rule" (Sewell & Dikshit 1896, rules for the beginning of the solar month). It is implemented identically by the Dart package and by AjeshAB:

> Let *S* be the sankranti moment and *D* the civil day containing *S*.
> If *S* occurs before **sunrise + 3/5 × (sunset − sunrise)** on day *D*, then *D* is day 1 of the new month.
> Otherwise day 1 is *D* + 1.

Other regions use different rules: Tamil Nadu uses sunset, Odisha uses a same-day rule, and Bengal uses midnight. This is why **kollavarsham-js, which uses a midnight rule, is wrong for Kerala** about a fifth of the time (see the research report §3).

Details we have decided on and those still open:

| Topic | Choice | Status |
|---|---|---|
| Civil day | Midnight to midnight, IST (UTC+05:30) | [DECISION] Kerala civil practice |
| Sankranti before sunrise on day *D* | Day *D* is day 1 (it is before the cut-off) | [DECISION] Consistent with the rule and with Medam 2025, sankranti 03:30 IST, day 1 = 14 Apr |
| Sunrise and sunset definition | Upper limb on the horizon, refraction −0.833° | [DECISION] Standard. Changing the definition shifts the cut-off by about 1–2 minutes. |
| Reference location | Thiruvananthapuram (8.5241° N, 76.9366° E) by default, configurable | [OPEN Q2] Which location do almanacs use? A location in north Kerala moves the cut-off by a few minutes. |
| Ayanamsa | Lahiri (Chitrapaksha) | [SOURCE] Indian national standard |
| Lahiri convention | A (1956 definition + nutation) or B (drikpanchang-calibrated) | [OPEN Q1] They differ by 5–9 minutes, which changes 3 of 492 month starts between 1990 and 2030. Decided from evidence in Sprint 2. |
| Almanac exceptions | A curated, evidence-backed **override table**, kept as data | [DECISION] ADR-0005 |

## 3. Computation pipeline

```
Gregorian date ──► epoch day ──► which month contains it?
                                     │
            month-start lookup ◄─────┘ (cached per ME year, computed on demand)
                    │
   for (year, month): approximate date of sankranti
        ──► root-find the instant when the Sun's sidereal longitude = k·30°  (TT → UT via ΔT)
        ──► sunrise and sunset at the reference location on that civil day
        ──► apply the 3/5 rule ──► civil start date
        ──► apply a verified override if one is present ──► MonthBoundary
```

1. **Solar longitude:**
   - Use VSOP87 truncated as in Meeus, *Astronomical Algorithms* 2nd ed., ch. 32, giving heliocentric longitude, then geocentric.
   - Add nutation in longitude (ch. 22) and aberration (ch. 25) to get the **apparent** tropical longitude.
   - Target accuracy: ≤ 1″, about 25 seconds of time at a sankranti.
2. **Sidereal longitude:** the apparent longitude minus the Lahiri ayanamsa, using the convention chosen under Q1.
3. **Sankranti:**
   - Bracket the crossing by mean solar motion, then find it with bisection or Brent to within 1 second.
   - The calculation is deterministic and contains no time-of-day randomness.
4. **ΔT:**
   - A table of yearly values for 1900–2100 (IERS observed values, then Stephenson–Morrison–Hohenkerk 2016 extrapolation). Polynomials are used outside that range.
   - [OPEN Q5] Pick a source with a clear license.
5. **Sunrise and sunset:** Meeus ch. 15, iterated, for the upper limb with −0.833° refraction.
6. **Month starts** feed both conversion directions:
   - `fromGregorian(d)`: find the month whose start ≤ *d* < next start. Day number = *d* − start + 1.
   - `toGregorian(y, m, day)`: start(y, m) + day − 1. If *day* > month length, return an explicit error (never roll over silently).
7. **Year:** the ME year increments at Chingam 1. Equivalently, it is the Gregorian year − 824 for dates on or after Chingam 1 in that Gregorian year, and − 825 before.
   - [DECISION] The year is derived from the boundary computation. The offset formula is used only as an assertion in tests.

### Performance

- [DECISION] Month starts are computed lazily per ME year and memoised in an immutable, thread-safe cache.
- At about 12 sankrantis per year, a few hundred solar evaluations, each costing microseconds, take well under 1 ms per year on a phone.
- A generated month-length table, like the one in the Dart package, is kept as an **optimisation to consider in Sprint 7, backed by benchmarks**. It must be generated by our own engine in the build, never hand-maintained.

## 4. Supported range

- [DECISION] Initial declared range: **1 Jan 1900 to 31 Dec 2100 CE (ME 1075–1276)**.
  - The ΔT data is good across this range.
  - Dates outside it throw `DateOutOfSupportedRangeException`. They never return a silently degraded result.
- Validation density is highest from 1990 to 2030 (see `VALIDATION_STRATEGY.md`).
- Historical dates before 1900 need ΔT extrapolation (uncertain by minutes to hours) and possibly Julian-calendar handling for pre-1582 dates. **They are out of scope until a later ADR.**

## 5. Known disagreement sources

- **Almanac vs computation at borderline months:** when the sankranti falls within about ±10 minutes of the cut-off, almanacs can disagree with each other and with any computation. Causes include different sankranti values (Surya Siddhanta vs drik), sunrise conventions and locations.
  - We report a **margin** for every month boundary (`MonthBoundary.cutoffMargin`) so applications can flag uncertain dates.
- **Traditional (Surya Siddhanta) vs modern (drik) almanacs:** some printed panchangams may still use traditional computations. [OPEN Q3] Which almanac is authoritative for this product? The default choice is drik, matching mainstream Kerala almanacs and the Kerala Government calendar. [ASSUMPTION; to verify]
- **No claim of 100 % accuracy.** Published accuracy statements will be limited to "matched N of N sourced observations in years X–Y; K months in the range fall within 10 minutes of the cut-off."

## 6. References

1. R. Sewell & S. B. Dikshit, *The Indian Calendar*, London 1896: solar month rules; the "afternoon rule" for Malabar.
2. J. Meeus, *Astronomical Algorithms*, 2nd ed., Willmann-Bell 1998: chapters 15, 22, 25, 32.
3. E. M. Reingold & N. Dershowitz, *Calendrical Calculations*, 4th ed., CUP 2018: Hindu solar calendars (cross-check of the rule formulations).
4. Indian Calendar Reform Committee report (1955): the Lahiri ayanamsa definition.
5. Cross-check implementations: Dart `kollavarsham` 1.2.1 (MIT) and AjeshAB/malayalam-calendar (MIT). Reference only; no code is copied.
