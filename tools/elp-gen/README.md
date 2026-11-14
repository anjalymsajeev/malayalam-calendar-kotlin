# Lunar term generator

This generates `kollavarsham-astronomy/src/main/kotlin/.../MoonTerms.kt` from Meeus Tables 47.A and 47.B.

```bash
npm install --ignore-scripts astronomia@4.1.1
```

```bash
node generate.mjs node_modules/astronomia/src/moonposition.js ../../kollavarsham-astronomy/src/main/kotlin/io/github/anjalymsajeev/kollavarsham/astronomy/MoonTerms.kt
```

`astronomia` (MIT) is only the carrier of the published table values. It is not a dependency of the SDK.
