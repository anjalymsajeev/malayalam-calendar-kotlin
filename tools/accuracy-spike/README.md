# Accuracy spike (Sprint 0)

This is throwaway measurement code. **It is not part of the SDK and is never shipped.**

`compare.js` computes Kerala month starts for 1990–2030 in two ways and compares them:

- **Modern:** `astronomy-engine` (VSOP87) for the Sun, the Lahiri ayanamsa, and the 3/5-of-daytime rule at Thiruvananthapuram.
- **kollavarsham-js:** the published npm package.

Both are also compared with the observed Kerala dates listed in `VALIDATION_STRATEGY.md`.

## Run

`sankranti_1900_2100.csv` is the Dart package's Swiss-Ephemeris fixture. It is used only for a sanity check of sankranti instants and is **not vendored here** (see the license review). Download it from `midhunarmid/kollavarsham` at `packages/kollavarsham/test/fixtures/`.

```bash
npm install
```

```bash
AYANAMSA=A TZ=Asia/Kolkata node compare.js "$PWD/node_modules" /path/to/sankranti_1900_2100.csv results
```

`AYANAMSA` selects the convention:

- `A`: the 1956 Lahiri definition, IAU 2006 precession, plus nutation.
- `B`: the Dart package's drikpanchang-calibrated convention.

## Results (2026-11-14)

| | Modern A | Modern B | kollavarsham-js |
|---|---|---|---|
| Observed starts matched (n = 14) | 14 | 12 | 11 |
| Agreement with Modern A, 1990–2030 (n = 492) | – | 489 | 386 |

The raw outputs are in `results/`.
