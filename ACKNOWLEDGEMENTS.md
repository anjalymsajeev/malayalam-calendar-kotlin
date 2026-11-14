# Acknowledgements

No third-party source code is included in this SDK. The following works informed its design and are
used as references or cross-checks:

- R. Sewell & S. B. Dikshit, *The Indian Calendar* (1896): solar-month rules.
- J. Meeus, *Astronomical Algorithms*, 2nd ed. (1998): solar position and sunrise algorithms.
- P. Bretagnon & G. Francou, VSOP87 (A&A 202, 309, 1988): the Earth series coefficients in `Vsop87Earth.kt`. They were taken
  unmodified from the MIT-licensed npm package [astronomia](https://github.com/commenthol/astronomia) (data file only) by
  `tools/vsop87-gen`.
- M. Chapront-Touzé & J. Chapront, ELP-2000/82, as abridged in Meeus's Tables 47.A/47.B: the lunar terms in `MoonTerms.kt`,
  extracted from `astronomia` (MIT) by `tools/elp-gen`.
- F. Espenak & J. Meeus, NASA *Five Millennium Canon of Solar Eclipses* (2006): ΔT polynomial fits.
- [astronomy-engine](https://github.com/cosinekitty/astronomy) (MIT, Don Cross): used only in `tools/` to generate test reference data.
- [kollavarsham-js](https://github.com/kollavarsham/kollavarsham-js) (MIT, The Kollavarsham Team).
- [kollavarsham for Dart](https://github.com/midhunarmid/kollavarsham) (MIT, Midhun Mohanan): observed
  month starts and borderline-month analysis used as cross-checks.
- [AjeshAB/malayalam-calendar](https://github.com/AjeshAB/malayalam-calendar) (MIT, Ajesh AB).
