# Contributing

## Build

```bash
./gradlew build
```

`build` compiles, runs the unit tests, checks the public API against `*/api/*.api`, and verifies that
`kollavarsham-core` has no Android or UI dependencies.

## Public API changes

The public API is tracked with the Kotlin binary-compatibility-validator. If you change it on purpose:

```bash
./gradlew apiDump
```

Commit the updated `.api` files, and explain the change in the pull request and in `CHANGELOG.md`.

## Rules

- **Accuracy:** never change calendar results without evidence. A change to a month start needs a cited
  source (see `VALIDATION_STRATEGY.md`), and a new verified override needs a tier-1 citation in
  `verified-overrides.csv`.
- **No copied code:** don't paste code from other projects. If reuse is ever justified, record the
  license and attribution in an ADR first.
- **Core stays pure:** no Android, UI, I/O, network or system clock in `kollavarsham-core` or
  `kollavarsham-astronomy`.
- **Style:** follow the Kotlin coding conventions. Every public declaration has KDoc with a usage example.
- **Tests:** each change comes with unit tests. Keep commits small and logically grouped.
