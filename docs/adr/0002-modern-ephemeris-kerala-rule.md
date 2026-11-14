# ADR-0002: Modern solar ephemeris, Lahiri ayanamsa, and the Kerala 3/5-of-daytime rule

- Status: **Accepted** (Sprint 2). Convention A (`LahiriTrue`) was chosen on evidence.
- Date: 2026-11-14

## Decision

A month starts on the civil (IST) day of the nirayana sankranti if the sankranti falls before sunrise + 0.6 × daylength at the reference location. Otherwise it starts on the next day.

- Solar longitude: VSOP87 truncated (Meeus ch. 32) with nutation and aberration.
- ΔT: from a table.
- Sunrise: upper limb with −0.833° refraction.
- Reference location: Thiruvananthapuram by default, configurable.

The Lahiri convention (A: 1956 definition plus nutation; B: drikpanchang-calibrated) is chosen in Sprint 2 by testing both against the T1 dataset.

- Convention A matched 14 of 14 observations in the spike; B matched 12 of 14.
- They differ on 3 of 492 month starts between 1990 and 2030.
- The convention is kept **internal**, not a public option, until the evidence is in.

## Alternatives rejected

- **Surya Siddhanta model:** measured as inaccurate for current Kerala almanacs.
- **Midnight rule:** a Bengal-style rule, not the Kerala practice.
- **A fixed Gregorian month-day table:** month lengths vary between 29 and 32 days.

## Consequences

- Borderline months (|margin| < 10 min) are inherently uncertain.
- `MonthBoundary.cutoffMargin` is exposed so that applications can show this uncertainty.

## Sprint 2 outcome

- The Lahiri convention is **A** (`Ayanamsa.LahiriTrue`). It matched 11 of 11 cited observations; B matched 8 of 11, failing all three months where the conventions differ.
- Thulam 1197 has a computed margin of about 0 minutes, inside the engine's uncertainty. It is pinned by a cited verified override.
