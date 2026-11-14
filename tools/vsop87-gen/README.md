# VSOP87 coefficient generator

This generates `kollavarsham-astronomy/src/main/kotlin/.../Vsop87Earth.kt` from the published VSOP87D Earth series.

```bash
npm install --ignore-scripts astronomia@4.1.1
```

```bash
node generate.mjs node_modules/astronomia/data/vsop87Dearth.js ../../kollavarsham-astronomy/src/main/kotlin/io/github/anjalymsajeev/kollavarsham/astronomy/Vsop87Earth.kt
```

`astronomia` (MIT) is only the carrier of the unmodified published coefficients. It is not a dependency of the SDK.
